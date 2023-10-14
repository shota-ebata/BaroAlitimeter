package com.ebata_shota.baroalitimeter.ui.model

import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel

data class SensorAndPrefModel(
    val mode: MainViewModel.Mode,
    val pressureSensorState: Pressure,
    val temperatureSensorState: Temperature,
    val preferencesModel: PreferencesModel
)
