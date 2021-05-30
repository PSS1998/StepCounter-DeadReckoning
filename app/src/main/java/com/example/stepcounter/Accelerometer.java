package com.example.stepcounter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class Accelerometer extends GameSensorListener {
    private float[] acceleration = new float[3];
    private double timestamp;
    static private Accelerometer accelerometer;

    private Accelerometer(SensorManager sensorManager) {
        super(sensorManager);
    }

    public static Accelerometer getInstance(SensorManager sensorManager) {
        if (accelerometer == null)
            accelerometer = new Accelerometer(sensorManager);
        return accelerometer;
    }

    public Sensor createSensor() {
        return super.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
//            System.arraycopy(event.values, 0, acceleration, 0, acceleration.length);
            timestamp = event.timestamp;
        }
    }
}
