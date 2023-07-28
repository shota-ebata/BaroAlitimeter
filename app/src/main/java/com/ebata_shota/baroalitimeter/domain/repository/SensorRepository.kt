package com.ebata_shota.baroalitimeter.domain.repository

import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import kotlinx.coroutines.flow.StateFlow

interface SensorRepository {
    val pressureState: StateFlow<Pressure>
    val temperatureState: StateFlow<Temperature>
}