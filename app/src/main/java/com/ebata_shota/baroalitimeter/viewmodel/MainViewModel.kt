package com.ebata_shota.baroalitimeter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebata_shota.baroalitimeter.domain.extensions.collect
import com.ebata_shota.baroalitimeter.domain.extensions.logUserActionEvent
import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.model.content.UserActionEvent
import com.ebata_shota.baroalitimeter.domain.repository.CalcRepository
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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

    sealed class UiState {

        object Loading : UiState()

        data class ViewerMode(
            val pressureText: String,
            val seaLevelPressureText: String,
            val temperatureText: String,
            val altitudeText: String,
        ) : UiState()

        data class EditTemperatureMode(
            val pressureText: String,
            val seaLevelPressureText: String,
            val defaultTemperatureText: String,
            val altitudeText: String,
        ) : UiState()

        data class EditAltitudeMode(
            val pressureText: String,
            val seaLevelPressureText: String,
            val temperatureText: String,
            val defaultAltitudeText: String,
        ) : UiState()
    }

    enum class Mode {
        Viewer,
        EditTemperature,
        EditAltitude,
    }

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _modeState: MutableStateFlow<Mode> = MutableStateFlow(Mode.Viewer)
    private val modeState: StateFlow<Mode> = _modeState.asStateFlow()

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
            when (pressureSensorState) {
                is Pressure.Loading -> UiState.Loading
                is Pressure.Success -> when (mode) {
                    Mode.Viewer -> createViewMode(pressureSensorState, preferencesModel.seaLevelPressure, preferencesModel.temperature)
                    Mode.EditTemperature -> createEditModeTemperature(pressureSensorState, preferencesModel.seaLevelPressure, preferencesModel.temperature)
                    Mode.EditAltitude -> createEditModeAltitude(pressureSensorState, preferencesModel.seaLevelPressure, preferencesModel.temperature)
                }
            }
        }.distinctUntilChanged { old, new ->
            // 重複を無視する
            old == new
        }.collect(viewModelScope, _uiState)
    }

    private suspend fun createViewMode(pressure: Pressure.Success, seaLevelPressure: Float, temperature: Float) = UiState.ViewerMode(
        pressureText = pressure.value.formattedString(1),
        seaLevelPressureText = seaLevelPressure.formattedString(2),
        altitudeText = calcRepository.calcAltitude(
            pressure = pressure.value,
            temperature = temperature,
            seaLevelPressure = seaLevelPressure
        ).formattedString(0),
        temperatureText = temperature.formattedString(0),
    )

    private suspend fun createEditModeTemperature(pressureState: Pressure.Success, seaLevelPressure: Float, temperature: Float) = UiState.EditTemperatureMode(
        pressureText = pressureState.value.formattedString(1),
        seaLevelPressureText = seaLevelPressure.formattedString(2),
        altitudeText = calcRepository.calcAltitude(
            pressure = pressureState.value,
            temperature = temperature,
            seaLevelPressure = seaLevelPressure
        ).formattedString(0),
        defaultTemperatureText = temperature.formattedString(0),
    )

    private suspend fun createEditModeAltitude(pressure: Pressure.Success, seaLevelPressure: Float, temperature: Float) = UiState.EditAltitudeMode(
        pressureText = pressure.value.formattedString(1),
        seaLevelPressureText = seaLevelPressure.formattedString(2),
        defaultAltitudeText = calcRepository.calcAltitude(
            pressure = pressure.value,
            temperature = temperature,
            seaLevelPressure = seaLevelPressure
        ).formattedString(0),
        temperatureText = temperature.formattedString(0),
    )

    fun changeModeToEditTemperature() {
        if (uiState.value is UiState.ViewerMode) {
            logUserActionEvent(UserActionEvent.EditTemperature)
            _modeState.value = Mode.EditTemperature
        }
    }

    fun changeModeToEditAltitude() {
        if (uiState.value is UiState.ViewerMode) {
            logUserActionEvent(UserActionEvent.EditAltitude)
            _modeState.value = Mode.EditAltitude
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

    fun setTemperature(temperatureText: String) {
        logUserActionEvent(UserActionEvent.DoneEditTemperature)
        val state = uiState.value
        val pressureState = sensorRepository.pressureSensorState.value
        if (
            state is UiState.EditTemperatureMode
            && pressureState is Pressure.Success
        ) {
            viewModelScope.launch {
                try {
                    val temperature = temperatureText.toFloat() // FIXME: 入力制限をしていないので、Floatの範囲外に入ると問題になる
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
    fun setAltitude(altitudeText: String) {
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
                        altitude = altitudeText.toFloat() // FIXME: 入力制限をしていないので、Floatの範囲外に入ると問題になる
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