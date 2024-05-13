package com.ebata_shota.baroalitimeter.viewmodel

import androidx.annotation.StringRes
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.domain.extensions.logUserActionEvent
import com.ebata_shota.baroalitimeter.domain.model.ContentParams
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.model.content.UserActionEvent
import com.ebata_shota.baroalitimeter.domain.usecase.ContentParamsUseCase
import com.ebata_shota.baroalitimeter.domain.usecase.ThemeUseCase
import com.ebata_shota.baroalitimeter.ui.parts.AltitudePartsEvents
import com.ebata_shota.baroalitimeter.ui.parts.TemperaturePartsEvents
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val contentParamsUseCase: ContentParamsUseCase,
    private val themeUseCase: ThemeUseCase,
    private val firebaseAnalytics: FirebaseAnalytics,
) : ViewModel() {

    sealed interface MainUiState {

        object Uninitialized : MainUiState

        data class Initialized(
            val themeMode: ThemeMode,
            val contentUiState: ContentUiState,
        ) : MainUiState
    }

    data class ContentUiState(
        val pressureText: String,
        val seaLevelPressureText: String,
        val temperatureUiState: TemperatureUiState,
        val altitudeUiState: AltitudeUiState,
    ) {

        sealed interface TemperatureUiState {

            data class ViewerMode(
                val temperatureText: String,
            ) : TemperatureUiState

            data class EditMode(
                val temperatureTextFieldValue: TextFieldValue,
            ) : TemperatureUiState
        }

        sealed interface AltitudeUiState {

            data class ViewerMode(
                val altitudeText: String,
            ) : AltitudeUiState

            data class EditMode(
                val altitudeTextFieldValue: TextFieldValue,
            ) : AltitudeUiState
        }
    }

    enum class ShowUndoSnackBarEvent(
        @StringRes val snackBarText: Int,
    ) {
        Temperature(R.string.snack_bar_message_change_temperature),
        Altitude(R.string.snack_bar_message_change_altitude)
    }

    enum class Mode {
        Viewer,
        EditTemperature,
        EditAltitude
    }

    private val _modeState: MutableStateFlow<Mode> = MutableStateFlow(Mode.Viewer)
    val modeState: StateFlow<Mode> = _modeState.asStateFlow()

    private val _temperatureTextFieldValue = MutableStateFlow(TextFieldValue())
    private val temperatureTextFieldValue = _temperatureTextFieldValue.asStateFlow()

    private val _altitudeTextFieldValue = MutableStateFlow(TextFieldValue())
    private val altitudeTextFieldValue = _altitudeTextFieldValue.asStateFlow()

    val mainUiState: StateFlow<MainUiState> = combine(
        modeState,
        temperatureTextFieldValue,
        altitudeTextFieldValue,
        contentParamsUseCase.contentParamsFlow,
        themeUseCase.themeMode
    ) {
            mode: Mode,
            temperatureTextFieldValue: TextFieldValue,
            altitudeTextFieldValue: TextFieldValue,
            contentParams: ContentParams,
            themeMode: ThemeMode,
        ->
        val pressureText = contentParams.pressure.formattedString(1)
        val seaLevelPressureText = contentParams.seaLevelPressure.formattedString(2)
        val temperatureText = contentParams.temperature.formattedString(0)
        val altitudeText = contentParams.altitude.formattedString(0)

        MainUiState.Initialized(
            themeMode = themeMode,
            contentUiState = ContentUiState(
                pressureText = pressureText,
                seaLevelPressureText = seaLevelPressureText,
                temperatureUiState = if (mode == Mode.EditTemperature) {
                    ContentUiState.TemperatureUiState.EditMode(temperatureTextFieldValue)
                } else {
                    ContentUiState.TemperatureUiState.ViewerMode(temperatureText)
                },
                altitudeUiState = if (mode == Mode.EditAltitude) {
                    ContentUiState.AltitudeUiState.EditMode(altitudeTextFieldValue)
                } else {
                    ContentUiState.AltitudeUiState.ViewerMode(altitudeText)
                }
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = MainUiState.Uninitialized
    )

    fun onSelectedThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            themeUseCase.setThemeMode(themeMode)
        }
    }

    private val _showUndoSnackBarEvent = MutableSharedFlow<ShowUndoSnackBarEvent>()
    val showUndoSnackBarEvent: SharedFlow<ShowUndoSnackBarEvent> = _showUndoSnackBarEvent.asSharedFlow()

    fun onClickTemperature() {
        changeModeToEditTemperature()
    }

    private fun changeModeToEditTemperature() {
        viewModelScope.launch {
            _modeState.update { mode ->
                if (mode != Mode.Viewer) {
                    return@launch
                }
                val temperature = contentParamsUseCase.getTemperature()
                val temperatureText = temperature.formattedString(0)
                updateTemperatureTextFieldValue(
                    TextFieldValue(
                        text = temperatureText,
                        selection = TextRange(temperatureText.length)
                    )
                )
                logUserActionEvent(UserActionEvent.EditTemperature)
                Mode.EditTemperature
            }
        }
    }

    fun onClickAltitude() {
        changeModeToEditAltitude()
    }

    private fun changeModeToEditAltitude() {
        viewModelScope.launch {
            _modeState.update { mode ->
                if (mode != Mode.Viewer) {
                    return@launch
                }
                val altitude = contentParamsUseCase.getAltitude()
                val altitudeText = altitude.formattedString(0)
                updateAltitudeTextFieldValue(
                    TextFieldValue(
                        text = altitudeText,
                        selection = TextRange(altitudeText.length)
                    )
                )
                logUserActionEvent(UserActionEvent.EditAltitude)
                Mode.EditAltitude
            }
        }
    }

    fun onChangeTemperatureTextFieldValue(textFieldValue: TextFieldValue) {
        updateTemperatureTextFieldValue(textFieldValue)
    }

    private fun updateTemperatureTextFieldValue(textFieldValue: TextFieldValue) {
        _temperatureTextFieldValue.update {
            textFieldValue
        }
    }

    fun onChangeAltitudeTextFieldValue(textFieldValue: TextFieldValue) {
        updateAltitudeTextFieldValue(textFieldValue)
    }

    private fun updateAltitudeTextFieldValue(textFieldValue: TextFieldValue) {
        _altitudeTextFieldValue.update {
            textFieldValue
        }
    }

    fun onClickCancelEditTemperature() {
        cancelEditTemperature()
    }

    private fun cancelEditTemperature() {
        logUserActionEvent(UserActionEvent.CancelEditTemperatureByButton)
        changeModeToViewer()
    }

    fun onClickCancelAltitude() {
        cancelEditAltitude()
    }

    private fun cancelEditAltitude() {
        logUserActionEvent(UserActionEvent.CancelEditAltitudeByButton)
        changeModeToViewer()
    }

    fun onBackPressedCallback() {
        when (modeState.value) {
            Mode.Viewer -> Unit // 発生しないケース
            Mode.EditTemperature -> logUserActionEvent(UserActionEvent.CancelEditTemperatureByOnBackPressedCallback)
            Mode.EditAltitude -> logUserActionEvent(UserActionEvent.CancelEditAltitudeByOnBackPressedCallback)
        }
        changeModeToViewer()
    }

    private fun changeModeToViewer() {
        _modeState.update {
            Mode.Viewer
        }
    }

    fun onClickDoneEditTemperature() {
        onCompletedEditTemperature()
        showTemperatureUndoSnackBar()
    }

    private fun onCompletedEditTemperature() {
        viewModelScope.launch {
            logUserActionEvent(UserActionEvent.DoneEditTemperature)

            // UiStateが初期化されている前提
            val currentMainUiState = mainUiState.value
            if (currentMainUiState !is MainUiState.Initialized) {
                return@launch
            }

            // Editモードじゃないなら無視
            val temperatureUiState = currentMainUiState.contentUiState.temperatureUiState
            if (temperatureUiState !is ContentUiState.TemperatureUiState.EditMode) {
                return@launch
            }

            try {
                // FIXME: 入力制限をしていないので、Floatの範囲外に入ると問題になる
                val temperature = temperatureUiState.temperatureTextFieldValue.text.toFloat()
                contentParamsUseCase.setTemperature(temperature)
            } catch (e: NumberFormatException) {
                // 変換に失敗したら、特に何もない
            } finally {
                // 最終的に編集モードを終了する
                changeModeToViewer()
            }
        }
    }

    private fun showTemperatureUndoSnackBar() {
        viewModelScope.launch {
            _showUndoSnackBarEvent.emit(ShowUndoSnackBarEvent.Temperature)
        }
    }

    fun onClickDoneEditAltitude() {
        onCompletedEditAltitude()
        showAltitudeUndoSnackbar()
    }

    private fun onCompletedEditAltitude() {
        viewModelScope.launch {
            logUserActionEvent(UserActionEvent.DoneEditAltitude)

            // UiStateが初期化されている前提
            val currentMainUiState = mainUiState.value
            if (currentMainUiState !is MainUiState.Initialized) {
                return@launch
            }

            // EditAltitudeモードじゃないなら無視
            val altitudeUiState = currentMainUiState.contentUiState.altitudeUiState
            if (altitudeUiState !is ContentUiState.AltitudeUiState.EditMode) {
                return@launch
            }

            try {
                // FIXME: 入力制限をしていないので、Floatの範囲外に入ると問題になる
                val altitude = altitudeUiState.altitudeTextFieldValue.text.toFloat()
                contentParamsUseCase.setAltitude(altitude)
            } catch (e: NumberFormatException) {
                // 変換に失敗したら、特に何もない
            } finally {
                // 最終的に編集モードを終了する
                changeModeToViewer()
            }
        }
    }

    private fun showAltitudeUndoSnackbar() {
        viewModelScope.launch {
            _showUndoSnackBarEvent.emit(ShowUndoSnackBarEvent.Altitude)
        }
    }

    fun onDismissedSnackBar(event: ShowUndoSnackBarEvent) {
        when (event) {
            ShowUndoSnackBarEvent.Temperature -> onDismissedTemperatureSnackBar()
            ShowUndoSnackBarEvent.Altitude -> onDismissedAltitudeSnackbar()
        }
    }

    fun onActionPerformedSnackBar(event: ShowUndoSnackBarEvent) {
        when (event) {
            ShowUndoSnackBarEvent.Temperature -> undoTemperature()
            ShowUndoSnackBarEvent.Altitude -> undoAltitude()
        }
    }

    private fun undoTemperature() {
        logUserActionEvent(UserActionEvent.UndoTemperature)
        viewModelScope.launch {
            contentParamsUseCase.undoTemperature()
        }
    }

    private fun undoAltitude() {
        logUserActionEvent(UserActionEvent.UndoAltitude)
        viewModelScope.launch {
            contentParamsUseCase.undoSeaLevelPressure()
        }
    }

    private fun onDismissedAltitudeSnackbar() {
        logUserActionEvent(UserActionEvent.DismissedUndoAltitude)
    }

    private fun onDismissedTemperatureSnackBar() {
        logUserActionEvent(UserActionEvent.DismissedUndoTemperature)
    }

    private fun logUserActionEvent(userActionEvent: UserActionEvent) {
        firebaseAnalytics.logUserActionEvent(userActionEvent)
    }

    // FIXME: UseCaseなどに切り出す
    private fun Float?.formattedString(
        fractionDigits: Int,
        usesGroupingSeparator: Boolean = false,
    ): String {
        // nullの場合は空文字
        if (this == null) return ""
        var format = "%.${fractionDigits}f"
        if (usesGroupingSeparator) {
            format = "%,.${fractionDigits}f"
        }
        return format.format(this)
    }
}
