package com.ebata_shota.baroalitimeter.usecase

import com.ebata_shota.baroalitimeter.di.annotation.CoroutineDispatcherDefault
import com.ebata_shota.baroalitimeter.domain.usecase.CalcUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.pow

class CalcUseCaseImpl
@Inject
constructor(
    @CoroutineDispatcherDefault
    private val dispatcher: CoroutineDispatcher,
) : CalcUseCase {

    override suspend fun calcAltitude(pressure: Float, seaLevelPressure: Float, temperature: Float): Float {
        return withContext(dispatcher) {
            ((((seaLevelPressure.toDouble() / pressure.toDouble()).pow(1 / 5.257) - 1) * (temperature + 273.15)) / 0.0065).toFloat()
        }
    }

    override suspend fun calcSeaLevelPressure(pressure: Float, temperature: Float, altitude: Float): Float {
        return withContext(dispatcher) {
            val d = 0.0065 * altitude
            (pressure * (1 - (d / (temperature + d + 273.15))).pow(-5.257)).toFloat()
        }
    }
}