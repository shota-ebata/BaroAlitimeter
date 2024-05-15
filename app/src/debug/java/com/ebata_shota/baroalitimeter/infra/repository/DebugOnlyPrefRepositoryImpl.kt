package com.ebata_shota.baroalitimeter.infra.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ebata_shota.baroalitimeter.di.annotation.DebugOnly
import com.ebata_shota.baroalitimeter.domain.content.DummySensor
import com.ebata_shota.baroalitimeter.domain.repository.DebugOnlyPrefRepository
import com.ebata_shota.baroalitimeter.infra.DebugOnlyPreferencesKeys
import com.ebata_shota.baroalitimeter.infra.extension.prefFlow
import com.ebata_shota.baroalitimeter.infra.extension.setPrefValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DebugOnlyPrefRepositoryImpl
@Inject
constructor(
    @DebugOnly
    private val dataStore: DataStore<Preferences>,
) : DebugOnlyPrefRepository {
    // センサーダミーフラグ
    private val _dummySensor: Flow<Int> by dataStore.prefFlow(
        key = DebugOnlyPreferencesKeys.DummySensor,
        defaultValue = DummySensor.OFF.ordinal
    )
    override val dummySensor: Flow<DummySensor> = _dummySensor.map {
        DummySensor.values().first { it.ordinal == _dummySensor.first() }
    }

    override suspend fun getDummySensor(): DummySensor {
        return dummySensor.first()
    }

    override suspend fun setDummySensor(value: DummySensor) {
        dataStore.setPrefValue(DebugOnlyPreferencesKeys.DummySensor, value.ordinal)
    }
}