package com.ebata_shota.baroalitimeter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.pow


@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val sensorRepository: SensorRepository,
    private val prefRepository: PrefRepository,
) : ViewModel() {

    sealed class UiState {
        data class ViewerMode(
            val pressureText: String? = null,
            val altitudeText: String? = null,
            val temperatureText: String? = null,
        ) : UiState()

        data class EditModeAltitude(
            val pressureText: String? = null,
            val defaultAltitudeText: String? = null,
            val temperatureText: String? = null,
        ) : UiState()

        data class EditModeTemperature(
            val pressureText: String? = null,
            val altitudeText: String? = null,
            val defaultTemperatureText: String? = null,
        ) : UiState()
    }


    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.ViewerMode())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sensorRepository.pressureState.map {
                when (val state = uiState.value) {
                    is UiState.ViewerMode -> state.copy(
                        pressureText = it.pressure?.formattedString(1),
                        altitudeText = it.pressure?.let { pressure ->
                            calcAltitude(
                                pressure = pressure,
                                seaLevelPressure = prefRepository.seaLevelPressure,
                                temperature = prefRepository.temperature
                            ).formattedString(0)
                        },
                        temperatureText = prefRepository.temperature.formattedString(1)
                    )

                    is UiState.EditModeAltitude -> state.copy(
                        pressureText = it.pressure?.formattedString(1),
                        defaultAltitudeText = it.pressure?.let { pressure ->
                            calcAltitude(
                                pressure = pressure,
                                seaLevelPressure = prefRepository.seaLevelPressure,
                                temperature = prefRepository.temperature
                            ).formattedString(0, false)
                        },
                        temperatureText = prefRepository.temperature.formattedString(1)
                    )

                    is UiState.EditModeTemperature -> state.copy(
                        pressureText = it.pressure?.formattedString(1),
                        altitudeText = it.pressure?.let { pressure ->
                            calcAltitude(
                                pressure = pressure,
                                seaLevelPressure = prefRepository.seaLevelPressure,
                                temperature = prefRepository.temperature
                            ).formattedString(0, false)
                        },
                        defaultTemperatureText = prefRepository.temperature.formattedString(1)
                    )
                }
            }.collect(_uiState)
        }
    }

    fun changeToEditModeTemperature() {
        val state = uiState.value
        if (state is UiState.ViewerMode) {
            _uiState.value = UiState.EditModeTemperature(
                pressureText = state.pressureText,
                altitudeText = state.altitudeText,
                defaultTemperatureText = state.temperatureText
            )
        }
    }

    fun changeToEditModeAltitude() {
        val state = uiState.value
        if (state is UiState.ViewerMode) {
            _uiState.value = UiState.EditModeAltitude(
                pressureText = state.pressureText,
                defaultAltitudeText = state.altitudeText,
                temperatureText = state.temperatureText
            )
        }
    }

    fun setTemperature(temperatureText: String) {
        val state = uiState.value
        if (state is UiState.EditModeTemperature) {
            val temperature = temperatureText.toFloat() // FIXME: TextFiledの貼り付け対策していないので落ちるかも
            viewModelScope.launch {
                prefRepository.temperature = temperature
            }
            val currentPressure = sensorRepository.pressureState.value.pressure
            _uiState.value = UiState.ViewerMode(
                pressureText = currentPressure.formattedString(1),
                altitudeText = state.altitudeText,
                temperatureText = prefRepository.temperature.toString()
            )
        }
    }

    fun setAltitude(altitudeText: String) {
        val state = uiState.value
        if (state is UiState.EditModeAltitude) {
            val altitude = altitudeText.toFloat() // FIXME: TextFiledの貼り付け対策していないので落ちるかも
            val currentPressure = sensorRepository.pressureState.value.pressure
            currentPressure?.let { pressure ->
                viewModelScope.launch {
                    prefRepository.seaLevelPressure = calcSeaLevelPressure(pressure, prefRepository.temperature, altitude)
                }
            }
            _uiState.value = UiState.ViewerMode(
                pressureText = currentPressure.formattedString(1),
                altitudeText = altitudeText,
                temperatureText = prefRepository.temperature.toString()
            )
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

    private suspend fun calcAltitude(pressure: Float, seaLevelPressure: Float, temperature: Float): Float {
        return withContext(Dispatchers.Default) {
            ((((seaLevelPressure.toDouble() / pressure.toDouble()).pow(1 / 5.257) - 1) * (temperature + 273.15)) / 0.0065).toFloat()
        }
    }

    private suspend fun calcSeaLevelPressure(pressure: Float, temperature: Float, altitude: Float): Float {
        return withContext(Dispatchers.Default) {
            val d = 0.0065 * altitude
            (pressure * (1 - (d / (temperature + d + 273.15))).pow(-5.257)).toFloat()
        }
    }

    companion object {


    }
}