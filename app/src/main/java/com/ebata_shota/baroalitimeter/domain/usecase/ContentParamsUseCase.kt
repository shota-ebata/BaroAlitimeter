package com.ebata_shota.baroalitimeter.domain.usecase

import com.ebata_shota.baroalitimeter.domain.model.ContentParams
import kotlinx.coroutines.flow.Flow

interface ContentParamsUseCase {
    val contentParamsFlow: Flow<ContentParams>
    suspend fun getTemperature(): Float
    suspend fun setTemperature(value: Float)
    suspend fun getAltitude(): Float
    suspend fun setAltitude(altitude: Float)
    suspend fun undoSeaLevelPressure()
    suspend fun undoTemperature()
}