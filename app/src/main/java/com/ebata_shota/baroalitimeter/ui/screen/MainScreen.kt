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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.ui.content.EditModeAltitudeContent
import com.ebata_shota.baroalitimeter.ui.content.EditModeTemperature
import com.ebata_shota.baroalitimeter.ui.content.RadioListContent
import com.ebata_shota.baroalitimeter.ui.content.ViewerModeContent
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
    onSelectedThemeMode: (ThemeMode) -> Unit,
    updateTemperatureTextFieldValue: (TextFieldValue) -> Unit,
    updateAltitudeTextFieldValue: (TextFieldValue) -> Unit,
    onClickTemperature: () -> Unit,
    onClickAltitude: () -> Unit,
    onClickCancelTemperature: () -> Unit,
    onClickCancelAltitude: () -> Unit,
    onCompletedEditTemperature: () -> Unit,
    onCompletedEditAltitude: () -> Unit,
    undoAltitude: () -> Unit,
    undoTemperature: () -> Unit,
    onDismissedAltitudeSnackbar: () -> Unit,
    onDismissedTemperatureSnackBar: () -> Unit,
) {
    val uiState: MainViewModel.UiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                            onSelectedThemeMode(themeModeRadioOption.themeMode)
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
                when (val uiState = uiState) {
                    is MainViewModel.UiState.Loading -> Unit // FIXME: ローディング中表示があれば実装したい
                    is MainViewModel.UiState.ViewerMode -> {
                        ViewerModeContent(
                            pressureText = uiState.pressureText,
                            altitudeText = uiState.altitudeText,
                            temperatureText = uiState.temperatureText,
                            onClickTemperature = onClickTemperature,
                            onClickAltitude = onClickAltitude,
                            seaLevelPressure = uiState.seaLevelPressureText
                        )
                    }

                    is MainViewModel.UiState.EditAltitudeMode -> {
                        // FIXME: stringResourceがComposable内部でしか呼び出せなくて、適切な場所に自信がない
                        val snackBarMessage = stringResource(id = R.string.snack_bar_message_change_altitude)
                        val snackBarActionLabel = stringResource(id = R.string.snack_bar_action_label_undo)
                        EditModeAltitudeContent(
                            pressureText = uiState.pressureText,
                            seaLevelPressure = uiState.seaLevelPressureText,
                            altitudeTextFieldValue = uiState.altitudeTextFieldValue,
                            updateAltitudeTextFieldValue = updateAltitudeTextFieldValue,
                            temperatureText = uiState.temperatureText,
                            onClickDone = {
                                onCompletedEditAltitude()
                                coroutineScope.launch {
                                    val snackbarResult = snackbarHostState.showSnackbar(
                                        message = snackBarMessage,
                                        actionLabel = snackBarActionLabel,
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                    when (snackbarResult) {
                                        SnackbarResult.Dismissed -> onDismissedAltitudeSnackbar()
                                        SnackbarResult.ActionPerformed -> undoAltitude()
                                    }
                                }
                            },
                            onClickCancel = onClickCancelAltitude
                        )
                    }

                    is MainViewModel.UiState.EditTemperatureMode -> {
                        // FIXME: stringResourceがComposable内部でしか呼び出せなくて、適切な場所に自信がない
                        val snackBarMessage = stringResource(id = R.string.snack_bar_message_change_temperature)
                        val snackBarActionLabel = stringResource(id = R.string.snack_bar_action_label_undo)
                        EditModeTemperature(
                            pressureText = uiState.pressureText,
                            seaLevelPressure = uiState.seaLevelPressureText,
                            temperatureTextFieldValue = uiState.temperatureTextFieldValue,
                            updateTemperatureTextFieldValue = updateTemperatureTextFieldValue,
                            altitudeText = uiState.altitudeText,
                            onClickDone = {
                                onCompletedEditTemperature()
                                coroutineScope.launch {
                                    val snackbarResult = snackbarHostState.showSnackbar(
                                        message = snackBarMessage,
                                        actionLabel = snackBarActionLabel,
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                    when (snackbarResult) {
                                        SnackbarResult.Dismissed -> onDismissedTemperatureSnackBar()
                                        SnackbarResult.ActionPerformed -> undoTemperature()
                                    }
                                }
                            },
                            onClickCancel = onClickCancelTemperature
                        )
                    }
                }
            }
        }
    }

}