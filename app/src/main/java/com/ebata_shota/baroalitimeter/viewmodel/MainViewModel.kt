package com.ebata_shota.baroalitimeter.viewmodel

import androidx.annotation.StringRes
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.domain.extensions.logUserActionEvent
import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.model.content.UserActionEvent
import com.ebata_shota.baroalitimeter.domain.repository.CalcRepository
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import com.ebata_shota.baroalitimeter.ui.model.SensorAndPrefModel
import com.ebata_shota.baroalitimeter.ui.parts.AltitudePartsEvents
import com.ebata_shota.baroalitimeter.ui.parts.TemperaturePartsEvents
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val sensorRepository: SensorRepository,
    private val prefRepository: PrefRepository,
    private val calcRepository: CalcRepository,
    private val firebaseAnalytics: FirebaseAnalytics,
) : ViewModel(),
    TemperaturePartsEvents,
    AltitudePartsEvents {

    sealed interface MainUiState {

    sealed interface ContentUiState {

        object Loading : ContentUiState

        data class VisibleUiState(
            val pressureText: String,
            val seaLevelPressureText: String,
            val temperatureUiState: TemperatureUiState,
            val altitudeUiState: AltitudeUiState,
        ) : ContentUiState

        sealed interface TemperatureUiState {

            data class ViewerMode(
                val temperatureText: String
            ) : TemperatureUiState

            data class EditMode(
                val temperatureTextFieldValue: TextFieldValue
            ) : TemperatureUiState
        }

        sealed interface AltitudeUiState {

            data class ViewerMode(
                val altitudeText: String
            ) : AltitudeUiState

            data class EditMode(
                val altitudeTextFieldValue: TextFieldValue
            ) : AltitudeUiState
        }
    }

    enum class ShowUndoSnackBarEvent(
        @StringRes val snackBarText: Int,
    ) {
        Temperature(R.string.snack_bar_message_change_temperature),
        Altitude(R.string.snack_bar_message_change_altitude)
    }

    enum class Mode {
        Viewer,
        EditTemperature,
        EditAltitude,
    }

    private val _modeState: MutableStateFlow<Mode> = MutableStateFlow(Mode.Viewer)
    val modeState: StateFlow<Mode> = _modeState.asStateFlow()

    val themeState: StateFlow<ThemeMode?> = prefRepository
        .preferencesFlow
        .map { it.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    fun onSelectedThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            prefRepository.setThemeMode(themeMode)
        }
    }

    private val _contentUiState = MutableStateFlow<ContentUiState>(ContentUiState.Loading)
    val contentUiState: StateFlow<ContentUiState> = _contentUiState.asStateFlow()

    private val _showUndoSnackBarEvent = MutableSharedFlow<ShowUndoSnackBarEvent>()
    val showUndoSnackBarEvent: SharedFlow<ShowUndoSnackBarEvent> = _showUndoSnackBarEvent.asSharedFlow()

    init {
        @Suppress("UnexpectedIndentation")
        viewModelScope.launch {
            combine(
                modeState,
                sensorRepository.pressureSensorState,
                sensorRepository.temperatureSensorState,
                prefRepository.preferencesFlow,
            ) {
                    mode: Mode,
                    pressureSensorState: Pressure,
                    temperatureSensorState: Temperature, // TODO: temperatureSensorStateをうまく使う
                    preferencesModel: PreferencesModel,
                ->
                SensorAndPrefModel(
                    mode = mode,
                    pressureSensorState = pressureSensorState,
                    temperatureSensorState = temperatureSensorState,
                    preferencesModel = preferencesModel
                )
            }.distinctUntilChanged { old, new ->
                // 重複を無視する
                old == new
            }.collect {
                _contentUiState.update { currentUiState ->
                    currentUiState.nextUiState(it)
                }
            }
        }
    }

    private suspend fun ContentUiState.nextUiState(
        sensorAndPrefModel: SensorAndPrefModel
    ): ContentUiState {
        val pressureSensorState: Pressure = sensorAndPrefModel.pressureSensorState
        val mode: Mode = sensorAndPrefModel.mode
        val preferencesModel: PreferencesModel = sensorAndPrefModel.preferencesModel
        return when (val currentMainUiState: ContentUiState = this) {

            ContentUiState.Loading -> when (pressureSensorState) {
                is Pressure.Loading -> ContentUiState.Loading
                is Pressure.Success -> {
                    val pressure = pressureSensorState.value
                    val temperature = preferencesModel.temperature
                    val seaLevelPressure = preferencesModel.seaLevelPressure
                    val pressureText = pressure.formattedString(1)
                    val seaLevelPressureText = seaLevelPressure.formattedString(2)
                    val temperatureText = temperature.formattedString(0)
                    val altitudeText = calcRepository.calcAltitude(
                        pressure = pressure,
                        temperature = temperature,
                        seaLevelPressure = seaLevelPressure
                    ).formattedString(0)

                    ContentUiState.VisibleUiState(
                        pressureText = pressureText,
                        seaLevelPressureText = seaLevelPressureText,
                        temperatureUiState = ContentUiState.TemperatureUiState.ViewerMode(temperatureText),
                        altitudeUiState = ContentUiState.AltitudeUiState.ViewerMode(altitudeText)
                    )
                }
            }

            is ContentUiState.VisibleUiState -> when (pressureSensorState) {
                Pressure.Loading -> ContentUiState.Loading
                is Pressure.Success -> {
                    val pressure = pressureSensorState.value
                    val temperature = preferencesModel.temperature
                    val seaLevelPressure = preferencesModel.seaLevelPressure
                    val pressureText = pressure.formattedString(1)
                    val seaLevelPressureText = seaLevelPressure.formattedString(2)
                    val temperatureText = temperature.formattedString(0)
                    val altitudeText = calcRepository.calcAltitude(
                        pressure = pressure,
                        temperature = temperature,
                        seaLevelPressure = seaLevelPressure
                    ).formattedString(0)

                    currentMainUiState.copy(
                        pressureText = pressureText,
                        seaLevelPressureText = seaLevelPressureText,
                        temperatureUiState = when (mode) {
                            Mode.Viewer, Mode.EditAltitude -> ContentUiState.TemperatureUiState.ViewerMode(temperatureText)
                            Mode.EditTemperature -> when (currentMainUiState.temperatureUiState) {
                                // 元々Editモードならそのまま
                                is ContentUiState.TemperatureUiState.EditMode -> currentMainUiState.temperatureUiState
                                // ViewerモードならEditモードに変更
                                is ContentUiState.TemperatureUiState.ViewerMode -> ContentUiState.TemperatureUiState.EditMode(
                                    temperatureTextFieldValue = TextFieldValue(
                                        text = temperatureText,
                                        selection = TextRange(temperatureText.length)
                                    )
                                )
                            }
                        },
                        altitudeUiState = when (mode) {
                            Mode.Viewer, Mode.EditTemperature -> ContentUiState.AltitudeUiState.ViewerMode(altitudeText)
                            Mode.EditAltitude -> when (currentMainUiState.altitudeUiState) {
                                // 元々Editモードならそのまま
                                is ContentUiState.AltitudeUiState.EditMode -> currentMainUiState.altitudeUiState
                                // ViewerモードならEditモードに変更
                                is ContentUiState.AltitudeUiState.ViewerMode -> ContentUiState.AltitudeUiState.EditMode(
                                    altitudeTextFieldValue = TextFieldValue(
                                        text = altitudeText,
                                        selection = TextRange(altitudeText.length)
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onClickTemperature() {
        changeModeToEditTemperature()
    }

    private fun changeModeToEditTemperature() {
        _modeState.update { mode ->
            val state = contentUiState.value
            if (state is ContentUiState.VisibleUiState && state.temperatureUiState is ContentUiState.TemperatureUiState.ViewerMode) {
                logUserActionEvent(UserActionEvent.EditTemperature)
                Mode.EditTemperature
            } else mode
        }
    }

    override fun onClickAltitude() {
        changeModeToEditAltitude()
    }

    private fun changeModeToEditAltitude() {
        _modeState.update { mode ->
            val state = contentUiState.value
            if (state is ContentUiState.VisibleUiState && state.altitudeUiState is ContentUiState.AltitudeUiState.ViewerMode) {
                logUserActionEvent(UserActionEvent.EditAltitude)
                Mode.EditAltitude
            } else mode
        }
    }

    override fun onChangeTemperatureTextFieldValue(textFieldValue: TextFieldValue) {
        updateTemperatureTextFieldValue(textFieldValue)
    }

    private fun updateTemperatureTextFieldValue(textFieldValue: TextFieldValue) {
        _contentUiState.update { currentMainUiState ->
            // 表示できるUiStateじゃないなら無視
            if (currentMainUiState !is ContentUiState.VisibleUiState) return@update currentMainUiState

            // Editモードじゃないなら無視
            val temperatureUiState = currentMainUiState.temperatureUiState
            if (temperatureUiState !is ContentUiState.TemperatureUiState.EditMode) return@update currentMainUiState

            // textFieldValueを更新
            currentMainUiState.copy(
                temperatureUiState = temperatureUiState.copy(
                    temperatureTextFieldValue = textFieldValue
                )
            )
        }
    }

    override fun onChangeAltitudeTextFieldValue(textFieldValue: TextFieldValue) {
        updateAltitudeTextFieldValue(textFieldValue)
    }

    private fun updateAltitudeTextFieldValue(textFieldValue: TextFieldValue) {
        _contentUiState.update { currentMainUiState ->
            // 表示できるUiStateじゃないなら無視
            if (currentMainUiState !is ContentUiState.VisibleUiState) return@update currentMainUiState

            // Editモードじゃないなら無視
            val altitudeUiState = currentMainUiState.altitudeUiState
            if (altitudeUiState !is ContentUiState.AltitudeUiState.EditMode) return@update currentMainUiState

            // textFieldValueを更新
            currentMainUiState.copy(
                altitudeUiState = altitudeUiState.copy(
                    altitudeTextFieldValue = textFieldValue
                )
            )
        }
    }

    override fun onClickCancelEditTemperature() {
        cancelEditTemperature()
    }

    private fun cancelEditTemperature() {
        logUserActionEvent(UserActionEvent.CancelEditTemperatureByButton)
        changeModeToViewer()
    }

    override fun onClickCancelAltitude() {
        cancelEditAltitude()
    }

    private fun cancelEditAltitude() {
        logUserActionEvent(UserActionEvent.CancelEditAltitudeByButton)
        changeModeToViewer()
    }

    fun onBackPressedCallback() {
        when (modeState.value) {
            Mode.Viewer -> Unit // 発生しないケース
            Mode.EditTemperature -> logUserActionEvent(UserActionEvent.CancelEditTemperatureByOnBackPressedCallback)
            Mode.EditAltitude -> logUserActionEvent(UserActionEvent.CancelEditAltitudeByOnBackPressedCallback)
        }
        changeModeToViewer()
    }

    private fun changeModeToViewer() {
        _modeState.update {
            Mode.Viewer
        }
    }

    override fun onClickDoneEditTemperature() {
        onCompletedEditTemperature()
        showTemperatureUndoSnackBar()
    }

    private fun onCompletedEditTemperature() {
        viewModelScope.launch {
            logUserActionEvent(UserActionEvent.DoneEditTemperature)
            val currentMainUiState = contentUiState.value

            // 表示できるUiStateじゃないなら無視
            if (currentMainUiState !is ContentUiState.VisibleUiState) return@launch

            // Editモードじゃないなら無視
            val temperatureUiState = currentMainUiState.temperatureUiState
            if (temperatureUiState !is ContentUiState.TemperatureUiState.EditMode) return@launch

            // 圧力センサーの値を取得できる状態じゃないなら無視
            val pressureState = sensorRepository.pressureSensorState.value
            if (pressureState !is Pressure.Success) return@launch

            try {
                val temperature = temperatureUiState.temperatureTextFieldValue.text.toFloat() // FIXME: 入力制限をしていないので、Floatの範囲外に入ると問題になる
                prefRepository.setTemperature(temperature)
            } catch (e: NumberFormatException) {
                // 変換に失敗したら、特に何もない
            } finally {
                // 最終的に編集モードを終了する
                changeModeToViewer()
            }
        }
    }

    private fun showTemperatureUndoSnackBar() {
        viewModelScope.launch {
            _showUndoSnackBarEvent.emit(ShowUndoSnackBarEvent.Temperature)
        }
    }

    override fun onClickDoneEditAltitude() {
        onCompletedEditAltitude()
        showAltitudeUndoSnackbar()
    }

    private fun onCompletedEditAltitude() {
        viewModelScope.launch {
            logUserActionEvent(UserActionEvent.DoneEditAltitude)
            val currentMainUiState = contentUiState.value

            // 表示できるUiStateじゃないなら無視
            if (currentMainUiState !is ContentUiState.VisibleUiState) return@launch

            // Editモードじゃないなら無視
            val altitudeUiState = currentMainUiState.altitudeUiState
            if (altitudeUiState !is ContentUiState.AltitudeUiState.EditMode) return@launch

            // 圧力センサーの値を取得できる状態じゃないなら無視
            val pressureState = sensorRepository.pressureSensorState.value
            if (pressureState !is Pressure.Success) return@launch

            try {
                val temperature = prefRepository.getTemperature().getOrThrow()
                val newSeaLevelPressure = calcRepository.calcSeaLevelPressure(
                    pressure = pressureState.value,
                    temperature = temperature,
                    altitude = altitudeUiState.altitudeTextFieldValue.text.toFloat() // FIXME: 入力制限をしていないので、Floatの範囲外に入ると問題になる
                )
                prefRepository.setSeaLevelPressure(newSeaLevelPressure)
            } catch (e: NumberFormatException) {
                // 変換に失敗したら、特に何もない
            } finally {
                // 最終的に編集モードを終了する
                changeModeToViewer()
            }
        }
    }

    private fun showAltitudeUndoSnackbar() {
        viewModelScope.launch {
            _showUndoSnackBarEvent.emit(ShowUndoSnackBarEvent.Altitude)
        }
    }

    fun onDismissedSnackBar(event: ShowUndoSnackBarEvent) {
        when (event) {
            ShowUndoSnackBarEvent.Temperature -> onDismissedTemperatureSnackBar()
            ShowUndoSnackBarEvent.Altitude -> onDismissedAltitudeSnackbar()
        }
    }

    fun onActionPerformedSnackBar(event: ShowUndoSnackBarEvent) {
        when (event) {
            ShowUndoSnackBarEvent.Temperature -> undoTemperature()
            ShowUndoSnackBarEvent.Altitude -> undoAltitude()
        }
    }

    private fun undoTemperature() {
        logUserActionEvent(UserActionEvent.UndoTemperature)
        viewModelScope.launch {
            prefRepository.undoTemperature()
        }
    }

    private fun undoAltitude() {
        logUserActionEvent(UserActionEvent.UndoAltitude)
        viewModelScope.launch {
            prefRepository.undoSeaLevelPressure()
        }
    }

    private fun onDismissedAltitudeSnackbar() {
        logUserActionEvent(UserActionEvent.DismissedUndoAltitude)
    }

    private fun onDismissedTemperatureSnackBar() {
        logUserActionEvent(UserActionEvent.DismissedUndoTemperature)
    }

    private fun logUserActionEvent(userActionEvent: UserActionEvent) {
        firebaseAnalytics.logUserActionEvent(userActionEvent)
    }

    private fun Float?.formattedString(
        fractionDigits: Int,
        usesGroupingSeparator: Boolean = false,
    ): String {
        // nullの場合は空文字
        if (this == null) return ""
        var format = "%.${fractionDigits}f"
        if (usesGroupingSeparator) {
            format = "%,.${fractionDigits}f"
        }
        return format.format(this)
    }
}
