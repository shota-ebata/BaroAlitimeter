package com.ebata_shota.baroalitimeter.infra.repository

import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * Only:
 * Int
 * Long
 * Float
 * String
 * Boolean
 * Set<String>
 */
class PrefRepositoryImpl
@Inject
constructor(
    context: Context
) : PrefRepository {
    private val appPref: SharedPreferences = context.getSharedPreferences("app_pref", Context.MODE_PRIVATE)

    // ユーザUID
    var userUid: String by pref(default = UUID.randomUUID().toString())

    // 海面気圧 デフォルトは1013.25f
    private var seaLevelPressurePref: Float by pref(default = SensorManager.PRESSURE_STANDARD_ATMOSPHERE)
    private val _seaLevelPressureState = MutableStateFlow(seaLevelPressurePref)
    override val seaLevelPressureState: StateFlow<Float> = _seaLevelPressureState.asStateFlow()
    override fun setSeaLevelPressure(newValue: Float) {
        seaLevelPressurePref = newValue
        _seaLevelPressureState.value = newValue
    }

    // 気温
    private var temperaturePref: Float by pref(default = 15.0f)
    private val _temperatureState = MutableStateFlow(temperaturePref)
    override val temperatureState: StateFlow<Float> = _temperatureState.asStateFlow()
    override fun setTemperature(newValue: Float) {
        temperaturePref = newValue
        _temperatureState.value = newValue
    }

    // ---------------------------------------------------------------------------------------------

    private fun <T> pref(default: T): ReadWriteProperty<PrefRepositoryImpl, T> = object : ReadWriteProperty<PrefRepositoryImpl, T> {

        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: PrefRepositoryImpl, property: KProperty<*>): T {
            val key = property.name
            return (appPref.all[key] as? T) ?: run {
                put(key, default)
                default
            }
        }

        override fun setValue(thisRef: PrefRepositoryImpl, property: KProperty<*>, value: T) {
            val key = property.name
            put(key, value)
        }
    }

    private fun <T : Any?> put(key: String, value: T?) {
        val editor = appPref.edit()
        when (value) {
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is String -> editor.putString(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Set<*> -> editor.putStringSet(key, value.map { it as String }.toSet())
            null -> editor.remove(key)
            else -> throw IllegalArgumentException("用意されていない型")
        }
        editor.apply()
    }
}
