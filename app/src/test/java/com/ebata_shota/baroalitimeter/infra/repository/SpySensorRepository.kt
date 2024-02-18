package com.ebata_shota.baroalitimeter.infra.repository

import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpySensorRepository : SensorRepository {
    private val _pressureSensorState = MutableStateFlow<Pressure>(Pressure.Loading)
    override val pressureSensorState: StateFlow<Pressure> = _pressureSensorState.asStateFlow()
    suspend fun emitPressureSensorState(pressure: Pressure) = _pressureSensorState.emit(pressure)

    private val _temperatureSensorState = MutableStateFlow<Temperature>(Temperature.Loading)
    override val temperatureSensorState: StateFlow<Temperature> = _temperatureSensorState.asStateFlow()
    suspend fun emitTemperatureSensorState(temperature: Temperature) = _temperatureSensorState.emit(temperature)
}
