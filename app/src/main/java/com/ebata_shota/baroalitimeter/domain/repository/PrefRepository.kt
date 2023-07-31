package com.ebata_shota.baroalitimeter.domain.repository

import kotlinx.coroutines.flow.Flow

interface PrefRepository {
    val seaLevelPressureFlow: Flow<Float>
    suspend fun setSeaLevelPressure(newValue: Float)

    val temperatureFlow: Flow<Float>
    suspend fun getTemperature(): Result<Float>
    suspend fun setTemperature(newValue: Float)
}