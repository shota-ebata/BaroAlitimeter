package com.ebata_shota.baroalitimeter.usecase.spy

import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.usecase.ThemeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SpyThemeUseCase : ThemeUseCase {

    private val _themeMode = MutableSharedFlow<ThemeMode>()
    override val themeMode: Flow<ThemeMode> = _themeMode.asSharedFlow()
    suspend fun emitThemeMode(value: ThemeMode) {
        _themeMode.emit(value)
    }

    override suspend fun setThemeMode(value: ThemeMode) {
        _themeMode.emit(value)
    }
}