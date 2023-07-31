package com.ebata_shota.baroalitimeter.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface PrefRepository {
    val seaLevelPressureState: StateFlow<Float>
    fun setSeaLevelPressure(newValue: Float)

    val temperatureState: StateFlow<Float>
    fun setTemperature(newValue: Float)
}