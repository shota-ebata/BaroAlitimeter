package com.ebata_shota.baroalitimeter.infra.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.ebata_shota.baroalitimeter.domain.model.Pressure
import com.ebata_shota.baroalitimeter.domain.repository.SensorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SensorRepositoryImpl
@Inject
constructor(
    context: Context
) : SensorRepository {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _pressureState: MutableStateFlow<Pressure> = MutableStateFlow(Pressure())
    override val pressureState: StateFlow<Pressure> = _pressureState.asStateFlow()

    init {
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        sensorManager.registerListener(
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    _pressureState.value = pressureState.value.copy(pressure = event?.values?.firstOrNull())
                }

                override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
            },
            pressureSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }
}