package com.ebata_shota.baroalitimeter.di.module

import com.ebata_shota.baroalitimeter.domain.usecase.CalcUseCase
import com.ebata_shota.baroalitimeter.domain.usecase.ContentParamsUseCase
import com.ebata_shota.baroalitimeter.domain.usecase.ThemeUseCase
import com.ebata_shota.baroalitimeter.usecase.CalcUseCaseImpl
import com.ebata_shota.baroalitimeter.usecase.ContentParamsUseCaseImpl
import com.ebata_shota.baroalitimeter.usecase.ThemeUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface UseCaseModule {

    @Binds
    fun bindCalcUseCase(useCase: CalcUseCaseImpl): CalcUseCase

    @Binds
    fun bindContentParamsUseCase(useCase: ContentParamsUseCaseImpl): ContentParamsUseCase

    @Binds
    fun bindThemeUseCase(useCase: ThemeUseCaseImpl): ThemeUseCase
}