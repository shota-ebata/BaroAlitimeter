package com.ebata_shota.baroalitimeter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.repository.CalcRepository
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
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
) : ViewModel() {

    sealed class UiState {

        object Loading : UiState()

        data class ViewerMode(
            val pressureText: String,
            val temperatureText: String,
            val altitudeText: String,
        ) : UiState()

        data class EditTemperatureMode(
            val pressureText: String,
            val defaultTemperatureText: String,
            val altitudeText: String,
        ) : UiState()

        data class EditAltitudeMode(
            val pressureText: String,
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
        viewModelScope.launch {
            combine(
                modeState,
                sensorRepository.pressureSensorState,
                sensorRepository.temperatureSensorState,
                prefRepository.seaLevelPressureState,
                prefRepository.temperatureState,
            ) {
                    mode: Mode,
                    pressureSensorState: Pressure,
                    temperatureSensorState: Temperature, // TODO: temperatureStateをうまく使う
                    seaLevelPressure: Float,
                    temperature: Float,
                ->
                when (pressureSensorState) {
                    is Pressure.Loading -> UiState.Loading
                    is Pressure.Success -> when (mode) {
                        Mode.Viewer -> createViewMode(pressureSensorState, seaLevelPressure, temperature)
                        Mode.EditTemperature -> createEditModeTemperature(pressureSensorState, seaLevelPressure, temperature)
                        Mode.EditAltitude -> createEditModeAltitude(pressureSensorState, seaLevelPressure, temperature)
                    }
                }
            }.distinctUntilChanged { old, new ->
                // 重複を無視する
                old == new
            }.collect(_uiState)
        }
    }

    private suspend fun createViewMode(pressure: Pressure.Success, seaLevelPressure: Float, temperature: Float) = UiState.ViewerMode(
        pressureText = pressure.value.formattedString(1),
        altitudeText = calcRepository.calcAltitude(
            pressure = pressure.value,
            temperature = temperature,
            seaLevelPressure = seaLevelPressure
        ).formattedString(0),
        temperatureText = temperature.formattedString(1)
    )

    private suspend fun createEditModeTemperature(pressureState: Pressure.Success, seaLevelPressure: Float, temperature: Float) = UiState.EditTemperatureMode(
        pressureText = pressureState.value.formattedString(1),
        altitudeText = calcRepository.calcAltitude(
            pressure = pressureState.value,
            temperature = temperature,
            seaLevelPressure = seaLevelPressure
        ).formattedString(0),
        defaultTemperatureText = temperature.formattedString(1)
    )

    private suspend fun createEditModeAltitude(pressure: Pressure.Success, seaLevelPressure: Float, temperature: Float) = UiState.EditAltitudeMode(
        pressureText = pressure.value.formattedString(1),
        defaultAltitudeText = calcRepository.calcAltitude(
            pressure = pressure.value,
            temperature = temperature,
            seaLevelPressure = seaLevelPressure
        ).formattedString(0),
        temperatureText = temperature.formattedString(1)
    )

    fun changeModeToEditTemperature() {
        if (uiState.value is UiState.ViewerMode) {
            _modeState.value = Mode.EditTemperature
        }
    }

    fun changeModeToEditAltitude() {
        if (uiState.value is UiState.ViewerMode) {
            _modeState.value = Mode.EditAltitude
        }
    }

    fun setTemperature(temperatureText: String) {
        val state = uiState.value
        val pressureState = sensorRepository.pressureSensorState.value
        if (
            state is UiState.EditTemperatureMode
            && pressureState is Pressure.Success
        ) {
            viewModelScope.launch {
                try {
                    val temperature = temperatureText.toFloat()
                    prefRepository.setTemperature(temperature)
                } catch (e: NumberFormatException) {
                    // 変換に失敗したら、特に何もない
                } finally {
                    // 最終的に編集モードを終了する
                    _modeState.value = Mode.Viewer
                }
            }
        }
    }

    fun setAltitude(altitudeText: String) {
        val state = uiState.value
        val pressureState = sensorRepository.pressureSensorState.value
        if (
            state is UiState.EditAltitudeMode
            && pressureState is Pressure.Success
        ) {
            viewModelScope.launch {
                try {
                    val altitude = altitudeText.toFloat()
                    val newSeaLevelPressure = calcRepository.calcSeaLevelPressure(
                        pressure = pressureState.value,
                        temperature = prefRepository.temperatureState.value,
                        altitude = altitude
                    )
                    prefRepository.setSeaLevelPressure(newSeaLevelPressure)
                } catch (e: NumberFormatException) {
                    // 変換に失敗したら、特に何もない
                } finally {
                    // 最終的に編集モードを終了する
                    _modeState.value = Mode.Viewer
                }
            }
        }
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