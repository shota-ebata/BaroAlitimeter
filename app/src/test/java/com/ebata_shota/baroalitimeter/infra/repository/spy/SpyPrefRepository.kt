package com.ebata_shota.baroalitimeter.infra.repository.spy

import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

class SpyPrefRepository: PrefRepository {
    private val _preferencesFlow = MutableSharedFlow<PreferencesModel>()
    override val preferencesFlow: Flow<PreferencesModel> = _preferencesFlow.asSharedFlow()
    suspend fun emitPreferencesFlow(value: PreferencesModel) {
        _preferencesFlow.emit(value)
    }

    override suspend fun setThemeMode(value: ThemeMode) {
        _preferencesFlow.emit(
            preferencesFlow.first().copy(themeMode = value)
        )
    }

    override suspend fun getSeaLevelPressure(): Result<Float> = runCatching {
        preferencesFlow.first().seaLevelPressure
    }

    override suspend fun setSeaLevelPressure(value: Float) {
        _preferencesFlow.emit(
            preferencesFlow.first().copy(seaLevelPressure = value)
        )
    }

    override suspend fun getTemperature(): Result<Float> = runCatching {
        preferencesFlow.first().temperature
    }

    override suspend fun setTemperature(value: Float) {
        _preferencesFlow.emit(
            preferencesFlow.first().copy(temperature = value)
        )
    }

    override suspend fun setUseTemperatureSensor(value: Boolean) {
        _preferencesFlow.emit(
            preferencesFlow.first().copy(useTemperatureSensor = value)
        )
    }

    override suspend fun undoSeaLevelPressure() {
        TODO("Not yet implemented")
    }

    override suspend fun undoTemperature() {
        TODO("Not yet implemented")
    }
}