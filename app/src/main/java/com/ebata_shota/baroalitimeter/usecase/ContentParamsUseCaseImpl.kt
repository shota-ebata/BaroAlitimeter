package com.ebata_shota.baroalitimeter.usecase

import com.ebata_shota.baroalitimeter.domain.model.ContentParams
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
        sensorRepository.pressureFlow,
        prefRepository.preferencesFlow
    ) { pressure, preferencesModel ->
        val temperature = preferencesModel.temperature
        val seaLevelPressure = preferencesModel.seaLevelPressure
        emit(
            ContentParams(
                pressure = pressure,
                temperature = temperature,
                seaLevelPressure = seaLevelPressure,
                altitude = calcUseCase.calcAltitude(
                    pressure = pressure,
                    temperature = temperature,
                    seaLevelPressure = seaLevelPressure
                )
            )
        )
    }

    override suspend fun getTemperature(): Float {
        return prefRepository.getTemperature()
    }

    override suspend fun setTemperature(value: Float) {
        prefRepository.setTemperature(value)
    }

    override suspend fun getAltitude(): Float {
        val temperature = prefRepository.getTemperature()
        val pressure = sensorRepository.getPressure()
        val seaLevelPressure = prefRepository.getSeaLevelPressure()
        return calcUseCase.calcAltitude(
            temperature = temperature,
            pressure = pressure,
            seaLevelPressure = seaLevelPressure
        )
    }

    override suspend fun setAltitude(altitude: Float) {
        val temperature = prefRepository.getTemperature()
        val pressure = sensorRepository.getPressure()
        val newSeaLevelPressure = calcUseCase.calcSeaLevelPressure(
            pressure = pressure,
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