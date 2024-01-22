package com.ebata_shota.baroalitimeter.ui.viewmodel

import android.hardware.SensorManager
import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.repository.CalcRepository
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.infra.repository.CalcRepositoryImpl
import com.ebata_shota.baroalitimeter.infra.repository.SpySensorRepository
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever


class MainViewModelTest {

    private val sensorRepository: SpySensorRepository = spy(SpySensorRepository())
    private val prefRepository: PrefRepository = mock()
    private lateinit var calcRepository: CalcRepository
    private val firebaseAnalytics: FirebaseAnalytics = mock()


    private lateinit var viewModel: MainViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        // memo: TestDispatcherについて https://developer.android.com/kotlin/coroutines/test?hl=ja#standardtestdispatcher
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)

        calcRepository = CalcRepositoryImpl(dispatcher = dispatcher)

        runTest {
            sensorRepository.emitPressureSensorState(
                Pressure.Success(
                    value = SensorManager.PRESSURE_STANDARD_ATMOSPHERE
                )
            )
            sensorRepository.emitTemperatureSensorState(
                Temperature.HasNotSensor
            )
        }

        whenever(prefRepository.preferencesFlow).thenReturn(
            MutableStateFlow(
                PreferencesModel(
                    themeMode = ThemeMode.LIGHT,
                    seaLevelPressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                    temperature = 15.0F,
                    useTemperatureSensor = false
                )
            )
        )

        viewModel = MainViewModel(
            sensorRepository = sensorRepository,
            prefRepository = prefRepository,
            calcRepository = calcRepository,
            firebaseAnalytics = firebaseAnalytics
        )
    }


    /**
     * 初期のモードはViewer
     */
    @Test
    fun defaultMode() {
        assertEquals(MainViewModel.Mode.Viewer, viewModel.modeState.value)
    }
}