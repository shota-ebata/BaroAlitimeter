package com.ebata_shota.baroalitimeter.di.module

import com.ebata_shota.baroalitimeter.domain.repository.DebugOnlyPrefRepository
import com.ebata_shota.baroalitimeter.infra.repository.DebugOnlyPrefRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DebugOnlyRepositoryModule {

    @Binds
    @Singleton
    fun bindDebugOnlyPrefRepository(repository: DebugOnlyPrefRepositoryImpl): DebugOnlyPrefRepository
}