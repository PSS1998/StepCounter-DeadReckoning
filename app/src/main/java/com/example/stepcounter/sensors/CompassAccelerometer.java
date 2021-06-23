package com.example.stepcounter.sensors;

import android.hardware.SensorManager;

public class CompassAccelerometer extends Accelerometer {
    private static CompassAccelerometer compassAccelerometer;

    public CompassAccelerometer(SensorManager sensorManager) {
        super(sensorManager);
    }

    public static CompassAccelerometer getInstance(SensorManager sensorManager) {
        if (compassAccelerometer == null){
            compassAccelerometer = new CompassAccelerometer(sensorManager);
        }
        return compassAccelerometer;
    }

}
