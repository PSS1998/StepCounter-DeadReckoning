package com.example.stepcounter.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class StepDetector extends SensorListener {
    private double timestamp;
    static private StepDetector stepDetector;
    private int NumberOfSteps = 0;

    public StepDetector(SensorManager sensorManager) {
        super(sensorManager);
    }

    public static StepDetector getInstance(SensorManager sensorManager) {
        if (stepDetector == null)
            stepDetector = new StepDetector(sensorManager);
        return stepDetector;
    }

    public Sensor createSensor() {
        return super.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }

    public int getNumberOfSteps() {
        return NumberOfSteps;
    }

    public void resetNumberOfSteps() {
        NumberOfSteps = 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null) {
            if (timestamp == 0)
                timestamp = event.timestamp;
            timestamp = event.timestamp;
            NumberOfSteps += 1;
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
