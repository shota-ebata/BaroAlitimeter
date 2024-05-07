package com.ebata_shota.baroalitimeter.viewmodel

import androidx.annotation.StringRes
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.domain.extensions.combineTransform6
import com.ebata_shota.baroalitimeter.domain.extensions.logUserActionEvent
import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.model.content.UserActionEvent
import com.ebata_shota.baroalitimeter.domain.repository.CalcRepository
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
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

        object Uninitialized : MainUiState

        data class Initialized(
            val themeMode: ThemeMode,
            val contentUiState: ContentUiState,
        ) : MainUiState
    }

    data class ContentUiState(
        val pressureText: String,
        val seaLevelPressureText: String,
        val temperatureUiState: TemperatureUiState,
        val altitudeUiState: AltitudeUiState,
    ) {

        sealed interface TemperatureUiState {

            data class ViewerMode(
                val temperatureText: String,
            ) : TemperatureUiState

            data class EditMode(
                val temperatureTextFieldValue: TextFieldValue,
            ) : TemperatureUiState
        }

        sealed interface AltitudeUiState {

            data class ViewerMode(
                val altitudeText: String,
            ) : AltitudeUiState

            data class EditMode(
                val altitudeTextFieldValue: TextFieldValue,
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

    private val _temperatureTextFieldValue = MutableStateFlow(TextFieldValue())
    private val temperatureTextFieldValue = _temperatureTextFieldValue.asStateFlow()

    private val _altitudeTextFieldValue = MutableStateFlow(TextFieldValue())
    private val altitudeTextFieldValue = _altitudeTextFieldValue.asStateFlow()

    val mainUiState: StateFlow<MainUiState> = combineTransform6(
        modeState,
        temperatureTextFieldValue,
        altitudeTextFieldValue,
        sensorRepository.pressureSensorState,
        sensorRepository.temperatureSensorState,
        prefRepository.preferencesFlow
    ) {
            mode: Mode,
            temperatureTextFieldValue: TextFieldValue,
            altitudeTextFieldValue: TextFieldValue,
            pressureSensorState: Pressure,
            temperatureSensorState: Temperature, // TODO: temperatureSensorStateをうまく使う
            preferencesModel: PreferencesModel,
        ->

        // TODO: UseCaseに切り出し
        if (pressureSensorState !is Pressure.Success) {
            emit(MainUiState.Uninitialized)
            return@combineTransform6
        }

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

        val initialized = MainUiState.Initialized(
            themeMode = preferencesModel.themeMode,
            contentUiState = ContentUiState(
                pressureText = pressureText,
                seaLevelPressureText = seaLevelPressureText,
                temperatureUiState = if (mode == Mode.EditTemperature) {
                    ContentUiState.TemperatureUiState.EditMode(
                        temperatureTextFieldValue = temperatureTextFieldValue
                    )
                } else {
                    ContentUiState.TemperatureUiState.ViewerMode(temperatureText)
                },
                altitudeUiState = if (mode == Mode.EditAltitude) {
                    ContentUiState.AltitudeUiState.EditMode(
                        altitudeTextFieldValue = altitudeTextFieldValue
                    )
                } else {
                    ContentUiState.AltitudeUiState.ViewerMode(altitudeText)
                }
            )
        )
        emit(initialized)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = MainUiState.Uninitialized
    )

    fun onSelectedThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            prefRepository.setThemeMode(themeMode)
        }
    }

    private val _showUndoSnackBarEvent = MutableSharedFlow<ShowUndoSnackBarEvent>()
    val showUndoSnackBarEvent: SharedFlow<ShowUndoSnackBarEvent> = _showUndoSnackBarEvent.asSharedFlow()

    override fun onClickTemperature() {
        changeModeToEditTemperature()
    }

    private fun changeModeToEditTemperature() {
        viewModelScope.launch {
            val temperatureText = prefRepository.getTemperature().getOrNull()?.formattedString(0) ?: run {
                return@launch
            }
            updateTemperatureTextFieldValue(
                TextFieldValue(
                    text = temperatureText,
                    selection = TextRange(temperatureText.length)
                )
            )
            _modeState.update { mode ->
                if (mode != Mode.Viewer) {
                    return@launch
                }
                logUserActionEvent(UserActionEvent.EditTemperature)
                Mode.EditTemperature
            }
        }
    }

    override fun onClickAltitude() {
        changeModeToEditAltitude()
    }

    private fun changeModeToEditAltitude() {
        viewModelScope.launch {
            val pressureSensorState = sensorRepository.getPressureSensorValue().getOrNull() ?: run {
                return@launch
            }
            val temperature = prefRepository.getTemperature().getOrNull() ?: run {
                return@launch
            }
            val seaLevelPressure = prefRepository.getSeaLevelPressure().getOrNull() ?: run {
                return@launch
            }
            val altitudeText = calcRepository.calcAltitude(
                pressure = pressureSensorState.value,
                temperature = temperature,
                seaLevelPressure = seaLevelPressure
            ).formattedString(0)
            updateAltitudeTextFieldValue(
                TextFieldValue(
                    text = altitudeText,
                    selection = TextRange(altitudeText.length)
                )
            )
            _modeState.update { mode ->
                if (mode != Mode.Viewer) {
                    return@launch
                }
                logUserActionEvent(UserActionEvent.EditAltitude)
                Mode.EditAltitude
            }
        }
    }

    override fun onChangeTemperatureTextFieldValue(textFieldValue: TextFieldValue) {
        updateTemperatureTextFieldValue(textFieldValue)
    }

    private fun updateTemperatureTextFieldValue(textFieldValue: TextFieldValue) {
        _temperatureTextFieldValue.update {
            textFieldValue
        }
    }

    override fun onChangeAltitudeTextFieldValue(textFieldValue: TextFieldValue) {
        updateAltitudeTextFieldValue(textFieldValue)
    }

    private fun updateAltitudeTextFieldValue(textFieldValue: TextFieldValue) {
        _altitudeTextFieldValue.update {
            textFieldValue
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

            // 表示できるUiStateじゃないなら無視
            val currentMainUiState = mainUiState.value
            if (currentMainUiState !is MainUiState.Initialized) {
                return@launch
            }

            // Editモードじゃないなら無視
            val temperatureUiState = currentMainUiState.contentUiState.temperatureUiState
            if (temperatureUiState !is ContentUiState.TemperatureUiState.EditMode) {
                return@launch
            }

            // 圧力センサーの値を取得できる状態じゃないなら無視
            val pressureState = sensorRepository.pressureSensorState.value
            if (pressureState !is Pressure.Success) {
                return@launch
            }

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

            // 表示できるUiStateじゃないなら無視
            val currentMainUiState = mainUiState.value
            if (currentMainUiState !is MainUiState.Initialized) {
                return@launch
            }

            // Editモードじゃないなら無視
            val altitudeUiState = currentMainUiState.contentUiState.altitudeUiState
            if (altitudeUiState !is ContentUiState.AltitudeUiState.EditMode) {
                return@launch
            }

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
