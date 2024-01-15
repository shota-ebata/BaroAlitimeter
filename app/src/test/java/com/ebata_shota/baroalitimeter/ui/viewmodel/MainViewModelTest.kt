package com.ebata_shota.baroalitimeter.ui.viewmodel

import android.hardware.SensorManager
import com.ebata_shota.baroalitimeter.domain.model.PreferencesModel
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.repository.CalcRepository
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever


class MainViewModelTest {


    private val sensorRepository: SensorRepository = mock()
    private val prefRepository: PrefRepository = mock()
    private val calcRepository: CalcRepository = mock()
    private val firebaseAnalytics: FirebaseAnalytics = mock()


    private lateinit var viewModel: MainViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setup() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)


        whenever(sensorRepository.pressureSensorState).thenReturn(
            MutableStateFlow(
                Pressure.Success(
                    value = SensorManager.PRESSURE_STANDARD_ATMOSPHERE
                )
            )
        )
        whenever(sensorRepository.temperatureSensorState).thenReturn(
            MutableStateFlow(
                Temperature.HasNotSensor
            )
        )
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


    @Test
    @DisplayName("初期のモードはMode.Viewer")
    fun modeViewer() {
        assertEquals(MainViewModel.Mode.Viewer, viewModel.modeState.value)
    }
}