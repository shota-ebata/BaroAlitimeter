package com.ebata_shota.baroalitimeter.infra

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object AppPreferencesKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val SEA_LEVEL_PRESSURE = floatPreferencesKey("seaLevelPressure")
    val OLD_SEA_LEVEL_PRESSURE = floatPreferencesKey("oldSeaLevelPressure")
    val TEMPERATURE = floatPreferencesKey("temperature")
    val OLD_TEMPERATURE = floatPreferencesKey("oldTemperature")
    val USE_TEMPERATURE_SENSOR = booleanPreferencesKey("useTemperatureSensor")
}
