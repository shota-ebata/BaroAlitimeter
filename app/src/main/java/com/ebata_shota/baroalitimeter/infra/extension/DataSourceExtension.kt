package com.ebata_shota.baroalitimeter.infra.extension

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlin.properties.ReadOnlyProperty

@Suppress("UNCHECKED_CAST")
fun <T, V> DataStore<Preferences>.prefFlow(
    key: Preferences.Key<V>,
    defaultValue: V?,
) = ReadOnlyProperty<T, Flow<V>> { _, _ ->
    this.data.catch { throwable ->
        if (throwable is IOException) {
            emit(emptyPreferences())
        } else {
            throw throwable
        }
    }.map { preferences ->
        preferences[key] ?: defaultValue as V
    }
}

suspend fun <T> DataStore<Preferences>.setPrefValue(
    key: Preferences.Key<T>,
    value: T,
) {
    this.edit { preferences ->
        preferences[key] = value
    }
}

suspend fun <T> DataStore<Preferences>.removePref(key: Preferences.Key<T>) {
    this.edit { preferences ->
        preferences.remove(key)
    }
}