package com.ebata_shota.baroalitimeter.usecase

import com.ebata_shota.baroalitimeter.domain.usecase.CalcUseCase

class SpyCalcUseCase : CalcUseCase {

    val calcAltitudeParamList = mutableListOf<CalcAltitudeParam>()
    var calcAltitudeResult: Float = 0.0f
    override suspend fun calcAltitude(
        pressure: Float,
        seaLevelPressure: Float,
        temperature: Float
    ): Float {
        calcAltitudeParamList.add(
            CalcAltitudeParam(
                pressure = pressure,
                seaLevelPressure = seaLevelPressure,
                temperature = temperature
            )
        )
        return calcAltitudeResult
    }

    data class CalcAltitudeParam(
        val pressure: Float,
        val seaLevelPressure: Float,
        val temperature: Float
    )

    var calcSeaLevelPressureResult: Float = 0.0f
    override suspend fun calcSeaLevelPressure(
        pressure: Float,
        temperature: Float,
        altitude: Float
    ): Float {
       return calcSeaLevelPressureResult
    }
}