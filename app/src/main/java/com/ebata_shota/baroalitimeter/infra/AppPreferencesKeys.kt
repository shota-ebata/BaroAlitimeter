package com.ebata_shota.baroalitimeter.infra

import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object AppPreferencesKeys {
    val USER_UID = stringPreferencesKey("userUid")
    val SEA_LEVEL_PRESSURE = floatPreferencesKey("seaLevelPressure")
    val TEMPERATURE = floatPreferencesKey("temperature")
}