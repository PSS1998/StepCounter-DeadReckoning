package com.example.stepcounter.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class Gravity extends SensorListener {
    static private Gravity gravity;
    private double gradient = 0;
    private double timestamp;
    public static float NS2US = 1.0f / 1000.0f;
    public static int READ_SENSOR_RATE = 20;


    private Gravity(SensorManager sensorManager) {
        super(sensorManager);
    }
    public static Gravity getInstance(SensorManager sensorManager) {
        if (gravity == null)
            gravity = new Gravity(sensorManager);
        return gravity;
    }

    @Override
    public Sensor createSensor() {
        return super.sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent != null) {
            float dT = (float) ((sensorEvent.timestamp - timestamp) * NS2US);
            if (dT > READ_SENSOR_RATE) {
                double gravityX = sensorEvent.values[0];
                double gravityY = sensorEvent.values[1];
                this.gradient = Math.atan2(gravityY, gravityX);
                System.out.println("this.gradient: " + this.gradient);
            }
            timestamp = sensorEvent.timestamp;
        }
    }

    @Override
    public float[] getOrientationValues() {
        return new float[0];
    }

    @Override
    public long getTimestamp() {
        return 0;
    }

}
