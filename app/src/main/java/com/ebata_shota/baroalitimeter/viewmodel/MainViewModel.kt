package com.ebata_shota.baroalitimeter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebata_shota.baroalitimeter.domain.model.Pressure
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
            val altitudeText: String,
            val temperatureText: String,
        ) : UiState()

        data class EditAltitudeMode(
            val pressureText: String,
            val defaultAltitudeText: String,
            val temperatureText: String,
        ) : UiState()

        data class EditTemperatureMode(
            val pressureText: String,
            val altitudeText: String,
            val defaultTemperatureText: String,
        ) : UiState()
    }

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sensorRepository.pressureState,
                sensorRepository.temperatureState
            ) { pressureState, temperatureState -> // TODO: temperatureStateをうまく使う
                when (pressureState) {
                    is Pressure.Loading -> UiState.Loading
                    is Pressure.Success -> when (uiState.value) {
                        is UiState.Loading -> createViewMode(pressureState)
                        is UiState.ViewerMode -> createViewMode(pressureState)
                        is UiState.EditAltitudeMode -> createEditModeAltitude(pressureState)
                        is UiState.EditTemperatureMode -> createEditModeTemperature(pressureState)
                    }
                }
            }.distinctUntilChanged { old, new ->
                // 重複を無視する
                old == new
            }.collect(_uiState)
        }
    }

    private suspend fun createViewMode(pressure: Pressure.Success) = UiState.ViewerMode(
        pressureText = pressure.value.formattedString(1),
        altitudeText = calcRepository.calcAltitude(
            pressure = pressure.value,
            seaLevelPressure = prefRepository.seaLevelPressure,
            temperature = prefRepository.temperature
        ).formattedString(0),
        temperatureText = prefRepository.temperature.formattedString(1)
    )

    private suspend fun createEditModeAltitude(pressure: Pressure.Success) = UiState.EditAltitudeMode(
        pressureText = pressure.value.formattedString(1),
        defaultAltitudeText = calcRepository.calcAltitude(
            pressure = pressure.value,
            seaLevelPressure = prefRepository.seaLevelPressure,
            temperature = prefRepository.temperature
        ).formattedString(0),
        temperatureText = prefRepository.temperature.formattedString(1)
    )

    private suspend fun createEditModeTemperature(pressureState: Pressure.Success) = UiState.EditTemperatureMode(
        pressureText = pressureState.value.formattedString(1),
        altitudeText = calcRepository.calcAltitude(
            pressure = pressureState.value,
            seaLevelPressure = prefRepository.seaLevelPressure,
            temperature = prefRepository.temperature
        ).formattedString(0),
        defaultTemperatureText = prefRepository.temperature.formattedString(1)
    )

    // FIXME: _uiState.valueをここで書き換えると
    //  タイミングによってはセンサー経由のイベントによってuiStateが上書きされる可能性があるのでよくない
    //  つまり_uiState.valueを書き換えるのはinitで定義している一箇所にするべきで
    //  ベースになる値を書き換えるようにしたほうがよい
    fun changeToEditModeTemperature() {
        val state = uiState.value
        if (state is UiState.ViewerMode) {
            _uiState.value = UiState.EditTemperatureMode(
                pressureText = state.pressureText,
                altitudeText = state.altitudeText,
                defaultTemperatureText = state.temperatureText
            )
        }
    }

    fun changeToEditModeAltitude() {
        val state = uiState.value
        if (state is UiState.ViewerMode) {
            _uiState.value = UiState.EditAltitudeMode(
                pressureText = state.pressureText,
                defaultAltitudeText = state.altitudeText,
                temperatureText = state.temperatureText
            )
        }
    }

    fun setTemperature(temperatureText: String) {
        val state = uiState.value
        val pressureState = sensorRepository.pressureState.value
        if (
            state is UiState.EditTemperatureMode
            && pressureState is Pressure.Success
        ) {
            val currentPressure = pressureState.value
            try {
                val temperature = temperatureText.toFloat()
                viewModelScope.launch {
                    prefRepository.temperature = temperature
                    _uiState.value = UiState.ViewerMode(
                        pressureText = currentPressure.formattedString(1),
                        altitudeText = state.altitudeText,
                        temperatureText = prefRepository.temperature.toString()
                    )
                }
            } catch (e: NumberFormatException) {
                _uiState.value = UiState.ViewerMode(
                    pressureText = currentPressure.formattedString(1),
                    altitudeText = state.altitudeText,
                    temperatureText = prefRepository.temperature.toString()
                )
            }
        }
    }

    fun setAltitude(altitudeText: String) {
        val state = uiState.value
        val pressureState = sensorRepository.pressureState.value
        if (
            state is UiState.EditAltitudeMode
            && pressureState is Pressure.Success
        ) {
            val currentPressure = pressureState.value
            try {
                val altitude = altitudeText.toFloat()
                viewModelScope.launch {
                    prefRepository.seaLevelPressure = calcRepository.calcSeaLevelPressure(
                        pressure = currentPressure,
                        temperature = prefRepository.temperature,
                        altitude = altitude
                    )
                    _uiState.value = UiState.ViewerMode(
                        pressureText = currentPressure.formattedString(1),
                        altitudeText = altitudeText,
                        temperatureText = prefRepository.temperature.toString()
                    )
                }
            } catch (e: NumberFormatException) {
                _uiState.value = UiState.ViewerMode(
                    pressureText = currentPressure.formattedString(1),
                    altitudeText = altitudeText,
                    temperatureText = prefRepository.temperature.toString()
                )
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