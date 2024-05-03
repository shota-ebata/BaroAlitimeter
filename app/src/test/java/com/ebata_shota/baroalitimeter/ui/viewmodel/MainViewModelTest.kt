package com.ebata_shota.baroalitimeter.ui.viewmodel

import com.ebata_shota.baroalitimeter.domain.model.ContentParams
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.usecase.ContentParamsUseCase
import com.ebata_shota.baroalitimeter.domain.usecase.ThemeUseCase
import com.ebata_shota.baroalitimeter.usecase.spy.SpyContentParamsUseCase
import com.ebata_shota.baroalitimeter.usecase.spy.SpyThemeUseCase
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy

class MainViewModelTest {

    private lateinit var viewModel: MainViewModel

    private lateinit var spyContentParamsUseCase: SpyContentParamsUseCase
    private lateinit var mockContentParamsUseCase: ContentParamsUseCase

    private val spyThemeUseCase: SpyThemeUseCase = spy(SpyThemeUseCase())
    private lateinit var mockThemeUseCase: ThemeUseCase

    private val firebaseAnalytics: FirebaseAnalytics = mock()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        // memo: TestDispatcherについて https://developer.android.com/kotlin/coroutines/test?hl=ja#standardtestdispatcher
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)

        spyContentParamsUseCase = spy(SpyContentParamsUseCase())
        mockContentParamsUseCase = mock()

        mockThemeUseCase = mock()


        runTest {
            spyContentParamsUseCase.emitContentParamsFlow(
                ContentParams(
                    pressure = 1000.0f,
                    temperature = 25.0f,
                    seaLevelPressure = 1000.0f,
                    altitude = 0.0f
                )
            )
        }
    }

    /**
     * 初期のモードはViewer
     */
    @Test
    fun test_defaultMode() {
        runTest {
            // preparation
            viewModel = MainViewModel(
                contentParamsUseCase = spyContentParamsUseCase,
                themeUseCase = spyThemeUseCase,
                firebaseAnalytics = firebaseAnalytics
            )
            spyThemeUseCase.emitThemeMode(ThemeMode.LIGHT)
            // execute


            // assert
            assertEquals(MainViewModel.Mode.Viewer, viewModel.modeState.value)
        }
    }

    // TODO: テスト追加
}
