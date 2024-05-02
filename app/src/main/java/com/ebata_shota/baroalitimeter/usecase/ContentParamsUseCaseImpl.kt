package com.ebata_shota.baroalitimeter.usecase

import com.ebata_shota.baroalitimeter.domain.model.ContentParams
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.repository.PrefRepository
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import com.ebata_shota.baroalitimeter.domain.usecase.CalcUseCase
import com.ebata_shota.baroalitimeter.domain.usecase.ContentParamsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import javax.inject.Inject

class ContentParamsUseCaseImpl
@Inject
constructor(
    private val sensorRepository: SensorRepository,
    private val prefRepository: PrefRepository,
    private val calcUseCase: CalcUseCase,
) : ContentParamsUseCase {

    override val contentParamsFlow: Flow<ContentParams> = combineTransform(
        sensorRepository.pressureSensorState,
        prefRepository.preferencesFlow
    ) { pressure, preferencesModel ->
        if (pressure !is Pressure.Success) {
            // 準備が整っていないので何も発火しない
            return@combineTransform
        }
        val pressureValue = pressure.value
        val temperature = preferencesModel.temperature
        val seaLevelPressure = preferencesModel.seaLevelPressure
        emit(
            ContentParams(
                pressure = pressureValue,
                temperature = temperature,
                seaLevelPressure = seaLevelPressure,
                altitude = calcUseCase.calcAltitude(
                    pressure = pressureValue,
                    temperature = temperature,
                    seaLevelPressure = seaLevelPressure
                )
            )
        )
    }

    override suspend fun getTemperature(): Result<Float> {
        return prefRepository.getTemperature()
    }

    override suspend fun setTemperature(value: Float) {
        prefRepository.setTemperature(value)
    }

    override suspend fun getAltitude(): Result<Float> = runCatching {
        val pressureSensorState = sensorRepository.getPressureSensor().getOrThrow()
        val temperature = prefRepository.getTemperature().getOrThrow()
        val seaLevelPressure = prefRepository.getSeaLevelPressure().getOrThrow()
        calcUseCase.calcAltitude(
            pressure = pressureSensorState.value,
            temperature = temperature,
            seaLevelPressure = seaLevelPressure
        )
    }

    override suspend fun setAltitude(altitude: Float) {
        val temperature = prefRepository.getTemperature().getOrThrow()
        val pressureState = sensorRepository.getPressureSensor().getOrThrow()
        val newSeaLevelPressure = calcUseCase.calcSeaLevelPressure(
            pressure = pressureState.value,
            temperature = temperature,
            altitude = altitude
        )
        prefRepository.setSeaLevelPressure(newSeaLevelPressure)
    }

    override suspend fun undoTemperature() {
        prefRepository.undoTemperature()
    }

    override suspend fun undoSeaLevelPressure() {
        prefRepository.undoSeaLevelPressure()
    }
}