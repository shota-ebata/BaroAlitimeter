package com.ebata_shota.baroalitimeter.domain.repository

import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import kotlinx.coroutines.flow.Flow

interface PrefRepository {
    val preferencesFlow: Flow<PreferencesModel>
    suspend fun setDarkTheme(value: Boolean?)
    suspend fun setSeaLevelPressure(value: Float)
    suspend fun getTemperature(): Result<Float>
    suspend fun setTemperature(value: Float)
    suspend fun setUseTemperatureSensor(value: Boolean)
    suspend fun undoSeaLevelPressure()
    suspend fun undoTemperature()
}