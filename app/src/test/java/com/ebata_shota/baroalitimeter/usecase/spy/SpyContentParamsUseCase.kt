package com.ebata_shota.baroalitimeter.usecase.spy

import com.ebata_shota.baroalitimeter.domain.model.ContentParams
import com.ebata_shota.baroalitimeter.domain.usecase.ContentParamsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SpyContentParamsUseCase : ContentParamsUseCase {
    private val _contentParamsFlow = MutableSharedFlow<ContentParams>()
    override val contentParamsFlow: Flow<ContentParams> = _contentParamsFlow.asSharedFlow()
    suspend fun emitContentParamsFlow(contentParams: ContentParams) {
        _contentParamsFlow.emit(contentParams)
    }

    override suspend fun getTemperature(): Result<Float> = runCatching { 0.0f }

    override suspend fun setTemperature(value: Float) = Unit

    override suspend fun getAltitude(): Result<Float> = runCatching { 0.0f }

    override suspend fun setAltitude(altitude: Float) = Unit

    override suspend fun undoSeaLevelPressure() = Unit

    override suspend fun undoTemperature() = Unit
}