package com.ebata_shota.baroalitimeter.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.ui.content.MainContent
import com.ebata_shota.baroalitimeter.ui.content.RadioListContent
import com.ebata_shota.baroalitimeter.ui.model.ThemeModeRadioOption
import com.ebata_shota.baroalitimeter.ui.parts.MainTopAppBar
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
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
        // FIXME: Composeでcoroutine使うのがなぁ・・・なるべく使いたくないが・・・しかたないよなぁ
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

    when (val currentUiState = uiState) {
        MainViewModel.MainUiState.Uninitialized -> Unit // 表示できる状態ではないので何も処理しない
        is MainViewModel.MainUiState.Initialized -> {
            BaroAlitimeterTheme(
                darkTheme = when (currentUiState.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
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
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        sheetState = themeModalBottomSheetState,
                        sheetContent = {
                            RadioListContent(
                                radioOptions = ThemeModeRadioOption.values(),
                                selectedOption = ThemeModeRadioOption.of(currentUiState.themeMode),
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
                            modifier = Modifier
                                .fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MainContent(
                                uiState = currentUiState.contentUiState,
                                /**
                                 * FIXME: より良い方法があれば改善する。
                                 *  objectを毎回インスタンス生成するの馬鹿馬鹿しいのでViewModelにinterfaceを実装してみた。
                                 *  interfaceとはいえ他のComposable関数にViewModelを渡すのは微妙か？
                                 *  一応公式ドキュメントには、以下のように記載されているが・・・
                                 *  参考：https://developer.android.com/jetpack/compose/state-hoisting?hl=ja
                                 *    注: ViewModel インスタンスを他のコンポーザブルに渡さないでください。詳細については、アーキテクチャ状態ホルダーのドキュメントをご覧ください。
                                 *    参考：https://developer.android.com/topic/architecture/ui-layer/stateholders?hl=ja#business-logic
                                 *      警告: ViewModel インスタンスを他のコンポーズ可能な関数に渡さないでください。
                                 *      そのようにすると、コンポーズ可能な関数と ViewModel 型が結合されるため、再利用性が低くなり、テストとプレビューが難しくなります。
                                 *      また、ViewModel インスタンスを管理する明確な SSOT（信頼できる単一の情報源）がなくなります。
                                 *      ViewModel を渡すと、複数のコンポーザブルが ViewModel 関数を呼び出して状態を変更できるようになり、バグのデバッグが難しくなります。
                                 *      代わりに、UDF ベスト プラクティスに沿って、必要な状態のみを渡します。同様に、ViewModel のコンポーザブルの SSOT に達するまで、伝播イベントを渡します。
                                 *      これは、イベントを処理し、対応する ViewModel メソッドを呼び出す SSOT です。
                                 *  つまり、ViewModelのインスタンスをinterface経由で渡せば、上記の問題は起きないのでは？
                                 *  と言えそうなのでやってみた。
                                 */
                                temperaturePartsEvents = viewModel,
                                altitudePartsEvents = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
