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
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface UseCaseModule {
    @Binds
    @Singleton
    fun bindCalcUseCase(useCase: CalcUseCaseImpl): CalcUseCase

    @Binds
    @Singleton
    fun bindContentParamsUseCase(useCase: ContentParamsUseCaseImpl): ContentParamsUseCase

    @Binds
    @Singleton
    fun bindThemeUseCase(useCase: ThemeUseCaseImpl): ThemeUseCase
}