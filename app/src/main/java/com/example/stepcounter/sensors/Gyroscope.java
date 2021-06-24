package com.example.stepcounter.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.example.stepcounter.LocalDirection;

public class Gyroscope extends SensorListener {
    private double timestamp;

    float[] gyroscopeValues;
    static private Gyroscope gyroscope;
    private double gyroscopeTimestamp;
    private LocalDirection localDirection;

    private Gyroscope(SensorManager sensorManager, LocalDirection localDirection) {
        super(sensorManager);
        this.localDirection = localDirection;
    }

    public static Gyroscope getInstance(SensorManager sensorManager, LocalDirection localDirection) {
        if (gyroscope == null)
            gyroscope = new Gyroscope(sensorManager,localDirection);
        return gyroscope;
    }

    public Sensor createSensor() {
        return super.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public float[] getGyroscopeValues() {
        return gyroscopeValues;
    }

    public double getTimestamp() {
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
            gyroscopeTimestamp = (event.timestamp - timestamp) * LocalDirection.NS2S;
            timestamp = event.timestamp;
            gyroscopeValues[0] = event.values[0];
            gyroscopeValues[1] = event.values[1];
            gyroscopeValues[2] = event.values[2];
            this.localDirection.updateOnGyroscopeChanged();

        }

    }
}
