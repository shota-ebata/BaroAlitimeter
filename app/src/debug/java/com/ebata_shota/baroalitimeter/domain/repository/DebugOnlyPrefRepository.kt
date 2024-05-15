package com.ebata_shota.baroalitimeter.domain.repository

import com.ebata_shota.baroalitimeter.domain.content.DummySensor
import kotlinx.coroutines.flow.Flow

interface DebugOnlyPrefRepository {
    val dummySensor: Flow<DummySensor>
    suspend fun getDummySensor(): DummySensor
    suspend fun setDummySensor(value: DummySensor)
}