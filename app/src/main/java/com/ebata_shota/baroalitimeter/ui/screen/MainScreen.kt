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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.ui.content.MainContent
import com.ebata_shota.baroalitimeter.ui.content.RadioListContent
import com.ebata_shota.baroalitimeter.ui.model.ThemeModeRadioOption
import com.ebata_shota.baroalitimeter.ui.parts.MainTopAppBar
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    selectedThemeMode: ThemeMode,
) {
    val uiState: MainViewModel.MainUiState by viewModel.mainUiState.collectAsStateWithLifecycle()
    val mode: MainViewModel.Mode by viewModel.modeState.collectAsStateWithLifecycle()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    var shouldShowTopAppBarDropdownMenu: Boolean by remember {
        mutableStateOf(false)
    }
    val snackbarHostState: SnackbarHostState = remember {
        SnackbarHostState()
    }
    val themeModalBottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
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
                // FIXME: stringResourceがComposable内部でしか呼び出せなくて、適切な場所に自信がない
                val snackBarMessage = when (mode) {
                    MainViewModel.Mode.Viewer -> "" // この状態で表示する想定がないので空文字だが・・・
                    MainViewModel.Mode.EditTemperature -> stringResource(id = R.string.snack_bar_message_change_temperature)
                    MainViewModel.Mode.EditAltitude -> stringResource(id = R.string.snack_bar_message_change_altitude)
                }
                val snackBarActionLabel = stringResource(id = R.string.snack_bar_action_label_undo)
                when (val currentUiState = uiState) {
                    MainViewModel.MainUiState.Loading -> Unit // FIXME: ローディング中表示があれば実装したい
                    is MainViewModel.MainUiState.UiState -> {
                        MainContent(
                            uiState = currentUiState,
                            onChangeTemperatureTextFieldValue = viewModel::updateTemperatureTextFieldValue,
                            onClickTemperature = viewModel::changeModeToEditTemperature,
                            onClickDoneEditTemperature = {
                                viewModel.onCompletedEditTemperature()
                                coroutineScope.launch {
                                    val snackbarResult = snackbarHostState.showSnackbar(
                                        message = snackBarMessage,
                                        actionLabel = snackBarActionLabel,
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                    when (snackbarResult) {
                                        SnackbarResult.Dismissed -> viewModel.onDismissedTemperatureSnackBar()
                                        SnackbarResult.ActionPerformed -> viewModel.undoTemperature()
                                    }
                                }
                            },
                            onClickCancelEditTemperature = viewModel::cancelEditTemperature,
                            onChangeAltitudeTextFieldValue = viewModel::updateAltitudeTextFieldValue,
                            onClickAltitude = viewModel::changeModeToEditAltitude,
                            onClickDoneEditAltitude = {
                                viewModel.onCompletedEditAltitude()
                                coroutineScope.launch {
                                    val snackbarResult = snackbarHostState.showSnackbar(
                                        message = snackBarMessage,
                                        actionLabel = snackBarActionLabel,
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                    when (snackbarResult) {
                                        SnackbarResult.Dismissed -> viewModel.onDismissedAltitudeSnackbar()
                                        SnackbarResult.ActionPerformed -> viewModel.undoAltitude()
                                    }
                                }
                            },
                            onClickCancelAltitude = viewModel::cancelEditAltitude,
                            modifier = Modifier,
                        )
                    }
                }
            }
        }
    }
}