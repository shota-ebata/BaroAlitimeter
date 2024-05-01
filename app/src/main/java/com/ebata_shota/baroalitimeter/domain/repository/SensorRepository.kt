package com.ebata_shota.baroalitimeter.domain.repository

import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import kotlinx.coroutines.flow.StateFlow

interface SensorRepository {
    val pressureSensorState: StateFlow<Pressure>
    suspend fun getPressureSensor(): Result<Pressure.Success>
    val temperatureSensorState: StateFlow<Temperature>
}
