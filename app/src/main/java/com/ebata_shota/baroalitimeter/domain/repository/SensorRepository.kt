package com.ebata_shota.baroalitimeter.domain.repository

import com.ebata_shota.baroalitimeter.domain.model.Pressure
import kotlinx.coroutines.flow.StateFlow

interface SensorRepository {
    val pressureState: StateFlow<Pressure>
}