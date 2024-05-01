package com.ebata_shota.baroalitimeter.usecase

import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.domain.usecase.ThemeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeUseCaseImpl
@Inject
constructor(
    private val prefRepository: PrefRepository,
) : ThemeUseCase {
    override val themeMode: Flow<ThemeMode> = prefRepository.preferencesFlow
        .map { it.themeMode }
        .distinctUntilChanged()

    override suspend fun setThemeMode(value: ThemeMode) {
        prefRepository.setThemeMode(value)
    }
}