package com.example.stepcounter.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

abstract public class NormalSensorListener implements SensorEventListener {
    protected SensorManager sensorManager;

    public NormalSensorListener(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    abstract public Sensor createSensor();

    @Override
    abstract public void onSensorChanged(SensorEvent event);

    public void start() {
        Sensor sensor = createSensor();
        this.sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        this.sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
