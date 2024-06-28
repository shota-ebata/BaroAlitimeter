package com.ebata_shota.baroalitimeter.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalCoroutinesApi::class)
class CalcUseCaseImplTest {

    private lateinit var useCase: CalcUseCaseImpl

    @Before
    fun setup() {
        useCase = CalcUseCaseImpl(
            dispatcher = UnconfinedTestDispatcher()
        )
    }

    @Test
    fun calcAltitude() {
        runTest {
            val result: Float = useCase.calcAltitude(
                pressure = 900.0f,
                seaLevelPressure = 1013.25f,
                temperature = 15.0f
            )
            // 小数点以下第3位で四捨五入して
            assertEquals(1010.83f, BigDecimal(result.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toFloat())
        }
    }

    @Test
    fun test_failed() {
        assertEquals(1, 2)
    }
}
