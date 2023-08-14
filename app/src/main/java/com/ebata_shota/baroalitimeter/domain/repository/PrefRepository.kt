package com.ebata_shota.baroalitimeter.domain.repository

import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import kotlinx.coroutines.flow.Flow

interface PrefRepository {
    val preferencesFlow: Flow<PreferencesModel>
    suspend fun setThemeMode(value: ThemeMode)
    suspend fun setSeaLevelPressure(value: Float)
    suspend fun getTemperature(): Result<Float>
    suspend fun setTemperature(value: Float)
    suspend fun setUseTemperatureSensor(value: Boolean)
    suspend fun undoSeaLevelPressure()
    suspend fun undoTemperature()
}