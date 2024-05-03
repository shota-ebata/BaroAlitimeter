package com.ebata_shota.baroalitimeter.infra.repository.spy

import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SpyPrefRepository: PrefRepository {
    private val _preferencesFlow = MutableSharedFlow<PreferencesModel>()
    override val preferencesFlow: Flow<PreferencesModel> = _preferencesFlow.asSharedFlow()
    suspend fun emitPreferencesFlow(value: PreferencesModel) {
        _preferencesFlow.emit(value)
    }

    override suspend fun setThemeMode(value: ThemeMode) = Unit

    override suspend fun getSeaLevelPressure(): Float = 0.0f

    override suspend fun setSeaLevelPressure(value: Float) = Unit

    override suspend fun getTemperature(): Float = 0.0f

    override suspend fun setTemperature(value: Float) = Unit

    override suspend fun setUseTemperatureSensor(value: Boolean) = Unit

    override suspend fun undoSeaLevelPressure() = Unit

    override suspend fun undoTemperature() = Unit
}