package com.example.stepcounter.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.example.stepcounter.Filter;

public class Accelerometer extends SensorListener {
    private float[] acceleration = new float[3];
    private float[] rawAcceleration = new float[3];
    private double timestamp;

    public Accelerometer(SensorManager sensorManager) {
        super(sensorManager);
    }

    public Sensor createSensor() {
        return super.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public float[] getRawAcceleration() {
        return rawAcceleration;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public float[] getAcceleration() {
        return acceleration;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null) {
            if (timestamp == 0)
                timestamp = event.timestamp;
            acceleration = Filter.LPF(event.values.clone(), acceleration);
            rawAcceleration = acceleration;
            timestamp = event.timestamp;
//            StepCounterService.
        }
    }
}
