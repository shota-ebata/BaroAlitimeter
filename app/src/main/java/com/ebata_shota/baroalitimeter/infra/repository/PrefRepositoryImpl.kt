package com.ebata_shota.baroalitimeter.infra.repository

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.infra.AppPreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import kotlin.properties.ReadOnlyProperty

class PrefRepositoryImpl
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
    sensorManager: SensorManager,
) : PrefRepository {

    // テーマモード
    private val themeFlow: Flow<String> by prefFlow(AppPreferencesKeys.THEME_MODE, ThemeMode.SYSTEM.name)

    // 海面気圧 デフォルトは1013.25f
    private val seaLevelPressureFlow: Flow<Float> by prefFlow(AppPreferencesKeys.SEA_LEVEL_PRESSURE, SensorManager.PRESSURE_STANDARD_ATMOSPHERE)

    // 気温
    private val temperatureFlow: Flow<Float> by prefFlow(AppPreferencesKeys.TEMPERATURE, 15.0F)

    // 一つ前の海面気圧
    private val oldSeaLevelPressureFlow: Flow<Float?> by prefFlow(AppPreferencesKeys.OLD_SEA_LEVEL_PRESSURE, null)

    // 一つ前の気温
    private val oldTemperatureFlow: Flow<Float?> by prefFlow(AppPreferencesKeys.OLD_TEMPERATURE, null)

    // 気温センサーを使うか
    private val useTemperatureSensorFlow: Flow<Boolean> by prefFlow(AppPreferencesKeys.USE_TEMPERATURE_SENSOR, sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null)

    override suspend fun setThemeMode(value: ThemeMode) {
        setPrefValue(AppPreferencesKeys.THEME_MODE, value.name)
    }

    override suspend fun getSeaLevelPressure(): Float {
        return seaLevelPressureFlow.first()
    }

    override suspend fun setSeaLevelPressure(value: Float) {
        val seaLevelPressure = getSeaLevelPressure()
        setPrefValue(AppPreferencesKeys.OLD_SEA_LEVEL_PRESSURE, seaLevelPressure)
        setPrefValue(AppPreferencesKeys.SEA_LEVEL_PRESSURE, value)
    }

    override suspend fun getTemperature(): Float {
        return temperatureFlow.first()
    }

    override suspend fun setTemperature(value: Float) {
        val temperature = getTemperature()
        setPrefValue(AppPreferencesKeys.OLD_TEMPERATURE, temperature)
        setPrefValue(AppPreferencesKeys.TEMPERATURE, value)
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
        setPrefValue(AppPreferencesKeys.USE_TEMPERATURE_SENSOR, value)
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

    // ---------------------------------------------------------------------------------------------

    @Suppress("UNCHECKED_CAST")
    private fun <T> prefFlow(key: Preferences.Key<T>, defaultValue: T?) = ReadOnlyProperty<PrefRepositoryImpl, Flow<T>> { thisRef, _ ->
        thisRef.dataStore
            .data
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }.map { preferences ->
                preferences[key] ?: defaultValue as T
            }
    }

    private suspend fun <T> setPrefValue(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    private suspend fun <T> removePref(key: Preferences.Key<T>) {
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}
