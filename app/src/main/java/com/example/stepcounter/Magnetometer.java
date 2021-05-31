package com.example.stepcounter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class Magnetometer extends com.example.stepcounter.GameSensorListener {
    private float[] magneticField = new float[3];
    private double timestamp;
    static private com.example.stepcounter.Magnetometer magnetometer;

    public Magnetometer(SensorManager sensorManager) {
        super(sensorManager);
    }

    public static com.example.stepcounter.Magnetometer getInstance(SensorManager sensorManager) {
        if (magnetometer == null)
            magnetometer = new com.example.stepcounter.Magnetometer(sensorManager);
        return magnetometer;
    }

    public Sensor createSensor() {
        return super.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public float[] getMagnetic_field() {
        return magneticField;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null) {
            if (timestamp == 0)
                timestamp = event.timestamp;
            magneticField = Filter.LPF(event.values.clone(), magneticField);
            magneticField = Filter.LPF(magneticField, event.values.clone());
//            System.arraycopy(event.values, 0, magneticField, 0, magneticField.length);
            timestamp = event.timestamp;
        }
    }
}
