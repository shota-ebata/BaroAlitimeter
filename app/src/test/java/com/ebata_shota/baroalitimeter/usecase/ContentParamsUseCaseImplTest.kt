package com.ebata_shota.baroalitimeter.usecase

import com.ebata_shota.baroalitimeter.domain.model.ContentParams
import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.extensions.collectToList
import com.ebata_shota.baroalitimeter.infra.repository.spy.SpyPrefRepository
import com.ebata_shota.baroalitimeter.infra.repository.spy.SpySensorRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@Suppress("SameParameterValue")
class ContentParamsUseCaseImplTest {

    private lateinit var useCase: ContentParamsUseCaseImpl

    private lateinit var spySensorRepository: SpySensorRepository
    private lateinit var spyPrefRepository: SpyPrefRepository
    private lateinit var spyCalcUseCase: SpyCalcUseCase

    private val basePreferencesModel = PreferencesModel(
        themeMode = ThemeMode.LIGHT,
        seaLevelPressure = 1000.0f,
        temperature = 15.0f,
        useTemperatureSensor = false
    )

    @Before
    fun setup() {
        spySensorRepository = spyk(SpySensorRepository())
        spyPrefRepository = spyk(SpyPrefRepository())
        spyCalcUseCase = spyk(SpyCalcUseCase())
        useCase = ContentParamsUseCaseImpl(
            sensorRepository = spySensorRepository,
            prefRepository = spyPrefRepository,
            calcUseCase = spyCalcUseCase
        )
    }

    @Test
    fun test_contentParamsFlow() {
        runTest {
            // preparation
            val pressure = 1001.0f
            val seaLevelPressure = basePreferencesModel.seaLevelPressure
            val temperature = basePreferencesModel.temperature
            val calcAltitudeResult = 10.0f
            spyCalcUseCase.calcAltitudeResult = calcAltitudeResult

            // 初期状態ではFlowが発火しないこと
            val result = collectToList(useCase.contentParamsFlow)
            // assert
            assertTrue(result.isEmpty())

            // 圧力センサーの結果が取得済みになってもFlowが発火しないこと
            spySensorRepository.emitPressureSensorState(pressure)
            // assert
            assertTrue(result.isEmpty())

            // execute
            spyPrefRepository.emitPreferencesFlow(basePreferencesModel)
            // assert: SharedPreferencesの結果も揃ったときにFlowが発火すること
            val expected = ContentParams(
                pressure = pressure,
                temperature = temperature,
                seaLevelPressure = seaLevelPressure,
                altitude = calcAltitudeResult
            )
            assertEquals(expected, result.last())

            // 呼ばれたパラメータも確認
            val params = spyCalcUseCase.calcAltitudeParamList
            assertTrue(params.isNotEmpty())
            val param = params.last()
            assertEquals(pressure, param.pressure)
            assertEquals(seaLevelPressure, param.seaLevelPressure)
            assertEquals(temperature, param.temperature)
        }
    }

    @Test
    fun test_getTemperature() {
        runTest {
            // preparation
            coEvery { spyPrefRepository.getTemperature() } returns 1.0f

            // execute
            useCase.getTemperature()
            // assert: prefRepository.getTemperature()が一回実行されること
            coVerify { spyPrefRepository.getTemperature() }
        }
    }

    @Test
    fun test_setTemperature() {
        runTest {
            // preparation
            val temperature = basePreferencesModel.temperature
            coEvery { spyPrefRepository.setTemperature(temperature) } returns Unit

            // execute
            useCase.setTemperature(temperature)
            // assert: prefRepository.setTemperature()が一回実行されること
            coVerify { spyPrefRepository.setTemperature(temperature) }
        }
    }

    @Test
    fun test_getAltitude() {
        runTest {
            // preparation
            val temperature = basePreferencesModel.temperature
            val pressure = 1000.0f
            val seaLevelPressure = basePreferencesModel.seaLevelPressure
            val altitude = 200.0f
            coEvery { spyPrefRepository.getTemperature() } returns temperature
            coEvery { spySensorRepository.getPressure() } returns pressure
            coEvery { spyPrefRepository.getSeaLevelPressure() } returns seaLevelPressure
            coEvery {
                spyCalcUseCase.calcAltitude(
                    pressure = pressure,
                    temperature = temperature,
                    seaLevelPressure = seaLevelPressure
                )
            } returns altitude

            // execute
            val result = useCase.getAltitude()
            // assert: prefRepository.getAltitude()が一回実行されること
            assertEquals(altitude, result)
            coVerify {
                spyCalcUseCase.calcAltitude(
                    pressure = pressure,
                    seaLevelPressure = seaLevelPressure,
                    temperature = temperature
                )
            }
        }
    }

    @Test
    fun test_setAltitude() {
        runTest {
            // preparation
            val temperature = basePreferencesModel.temperature
            val pressure = 1000.0f
            val altitude = 200.0f
            val newSeaLevelPressure = basePreferencesModel.seaLevelPressure
            coEvery { spyPrefRepository.getTemperature() } returns temperature
            coEvery { spySensorRepository.getPressure() } returns pressure
            coEvery {
                spyCalcUseCase.calcSeaLevelPressure(
                    pressure = pressure,
                    temperature = temperature,
                    altitude = altitude
                )
            } returns newSeaLevelPressure
            coEvery {
                spyPrefRepository.setSeaLevelPressure(newSeaLevelPressure)
            } returns Unit

            // execute
            useCase.setAltitude(altitude)
            // assert: prefRepository.setAltitude()が一回実行されること
            coVerify {
                spyCalcUseCase.calcSeaLevelPressure(
                    pressure = pressure,
                    temperature = temperature,
                    altitude = altitude
                )
            }
            coVerify {
                spyPrefRepository.setSeaLevelPressure(newSeaLevelPressure)
            }
        }
    }

    @Test
    fun test_undoTemperature() {
        runTest {
            // preparation
            coEvery { spyPrefRepository.undoTemperature() } returns Unit

            // execute
            useCase.undoTemperature()
            // assert: prefRepository.undoTemperature()が一回実行されること
            coVerify { spyPrefRepository.undoTemperature() }
        }
    }

    @Test
    fun test_undoSeaLevelPressure() {
        runTest {
            // preparation
            coEvery { spyPrefRepository.undoSeaLevelPressure() } returns Unit

            // execute
            useCase.undoSeaLevelPressure()
            // assert: prefRepository.undoSeaLevelPressure()が一回実行されること
            coVerify { spyPrefRepository.undoSeaLevelPressure() }
        }
    }
}
