package com.ebata_shota.baroalitimeter.infra.repository.spy

import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class SpySensorRepository : SensorRepository {
    private val _pressureFlow = MutableSharedFlow<Float>()
    override val pressureFlow: Flow<Float> = _pressureFlow.asSharedFlow()
    suspend fun emitPressureSensorState(pressure: Float) {
        _pressureFlow.emit(pressure)
    }

    override suspend fun getPressure(): Float = 0.0f

    private val _temperatureSensorState = MutableStateFlow<Temperature>(Temperature.Loading)
    override val temperatureSensorState: StateFlow<Temperature> = _temperatureSensorState.asStateFlow()
    suspend fun emitTemperatureSensorState(temperature: Temperature) {
        _temperatureSensorState.emit(temperature)
    }
}
