package com.example.stepcounter.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.example.stepcounter.Filter;

public class Magnetometer extends NormalSensorListener {
    private float[] magneticField = new float[3];
    private double timestamp;
    static private Magnetometer magnetometer;

    public Magnetometer(SensorManager sensorManager) {
        super(sensorManager);
    }

    public static Magnetometer getInstance(SensorManager sensorManager) {
        if (magnetometer == null)
            magnetometer = new Magnetometer(sensorManager);
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
//            magneticField = Filter.LPF(magneticField, event.values.clone());
//            System.arraycopy(event.values, 0, magneticField, 0, magneticField.length);
            timestamp = event.timestamp;
        }
    }
}
