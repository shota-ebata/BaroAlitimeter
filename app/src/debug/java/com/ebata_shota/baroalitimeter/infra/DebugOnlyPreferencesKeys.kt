package com.ebata_shota.baroalitimeter.infra

import androidx.datastore.preferences.core.intPreferencesKey

object DebugOnlyPreferencesKeys {
    val DummySensor = intPreferencesKey("dummy_sensor")
}