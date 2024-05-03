package com.ebata_shota.baroalitimeter.infra.repository

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ebata_shota.baroalitimeter.domain.model.Temperature
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SensorRepositoryImpl
@Inject
constructor(
    sensorManager: SensorManager,
) : SensorRepository {

    override val pressureFlow: Flow<Float> = callbackFlow {
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.firstOrNull()?.let {
                    trySend(it)
                }
            }

            override fun onAccuracyChanged(
                p0: Sensor?,
                p1: Int
            ) = Unit
        }
        sensorManager.registerListener(
            listener,
            pressureSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }


    override val temperatureSensorState: Flow<Temperature> = callbackFlow {
        val temperatureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        if (temperatureSensor != null) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.values?.firstOrNull()?.let {
                        trySend(Temperature.Success(value = it))
                    }
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int
                ) = Unit
            }
            sensorManager.registerListener(
                listener,
                temperatureSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            awaitClose {
                sensorManager.unregisterListener(listener)
            }
        } else {
            trySend(Temperature.HasNotSensor)
        }
    }

    override suspend fun getPressure(): Float {
        return pressureFlow.first()
    }
}
