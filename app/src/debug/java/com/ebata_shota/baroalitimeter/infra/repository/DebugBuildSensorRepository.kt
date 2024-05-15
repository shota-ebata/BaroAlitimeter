package com.ebata_shota.baroalitimeter.infra.repository

import com.ebata_shota.baroalitimeter.domain.content.DummySensor
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.repository.DebugOnlyPrefRepository
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class DebugBuildSensorRepository
@Inject
constructor(
    private val debugOnlyPrefRepository: DebugOnlyPrefRepository,
    private val repository: SensorRepositoryImpl,
) : SensorRepository {


    override val pressureFlow: Flow<Float> = combine(
        debugOnlyPrefRepository.dummySensor,
        repository.pressureFlow
    ) { dummySensor, pressure ->
        when (dummySensor) {
            DummySensor.OFF -> pressure
            DummySensor.ON -> 1000.0f
        }
    }

    override suspend fun getPressure(): Float {
        return when (debugOnlyPrefRepository.getDummySensor()) {
            DummySensor.OFF -> repository.getPressure()
            DummySensor.ON -> 1000.0f
        }
    }

    override val temperatureSensorState: Flow<Temperature> = combine(
        debugOnlyPrefRepository.dummySensor,
        repository.temperatureSensorState
    ) { dummySensor, temperatureSensorState ->
        when (dummySensor) {
            DummySensor.OFF -> temperatureSensorState
            DummySensor.ON -> Temperature.Success(value = 24.0f)
        }
    }
}