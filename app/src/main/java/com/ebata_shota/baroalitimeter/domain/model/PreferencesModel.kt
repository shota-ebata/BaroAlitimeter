package com.ebata_shota.baroalitimeter.domain.model

import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode

data class PreferencesModel(
    val themeMode: ThemeMode,
    val seaLevelPressure: Float,
    val temperature: Float,
    val useTemperatureSensor: Boolean,
)