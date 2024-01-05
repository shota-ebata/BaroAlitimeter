package com.ebata_shota.baroalitimeter.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.ui.content.MainContent
import com.ebata_shota.baroalitimeter.ui.content.RadioListContent
import com.ebata_shota.baroalitimeter.ui.model.ThemeModeRadioOption
import com.ebata_shota.baroalitimeter.ui.parts.AltitudePartsEvents
import com.ebata_shota.baroalitimeter.ui.parts.MainTopAppBar
import com.ebata_shota.baroalitimeter.ui.parts.TemperaturePartsEvents
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    selectedThemeMode: ThemeMode,
) {
    val uiState: MainViewModel.MainUiState by viewModel.mainUiState.collectAsStateWithLifecycle()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    var shouldShowTopAppBarDropdownMenu: Boolean by remember {
        mutableStateOf(false)
    }
    // stateだけど・・・ViewModelに置くのは少し難しいように見える
    val snackbarHostState: SnackbarHostState = remember {
        SnackbarHostState()
    }
    // stateだけど・・・ViewModelに置くのは少し難しいように見える
    val themeModalBottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )


    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // memo: immediateって？ https://qiita.com/dowa/items/8f05a92c7f5f59da5cb1
            withContext(Dispatchers.Main.immediate) {
                viewModel.showUndoSnackBarEvent.collect { event ->
                    val snackbarResult = snackbarHostState.showSnackbar(
                        message = context.getString(event.snackBarText),
                        actionLabel = context.getString(R.string.snack_bar_action_label_undo),
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                    when (snackbarResult) {
                        SnackbarResult.Dismissed -> viewModel.onDismissedSnackBar(event)
                        SnackbarResult.ActionPerformed -> viewModel.onActionPerformedSnackBar(event)
                    }
                }
            }
        }
    }
    Scaffold(
        topBar = {
            MainTopAppBar(
                expanded = shouldShowTopAppBarDropdownMenu,
                showTopAppBarDropdownMenu = { shouldShowTopAppBarDropdownMenu = true },
                hideTopAppBarDropdownMenu = { shouldShowTopAppBarDropdownMenu = false },
                onClickTheme = {
                    coroutineScope.launch {
                        themeModalBottomSheetState.show()
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }
    ) { innerPadding ->
        ModalBottomSheetLayout(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            sheetState = themeModalBottomSheetState,
            sheetContent = {
                RadioListContent(
                    radioOptions = ThemeModeRadioOption.values(),
                    selectedOption = ThemeModeRadioOption.of(selectedThemeMode),
                    onOptionSelected = { themeModeRadioOption ->
                        coroutineScope.launch {
                            themeModalBottomSheetState.hide()
                            viewModel.onSelectedThemeMode(themeModeRadioOption.themeMode)
                        }
                    }
                )
            }
        ) {
            Surface(
                modifier = modifier
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (val currentUiState = uiState) {
                    MainViewModel.MainUiState.Loading -> Unit // FIXME: ローディング中表示があれば実装したい
                    is MainViewModel.MainUiState.UiState -> {
                        MainContent(
                            uiState = currentUiState,
                            /**
                             * FIXME: objectを毎回インスタンス生成するの馬鹿馬鹿しい
                             *  ViewModelにinterfaceを実装するのって微妙かな？
                             *  interfaceとはいえ子のComposable関数にViewModelを渡すのは微妙か？
                             */
                            temperaturePartsEvents = object : TemperaturePartsEvents {
                                override fun onChangeTemperatureTextFieldValue(textFieldValue: TextFieldValue) {
                                    viewModel.onChangeTemperatureTextFieldValue(textFieldValue)
                                }

                                override fun onClickTemperature() {
                                    viewModel.onClickTemperature()
                                }

                                override fun onClickDoneEditTemperature() {
                                    viewModel.onClickDoneEditTemperature()
                                }

                                override fun onClickCancelEditTemperature() {
                                    viewModel.onClickCancelEditTemperature()
                                }

                            },
                            altitudePartsEvents = object : AltitudePartsEvents {
                                override fun onChangeAltitudeTextFieldValue(textFieldValue: TextFieldValue) {
                                    viewModel.onChangeAltitudeTextFieldValue(textFieldValue)
                                }

                                override fun onClickAltitude() {
                                    viewModel.onClickAltitude()
                                }

                                override fun onClickDoneEditAltitude() {
                                    viewModel.onClickDoneEditAltitude()
                                }

                                override fun onClickCancelAltitude() {
                                    viewModel.onClickCancelAltitude()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}