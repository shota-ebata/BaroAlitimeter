package com.ebata_shota.baroalitimeter.infra.repository

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.infra.AppPreferencesKeys
import com.ebata_shota.baroalitimeter.infra.extension.prefFlow
import com.ebata_shota.baroalitimeter.infra.extension.setPrefValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PrefRepositoryImpl
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
    sensorManager: SensorManager,
) : PrefRepository {

    // テーマモード
    private val themeFlow: Flow<String> by dataStore.prefFlow(
        key = AppPreferencesKeys.THEME_MODE,
        defaultValue = ThemeMode.SYSTEM.name
    )

    // 海面気圧 デフォルトは1013.25f
    private val seaLevelPressureFlow: Flow<Float> by dataStore.prefFlow(
        key = AppPreferencesKeys.SEA_LEVEL_PRESSURE,
        defaultValue = SensorManager.PRESSURE_STANDARD_ATMOSPHERE
    )

    // 気温
    private val temperatureFlow: Flow<Float> by dataStore.prefFlow(
        key = AppPreferencesKeys.TEMPERATURE,
        defaultValue = 15.0F
    )

    // 一つ前の海面気圧
    private val oldSeaLevelPressureFlow: Flow<Float?> by dataStore.prefFlow(
        key = AppPreferencesKeys.OLD_SEA_LEVEL_PRESSURE,
        defaultValue = null
    )

    // 一つ前の気温
    private val oldTemperatureFlow: Flow<Float?> by dataStore.prefFlow(
        key = AppPreferencesKeys.OLD_TEMPERATURE,
        defaultValue = null
    )

    // 気温センサーを使うか
    private val useTemperatureSensorFlow: Flow<Boolean> by dataStore.prefFlow(
        key = AppPreferencesKeys.USE_TEMPERATURE_SENSOR,
        defaultValue = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null
    )

    override suspend fun setThemeMode(value: ThemeMode) {
        dataStore.setPrefValue(AppPreferencesKeys.THEME_MODE, value.name)
    }

    override suspend fun getSeaLevelPressure(): Float {
        return seaLevelPressureFlow.first()
    }

    override suspend fun setSeaLevelPressure(value: Float) {
        val seaLevelPressure = getSeaLevelPressure()
        dataStore.setPrefValue(AppPreferencesKeys.OLD_SEA_LEVEL_PRESSURE, seaLevelPressure)
        dataStore.setPrefValue(AppPreferencesKeys.SEA_LEVEL_PRESSURE, value)
    }

    override suspend fun getTemperature(): Float {
        return temperatureFlow.first()
    }

    override suspend fun setTemperature(value: Float) {
        val temperature = getTemperature()
        dataStore.setPrefValue(AppPreferencesKeys.OLD_TEMPERATURE, temperature)
        dataStore.setPrefValue(AppPreferencesKeys.TEMPERATURE, value)
    }

    override suspend fun undoSeaLevelPressure() {
        getOldSeaLevelPressure().getOrNull()?.let { oldSeaLevelPressure ->
            setSeaLevelPressure(oldSeaLevelPressure)
        }
    }

    override suspend fun undoTemperature() {
        getOldTemperature().getOrNull()?.let { oldTemperature ->
            setTemperature(oldTemperature)
        }
    }

    override suspend fun setUseTemperatureSensor(value: Boolean) {
        dataStore.setPrefValue(AppPreferencesKeys.USE_TEMPERATURE_SENSOR, value)
    }

    override val preferencesFlow: Flow<PreferencesModel> = combine(
        themeFlow,
        seaLevelPressureFlow,
        temperatureFlow,
        useTemperatureSensorFlow
    ) { theme, seaLevelPressure, temperature, useTemperatureSensor ->
        PreferencesModel(
            themeMode = ThemeMode.values().find { it.valueName == theme } ?: throw IllegalStateException("PrefのThemeMode がありえない値になっている"),
            seaLevelPressure = seaLevelPressure,
            temperature = temperature,
            useTemperatureSensor = useTemperatureSensor
        )
    }

    private suspend fun getOldTemperature(): Result<Float?> {
        return Result.runCatching {
            oldTemperatureFlow.first()
        }
    }

    private suspend fun getOldSeaLevelPressure(): Result<Float?> {
        return Result.runCatching {
            oldSeaLevelPressureFlow.first()
        }
    }
}
