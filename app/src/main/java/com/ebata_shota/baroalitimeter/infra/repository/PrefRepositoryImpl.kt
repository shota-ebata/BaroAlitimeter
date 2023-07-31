package com.ebata_shota.baroalitimeter.infra.repository

import android.hardware.SensorManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.infra.AppPreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.properties.ReadOnlyProperty

class PrefRepositoryImpl
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
) : PrefRepository {

    // ユーザUID
    val userUid: Flow<String> by prefFlow(AppPreferencesKeys.USER_UID, UUID.randomUUID().toString())

    // 海面気圧 デフォルトは1013.25f
    override val seaLevelPressureFlow: Flow<Float> by prefFlow(AppPreferencesKeys.SEA_LEVEL_PRESSURE, SensorManager.PRESSURE_STANDARD_ATMOSPHERE)

    override suspend fun setSeaLevelPressure(newValue: Float) {
        setPrefValue(AppPreferencesKeys.SEA_LEVEL_PRESSURE, newValue)
    }

    // 気温
    override val temperatureFlow: Flow<Float> by prefFlow(AppPreferencesKeys.TEMPERATURE, 15.0F)

    override suspend fun getTemperature(): Result<Float> {
        return Result.runCatching {
            temperatureFlow.first()
        }
    }

    override suspend fun setTemperature(newValue: Float) {
        setPrefValue(AppPreferencesKeys.TEMPERATURE, newValue)
    }

    // ---------------------------------------------------------------------------------------------

    private fun <T> prefFlow(key: Preferences.Key<T>, defaultValue: T) = ReadOnlyProperty<PrefRepositoryImpl, Flow<T>> { thisRef, _ ->
        thisRef.dataStore
            .data
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }.map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

    private suspend fun <T> setPrefValue(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}
