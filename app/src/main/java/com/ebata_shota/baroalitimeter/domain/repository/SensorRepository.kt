package com.ebata_shota.baroalitimeter.domain.repository

import com.ebata_shota.baroalitimeter.domain.model.Temperature
import kotlinx.coroutines.flow.Flow

interface SensorRepository {
    val pressureFlow: Flow<Float>
    suspend fun getPressure(): Float
    val temperatureSensorState: Flow<Temperature>
}
