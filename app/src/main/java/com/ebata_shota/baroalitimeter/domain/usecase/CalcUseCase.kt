package com.ebata_shota.baroalitimeter.domain.usecase

interface CalcUseCase {

    suspend fun calcAltitude(
        pressure: Float,
        seaLevelPressure: Float,
        temperature: Float,
    ): Float

    suspend fun calcSeaLevelPressure(
        pressure: Float,
        temperature: Float,
        altitude: Float,
    ): Float
}