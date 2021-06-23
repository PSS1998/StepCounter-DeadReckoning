package com.example.stepcounter.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

abstract public class SensorListener implements SensorEventListener {
    protected SensorManager sensorManager;

    public SensorListener(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    abstract public Sensor createSensor();

    @Override
    abstract public void onSensorChanged(SensorEvent event);

    public void start(int sensorDelay) {
        Sensor sensor = createSensor();
        this.sensorManager.registerListener(this, sensor, sensorDelay);
    }

    public void stop() {
        this.sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
