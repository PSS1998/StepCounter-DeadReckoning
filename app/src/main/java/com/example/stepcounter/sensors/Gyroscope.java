package com.example.stepcounter.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.example.stepcounter.observers.Publisher;
import com.example.stepcounter.observers.Subscriber;

import java.util.ArrayList;

public class Gyroscope extends SensorListener implements Publisher {
    public static final double NS2S = 1.0f / 1000000000.0f;

    private static final ArrayList<Subscriber> subscribers = new ArrayList<>();
    private static Gyroscope gyroscope;

    private long timestamp;
    float[] gyroscopeValues = new float[3];
    private double gyroscopeTimestamp;

    private Gyroscope(SensorManager sensorManager) {
        super(sensorManager);
    }

    public static Gyroscope getInstance(SensorManager sensorManager) {
        if (gyroscope == null)
            gyroscope = new Gyroscope(sensorManager);
        return gyroscope;
    }

    public Sensor createSensor() {
        return super.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public float[] getOrientationValues() {
        return gyroscopeValues;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getGyroscopeTimestamp() {
        return gyroscopeTimestamp;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null) {
            if (timestamp == 0)
                timestamp = event.timestamp;
            gyroscopeTimestamp = (event.timestamp - timestamp) * Gyroscope.NS2S;
            timestamp = event.timestamp;
            gyroscopeValues[0] = event.values[0];
            gyroscopeValues[1] = event.values[1];
            gyroscopeValues[2] = event.values[2];
            this.publish();
        }
    }
    public void register(Subscriber subscriber) {
        Gyroscope.subscribers.add(subscriber);
    }

    @Override
    public void publish() {
        for (Subscriber subscriber: Gyroscope.subscribers)
            subscriber.update();
    }
}
