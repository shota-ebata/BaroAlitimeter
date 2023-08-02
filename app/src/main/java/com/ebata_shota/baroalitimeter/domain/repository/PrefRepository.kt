package com.ebata_shota.baroalitimeter.domain.repository

import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import kotlinx.coroutines.flow.Flow

interface PrefRepository {
    suspend fun setSeaLevelPressure(value: Float)

    suspend fun getTemperature(): Result<Float>
    suspend fun setTemperature(value: Float)

    suspend fun setUseTemperatureSensor(value: Boolean)

    val preferencesFlow: Flow<PreferencesModel>
}