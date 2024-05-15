package com.ebata_shota.baroalitimeter.di.module

import com.ebata_shota.baroalitimeter.domain.repository.CalcRepository
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import com.ebata_shota.baroalitimeter.infra.repository.CalcRepositoryImpl
import com.ebata_shota.baroalitimeter.infra.repository.DebugBuildSensorRepository
import com.ebata_shota.baroalitimeter.infra.repository.PrefRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    /**
     * SensorRepositoryImpl → DebugBuildSensorRepository
     */
    @Binds
    @Singleton
    fun bindSensorRepository(repository: DebugBuildSensorRepository): SensorRepository

    @Binds
    @Singleton
    fun bindPrefRepository(repository: PrefRepositoryImpl): PrefRepository

    @Binds
    @Singleton
    fun bindCalcRepository(repository: CalcRepositoryImpl): CalcRepository
}
