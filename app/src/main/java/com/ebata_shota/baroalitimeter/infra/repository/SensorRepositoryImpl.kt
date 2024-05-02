package com.ebata_shota.baroalitimeter.infra.repository

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SensorRepositoryImpl
@Inject
constructor(
    sensorManager: SensorManager,
) : SensorRepository {

    // FIXME: SharedFlowもしくはflowに変更
    private val _pressureState: MutableStateFlow<Pressure> = MutableStateFlow(Pressure.Loading)
    override val pressureSensorState: StateFlow<Pressure> = _pressureState.asStateFlow()

    private val _temperatureState: MutableStateFlow<Temperature> = MutableStateFlow(Temperature.Loading)
    override val temperatureSensorState: StateFlow<Temperature> = _temperatureState.asStateFlow()

    override suspend fun getPressureSensor(): Result<Pressure.Success> {
        return Result.runCatching {
            pressureSensorState.first() as Pressure.Success
        }
    }

    init {
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        sensorManager.registerListener(
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.values?.firstOrNull()?.let {
                        _pressureState.value = Pressure.Success(value = it)
                    }
                }

                override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
            },
            pressureSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        val temperatureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        if (temperatureSensor != null) {
            sensorManager.registerListener(
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        event?.values?.firstOrNull()?.let {
                            _temperatureState.value = Temperature.Success(value = it)
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                },
                temperatureSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        } else {
            _temperatureState.value = Temperature.HasNotSensor
        }
    }
}
