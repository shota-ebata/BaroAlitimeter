package com.ebata_shota.baroalitimeter.usecase

import com.ebata_shota.baroalitimeter.domain.model.ContentParams
import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import com.ebata_shota.baroalitimeter.domain.usecase.CalcUseCase
import com.ebata_shota.baroalitimeter.extensions.collectToList
import com.ebata_shota.baroalitimeter.infra.repository.spy.SpyPrefRepository
import com.ebata_shota.baroalitimeter.infra.repository.spy.SpySensorRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@Suppress("SameParameterValue")
class ContentParamsUseCaseImplTest {

    private lateinit var useCase: ContentParamsUseCaseImpl

    private lateinit var spySensorRepository: SpySensorRepository
    private lateinit var mockSensorRepository: SensorRepository

    private lateinit var spyPrefRepository: SpyPrefRepository
    private lateinit var mockPrefRepository: PrefRepository

    private lateinit var spyCalcUseCase: SpyCalcUseCase
    private lateinit var mockCalcUseCase: CalcUseCase

    private val basePreferencesModel = PreferencesModel(
        themeMode = ThemeMode.LIGHT,
        seaLevelPressure = 1000.0f,
        temperature = 15.0f,
        useTemperatureSensor = false
    )

    @Before
    fun setup() {
        spySensorRepository = SpySensorRepository()
        mockSensorRepository = mock()
        spyPrefRepository = SpyPrefRepository()
        mockPrefRepository = mock()
        spyCalcUseCase = SpyCalcUseCase()
        mockCalcUseCase = mock()
    }

    @Test
    fun test_contentParamsFlow() {
        runTest {
            // preparation
            useCase = ContentParamsUseCaseImpl(
                sensorRepository = spySensorRepository,
                prefRepository = spyPrefRepository,
                calcUseCase = spyCalcUseCase
            )
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
            spySensorRepository.emitPressureSensorState(Pressure.Success(pressure))
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
            whenever(mockPrefRepository.getTemperature()).thenReturn(Result.success(1.0f))
            useCase = ContentParamsUseCaseImpl(
                sensorRepository = spySensorRepository,
                prefRepository = mockPrefRepository,
                calcUseCase = spyCalcUseCase
            )

            // execute
            useCase.getTemperature()
            // assert: prefRepository.getTemperature()が一回実行されること
            verify(mockPrefRepository, times(1)).getTemperature()
        }
    }

    @Test
    fun test_setTemperature() {
        runTest {
            // preparation
            val temperature = 1.0f
            whenever(mockPrefRepository.setTemperature(temperature)).thenReturn(Unit)
            useCase = ContentParamsUseCaseImpl(
                sensorRepository = spySensorRepository,
                prefRepository = mockPrefRepository,
                calcUseCase = spyCalcUseCase
            )

            // execute
            useCase.setTemperature(temperature)
            // assert: prefRepository.setTemperature()が一回実行されること
            verify(mockPrefRepository, times(1)).setTemperature(temperature)
        }
    }

    @Test
    fun test_getAltitude() {
        runTest {
            // preparation
            val pressure = 1000.0f
            val seaLevelPressure = basePreferencesModel.seaLevelPressure
            val temperature = basePreferencesModel.temperature
            val altitude = 200.0f
            whenever(mockSensorRepository.getPressureSensor()).thenReturn(
                Result.success(
                    Pressure.Success(pressure)
                )
            )
            whenever(mockPrefRepository.getSeaLevelPressure()).thenReturn(
                Result.success(seaLevelPressure)
            )
            whenever(mockPrefRepository.getTemperature()).thenReturn(
                Result.success(temperature)
            )
            whenever(
                mockCalcUseCase.calcAltitude(
                    pressure = pressure,
                    temperature = temperature,
                    seaLevelPressure = seaLevelPressure
                )
            ).thenReturn(altitude)
            useCase = ContentParamsUseCaseImpl(
                sensorRepository = mockSensorRepository,
                prefRepository = mockPrefRepository,
                calcUseCase = mockCalcUseCase
            )

            // execute
            val result = useCase.getAltitude().getOrThrow()
            // assert: prefRepository.getAltitude()が一回実行されること
            assertEquals(altitude, result)
            verify(mockCalcUseCase, times(1)).calcAltitude(
                pressure = pressure,
                seaLevelPressure = seaLevelPressure,
                temperature = temperature
            )
        }
    }

    @Test
    fun test_setAltitude() {
        runTest {
            // preparation
            val pressure = 1000.0f
            val temperature = basePreferencesModel.temperature
            val altitude = 200.0f
            val newSeaLevelPressure = 999.0f
            whenever(mockSensorRepository.getPressureSensor()).thenReturn(
                Result.success(
                    Pressure.Success(pressure)
                )
            )
            whenever(mockPrefRepository.getTemperature()).thenReturn(
                Result.success(temperature)
            )
            whenever(
                mockCalcUseCase.calcSeaLevelPressure(
                    pressure = pressure,
                    temperature = temperature,
                    altitude = altitude
                )
            ).thenReturn(
                newSeaLevelPressure
            )
            useCase = ContentParamsUseCaseImpl(
                sensorRepository = mockSensorRepository,
                prefRepository = mockPrefRepository,
                calcUseCase = mockCalcUseCase
            )

            // execute
            useCase.setAltitude(altitude)
            // assert: prefRepository.setAltitude()が一回実行されること
            verify(mockCalcUseCase, times(1)).calcSeaLevelPressure(
                pressure = pressure,
                temperature = temperature,
                altitude = altitude
            )
            verify(mockPrefRepository, times(1)).setSeaLevelPressure(
                value = newSeaLevelPressure
            )
        }
    }

    @Test
    fun test_undoTemperature() {
        runTest {
            // preparation
            whenever(mockPrefRepository.undoTemperature()).thenReturn(Unit)
            useCase = ContentParamsUseCaseImpl(
                sensorRepository = mockSensorRepository,
                prefRepository = mockPrefRepository,
                calcUseCase = mockCalcUseCase
            )

            // execute
            useCase.undoTemperature()
            // assert: prefRepository.undoTemperature()が一回実行されること
            verify(mockPrefRepository, times(1)).undoTemperature()
        }
    }

    @Test
    fun test_undoSeaLevelPressure() {
        runTest {
            // preparation
            whenever(mockPrefRepository.undoSeaLevelPressure()).thenReturn(Unit)
            useCase = ContentParamsUseCaseImpl(
                sensorRepository = mockSensorRepository,
                prefRepository = mockPrefRepository,
                calcUseCase = mockCalcUseCase
            )

            // execute
            useCase.undoSeaLevelPressure()
            // assert: prefRepository.undoSeaLevelPressure()が一回実行されること
            verify(mockPrefRepository, times(1)).undoSeaLevelPressure()
        }
    }
}
