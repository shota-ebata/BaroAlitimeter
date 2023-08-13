package com.ebata_shota.baroalitimeter.ui.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.ebata_shota.baroalitimeter.domain.model.content.UserActionEvent
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    uiState: MainViewModel.UiState,
    onClickTemperature: () -> Unit,
    onClickAltitude: () -> Unit,
    onClickCancel: () -> Unit,
    setAltitude: (String) -> Unit,
    setTemperature: (String) -> Unit,
    undoAltitude: () -> Unit,
    undoTemperature: () -> Unit,
    logUserActionEvent: (UserActionEvent) -> Unit,
) {
    val snackbarHostState: SnackbarHostState = remember {
        SnackbarHostState()
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (uiState) {
                is MainViewModel.UiState.Loading -> Unit // FIXME: ローディング中表示があれば実装したい
                is MainViewModel.UiState.ViewerMode -> {
                    ViewerModeContent(
                        pressureText = uiState.pressureText,
                        altitudeText = uiState.altitudeText,
                        temperatureText = uiState.temperatureText,
                        onClickTemperature = {
                            logUserActionEvent(UserActionEvent.EditTemperature)
                            onClickTemperature()
                        },
                        onClickAltitude = {
                            logUserActionEvent(UserActionEvent.EditAltitude)
                            onClickAltitude()
                        },
                        seaLevelPressure = uiState.seaLevelPressureText
                    )
                }

                is MainViewModel.UiState.EditAltitudeMode -> {
                    EditModeAltitudeContent(
                        pressureText = uiState.pressureText,
                        seaLevelPressure = uiState.seaLevelPressureText,
                        defaultAltitudeText = uiState.defaultAltitudeText,
                        temperatureText = uiState.temperatureText,
                        onClickDone = {
                            logUserActionEvent(UserActionEvent.DoneEditAltitude)
                            setAltitude(it) // FIXME: suspendにしたほうがよいか？
                            scope.launch {
                                val snackbarResult = snackbarHostState.showSnackbar(
                                    message = "高度を変更",
                                    actionLabel = "戻す",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                                when(snackbarResult) {
                                    SnackbarResult.Dismissed -> {
                                        logUserActionEvent(UserActionEvent.DismissedUndoAltitude)
                                    }
                                    SnackbarResult.ActionPerformed -> {
                                        logUserActionEvent(UserActionEvent.UndoAltitude)
                                        undoAltitude()
                                    }
                                }
                            }
                        },
                        onClickCancel = {
                            logUserActionEvent(UserActionEvent.CancelEditAltitudeByButton)
                            onClickCancel()
                        }
                    )
                }

                is MainViewModel.UiState.EditTemperatureMode -> {
                    EditModeTemperature(
                        pressureText = uiState.pressureText,
                        seaLevelPressure = uiState.seaLevelPressureText,
                        altitudeText = uiState.altitudeText,
                        defaultTemperatureText = uiState.defaultTemperatureText,
                        onClickDone = {
                            logUserActionEvent(UserActionEvent.DoneEditTemperature)
                            setTemperature(it)
                            scope.launch {
                                val snackbarResult = snackbarHostState.showSnackbar(
                                    message = "気温を変更",
                                    actionLabel = "戻す",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                                when(snackbarResult) {
                                    SnackbarResult.Dismissed -> {
                                        logUserActionEvent(UserActionEvent.DismissedUndoTemperature)
                                    }
                                    SnackbarResult.ActionPerformed -> {
                                        logUserActionEvent(UserActionEvent.UndoTemperature)
                                        undoTemperature()
                                    }
                                }
                            }
                        },
                        onClickCancel = {
                            logUserActionEvent(UserActionEvent.CancelEditTemperatureByButton)
                            onClickCancel()
                        }
                    )
                }
            }
        }
    }
}