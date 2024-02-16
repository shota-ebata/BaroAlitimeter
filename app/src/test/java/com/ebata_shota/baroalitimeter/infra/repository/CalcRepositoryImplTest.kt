package com.ebata_shota.baroalitimeter.infra.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalCoroutinesApi::class)
class CalcRepositoryImplTest {

    private lateinit var repository: CalcRepositoryImpl

    @Before
    fun setup() {
        repository = CalcRepositoryImpl(
            dispatcher = UnconfinedTestDispatcher()
        )
    }

    @Test
    fun calcAltitude() {
        runTest {
            val result: Float = repository.calcAltitude(
                pressure = 900.0f,
                seaLevelPressure = 1013.25f,
                temperature = 15.0f
            )
            // 小数点以下第3位で四捨五入して
            assertEquals(1010.83f, BigDecimal(result.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toFloat())
        }
    }

    @Test
    fun calcAltitudeFail() {
        runTest {
            val result: Float = repository.calcAltitude(
                pressure = 900.0f,
                seaLevelPressure = 1013.25f,
                temperature = 15.0f
            )
            // 小数点以下第3位で四捨五入して
            assertEquals(1010.82f, BigDecimal(result.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toFloat())
        }
    }
}