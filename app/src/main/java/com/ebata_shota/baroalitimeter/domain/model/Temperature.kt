package com.ebata_shota.baroalitimeter.domain.model

sealed class Temperature {
    object Loading : Temperature()
    object HasNotSensor : Temperature()
    data class Success(val value: Float) : Temperature()
}
