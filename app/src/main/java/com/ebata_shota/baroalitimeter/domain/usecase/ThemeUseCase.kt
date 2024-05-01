package com.ebata_shota.baroalitimeter.domain.usecase

import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeUseCase {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(value: ThemeMode)
}