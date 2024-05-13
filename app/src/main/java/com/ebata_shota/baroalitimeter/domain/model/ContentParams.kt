package com.ebata_shota.baroalitimeter.domain.model

data class ContentParams(
    val pressure: Float,
    val temperature: Float,
    val seaLevelPressure: Float,
    val altitude: Float
)