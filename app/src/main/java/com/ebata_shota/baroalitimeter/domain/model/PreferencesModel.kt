package com.ebata_shota.baroalitimeter.domain.model

data class PreferencesModel(
    val seaLevelPressure: Float,
    val temperature: Float,
    val useTemperatureSensor: Boolean,
)