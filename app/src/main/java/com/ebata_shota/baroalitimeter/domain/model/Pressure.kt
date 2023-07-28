package com.ebata_shota.baroalitimeter.domain.model

sealed class Pressure {
    object Loading : Pressure()
    data class Success(val value: Float) : Pressure()
}