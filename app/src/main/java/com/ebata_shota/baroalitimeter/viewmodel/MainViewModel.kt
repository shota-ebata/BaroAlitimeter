package com.ebata_shota.baroalitimeter.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebata_shota.baroalitimeter.domain.extensions.collect
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
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
) : ViewModel() {

    sealed interface UiState {

        object Loading : UiState

        data class ViewerMode(
            val pressureText: String,
            val seaLevelPressureText: String,
            val temperatureText: String,
            val altitudeText: String,
        ) : UiState

        data class EditTemperatureMode(
            val pressureText: String,
            val seaLevelPressureText: String,
            val temperatureTextFieldValue: TextFieldValue,
            val altitudeText: String,
        ) : UiState

        data class EditAltitudeMode(
            val pressureText: String,
            val seaLevelPressureText: String,
            val temperatureText: String,
            val altitudeTextFieldValue: TextFieldValue,
        ) : UiState
    }

    enum class Mode {
        Viewer,
        EditTemperature,
        EditAltitude,
    }

    private val _modeState: MutableStateFlow<Mode> = MutableStateFlow(Mode.Viewer)
    private val modeState: StateFlow<Mode> = _modeState.asStateFlow()

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

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()


    init {

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
        }.collect(viewModelScope) {
            _uiState.update { currentUiState ->
                currentUiState.nextUiState(it)
            }
        }

//            .distinctUntilChanged { old, new ->
//                // 重複を無視する
//                old == new
//            }.collect(viewModelScope, _uiState)
    }


    private suspend fun UiState.nextUiState(sensorAndPrefModel: SensorAndPrefModel): UiState {
        val pressureSensorState: Pressure = sensorAndPrefModel.pressureSensorState
        val mode: Mode = sensorAndPrefModel.mode
        val preferencesModel: PreferencesModel = sensorAndPrefModel.preferencesModel
        return when (val currentUiState: UiState = this) {

            UiState.Loading -> when (pressureSensorState) {
                is Pressure.Loading -> UiState.Loading
                is Pressure.Success -> createUiState(mode, pressureSensorState, preferencesModel)
            }

            is UiState.ViewerMode -> when (pressureSensorState) {
                is Pressure.Loading -> UiState.Loading
                is Pressure.Success -> when (mode) {
                    Mode.Viewer -> {
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
                        currentUiState.copy(
                            pressureText = pressureText,
                            seaLevelPressureText = seaLevelPressureText,
                            temperatureText = temperatureText,
                            altitudeText = altitudeText
                        )
                    }

                    Mode.EditTemperature -> createEditModeTemperature(
                        pressureState = pressureSensorState,
                        seaLevelPressure = preferencesModel.seaLevelPressure,
                        temperature = preferencesModel.temperature
                    )

                    Mode.EditAltitude -> createEditModeAltitude(
                        pressureState = pressureSensorState,
                        seaLevelPressure = preferencesModel.seaLevelPressure,
                        temperature = preferencesModel.temperature
                    )
                }
            }

            is UiState.EditAltitudeMode -> when (pressureSensorState) {
                is Pressure.Loading -> UiState.Loading
                is Pressure.Success -> when (mode) {
                    Mode.Viewer -> createViewMode(
                        pressureState = pressureSensorState,
                        seaLevelPressure = preferencesModel.seaLevelPressure,
                        temperature = preferencesModel.temperature
                    )

                    Mode.EditTemperature -> throw IllegalStateException("UiState.EditAltitudeMode → EditTemperature は想定外")
                    Mode.EditAltitude -> {
                        val pressure = pressureSensorState.value
                        val temperature = preferencesModel.temperature
                        val seaLevelPressure = preferencesModel.seaLevelPressure

                        val pressureText = pressure.formattedString(1)
                        val seaLevelPressureText = seaLevelPressure.formattedString(2)
                        val temperatureText = temperature.formattedString(0)
                        currentUiState.copy(
                            pressureText = pressureText,
                            seaLevelPressureText = seaLevelPressureText,
                            temperatureText = temperatureText
                        )
                    }
                }
            }

            is UiState.EditTemperatureMode -> when (pressureSensorState) {
                is Pressure.Loading -> UiState.Loading
                is Pressure.Success -> when (mode) {
                    Mode.Viewer -> createViewMode(
                        pressureState = pressureSensorState,
                        seaLevelPressure = preferencesModel.seaLevelPressure,
                        temperature = preferencesModel.temperature
                    )

                    Mode.EditTemperature -> {
                        val pressure = pressureSensorState.value
                        val temperature = preferencesModel.temperature
                        val seaLevelPressure = preferencesModel.seaLevelPressure

                        val pressureText = pressure.formattedString(1)
                        val seaLevelPressureText = seaLevelPressure.formattedString(2)
                        val altitudeText = calcRepository.calcAltitude(
                            pressure = pressure,
                            temperature = temperature,
                            seaLevelPressure = seaLevelPressure
                        ).formattedString(0)
                        currentUiState.copy(
                            pressureText = pressureText,
                            seaLevelPressureText = seaLevelPressureText,
                            altitudeText = altitudeText
                        )
                    }

                    Mode.EditAltitude -> throw IllegalStateException("UiState.EditTemperature → EditAltitudeMode は想定外")
                }
            }
        }
    }

    private suspend fun createUiState(mode: Mode, pressureSensorState: Pressure.Success, preferencesModel: PreferencesModel) = when (mode) {
        Mode.Viewer -> createViewMode(pressureSensorState, preferencesModel.seaLevelPressure, preferencesModel.temperature)
        Mode.EditTemperature -> createEditModeTemperature(pressureSensorState, preferencesModel.seaLevelPressure, preferencesModel.temperature)
        Mode.EditAltitude -> createEditModeAltitude(pressureSensorState, preferencesModel.seaLevelPressure, preferencesModel.temperature)
    }

    private suspend fun createViewMode(
        pressureState: Pressure.Success,
        seaLevelPressure: Float,
        temperature: Float,
    ): UiState.ViewerMode {
        val pressureText = pressureState.value.formattedString(1)
        val seaLevelPressureText = seaLevelPressure.formattedString(2)
        val temperatureText = temperature.formattedString(0)
        val altitudeText = calcRepository.calcAltitude(
            pressure = pressureState.value,
            temperature = temperature,
            seaLevelPressure = seaLevelPressure
        ).formattedString(0)
        return UiState.ViewerMode(
            pressureText = pressureText,
            seaLevelPressureText = seaLevelPressureText,
            temperatureText = temperatureText,
            altitudeText = altitudeText
        )
    }

    private suspend fun createEditModeTemperature(
        pressureState: Pressure.Success,
        seaLevelPressure: Float,
        temperature: Float,
    ): UiState.EditTemperatureMode {
        val pressureText = pressureState.value.formattedString(1)
        val seaLevelPressureText = seaLevelPressure.formattedString(2)
        val temperatureText = temperature.formattedString(0)
        val altitudeText = calcRepository.calcAltitude(
            pressure = pressureState.value,
            temperature = temperature,
            seaLevelPressure = seaLevelPressure
        ).formattedString(0)
        return UiState.EditTemperatureMode(
            pressureText = pressureText,
            seaLevelPressureText = seaLevelPressureText,
            temperatureTextFieldValue = TextFieldValue(
                text = temperatureText,
                selection = TextRange(temperatureText.length)
            ),
            altitudeText = altitudeText,
        )
    }

    private suspend fun createEditModeAltitude(
        pressureState: Pressure.Success,
        seaLevelPressure: Float,
        temperature: Float,
    ): UiState.EditAltitudeMode {
        val pressureText = pressureState.value.formattedString(1)
        val seaLevelPressureText = seaLevelPressure.formattedString(2)
        val temperatureText = temperature.formattedString(0)
        val altitudeText = calcRepository.calcAltitude(
            pressure = pressureState.value,
            temperature = temperature,
            seaLevelPressure = seaLevelPressure
        ).formattedString(0)
        return UiState.EditAltitudeMode(
            pressureText = pressureText,
            seaLevelPressureText = seaLevelPressureText,
            temperatureText = temperatureText,
            altitudeTextFieldValue = TextFieldValue(
                text = altitudeText,
                selection = TextRange(altitudeText.length)
            )
        )
    }

    fun changeModeToEditTemperature() {
        val state = uiState.value
        if (state is UiState.ViewerMode) {
            logUserActionEvent(UserActionEvent.EditTemperature)
            _modeState.update { Mode.EditTemperature }
        }
    }

    fun changeModeToEditAltitude() {
        val state = uiState.value
        if (state is UiState.ViewerMode) {
            logUserActionEvent(UserActionEvent.EditAltitude)
            _modeState.value = Mode.EditAltitude
        }
    }

    fun updateTemperatureTextFieldValue(textFieldValue: TextFieldValue) {
        _uiState.update {
            if (it is UiState.EditTemperatureMode) {
                it.copy(temperatureTextFieldValue = textFieldValue)
            } else it
        }
    }

    fun updateAltitudeTextFieldValue(textFieldValue: TextFieldValue) {
        _uiState.update {
            if (it is UiState.EditAltitudeMode) {
                it.copy(altitudeTextFieldValue = textFieldValue)
            } else it
        }
    }

    fun cancelEditTemperature() {
        logUserActionEvent(UserActionEvent.CancelEditTemperatureByButton)
        changeModeToViewer()
    }

    fun cancelEditAltitude() {
        logUserActionEvent(UserActionEvent.CancelEditAltitudeByButton)
        changeModeToViewer()
    }

    fun onBackPressedCallback() {
        when (uiState.value) {
            is UiState.EditAltitudeMode -> logUserActionEvent(UserActionEvent.CancelEditAltitudeByOnBackPressedCallback)
            is UiState.EditTemperatureMode -> logUserActionEvent(UserActionEvent.CancelEditTemperatureByOnBackPressedCallback)
            else -> Unit
        }
        changeModeToViewer()
    }

    private fun changeModeToViewer() {
        _modeState.value = Mode.Viewer
    }

    fun onCompletedEditTemperature() {
        logUserActionEvent(UserActionEvent.DoneEditTemperature)
        val state = uiState.value
        val pressureState = sensorRepository.pressureSensorState.value
        if (
            state is UiState.EditTemperatureMode
            && pressureState is Pressure.Success
        ) {
            viewModelScope.launch {
                try {
                    val temperature = state.temperatureTextFieldValue.text.toFloat() // FIXME: 入力制限をしていないので、Floatの範囲外に入ると問題になる
                    prefRepository.setTemperature(temperature)
                } catch (e: NumberFormatException) {
                    // 変換に失敗したら、特に何もない
                } finally {
                    // 最終的に編集モードを終了する
                    changeModeToViewer()
                }
            }
        }
    }

    fun onCompletedEditAltitude() {
        logUserActionEvent(UserActionEvent.DoneEditAltitude)
        val state = uiState.value
        val pressureState = sensorRepository.pressureSensorState.value
        if (
            state is UiState.EditAltitudeMode
            && pressureState is Pressure.Success
        ) {
            viewModelScope.launch {
                try {
                    val temperature = prefRepository.getTemperature().getOrThrow()
                    val newSeaLevelPressure = calcRepository.calcSeaLevelPressure(
                        pressure = pressureState.value,
                        temperature = temperature,
                        altitude = state.altitudeTextFieldValue.text.toFloat() // FIXME: 入力制限をしていないので、Floatの範囲外に入ると問題になる
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
    }

    fun undoTemperature() {
        logUserActionEvent(UserActionEvent.UndoTemperature)
        viewModelScope.launch {
            prefRepository.undoTemperature()
        }
    }

    fun undoAltitude() {
        logUserActionEvent(UserActionEvent.UndoAltitude)
        viewModelScope.launch {
            prefRepository.undoSeaLevelPressure()
        }
    }

    fun onDismissedAltitudeSnackbar() {
        logUserActionEvent(UserActionEvent.DismissedUndoAltitude)
    }

    fun onDismissedTemperatureSnackBar() {
        logUserActionEvent(UserActionEvent.DismissedUndoTemperature)
    }

    private fun logUserActionEvent(userActionEvent: UserActionEvent) {
        firebaseAnalytics.logUserActionEvent(userActionEvent)
    }

    private fun Float?.formattedString(fractionDigits: Int, usesGroupingSeparator: Boolean = false): String {
        // nullの場合は空文字
        if (this == null) return ""
        var format = "%.${fractionDigits}f"
        if (usesGroupingSeparator) {
            format = "%,.${fractionDigits}f"
        }
        return format.format(this)
    }

    companion object {


    }
}