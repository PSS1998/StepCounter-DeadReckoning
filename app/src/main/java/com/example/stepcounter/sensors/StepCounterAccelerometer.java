package com.example.stepcounter.sensors;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.example.stepcounter.Filter;
import com.example.stepcounter.services.StepCounterService;

public class StepCounterAccelerometer extends Accelerometer {
    private StepCounterService stepCounterService;
    private static StepCounterAccelerometer stepCounterAccelerometer;
    private StepCounterAccelerometer(SensorManager sensorManager, StepCounterService stepCounterService) {
        super(sensorManager);
        this.stepCounterService = stepCounterService;
    }

    public static StepCounterAccelerometer getInstance(SensorManager sensorManager, StepCounterService stepCounterService) {
        if (stepCounterAccelerometer == null)
            stepCounterAccelerometer = new StepCounterAccelerometer(sensorManager, stepCounterService);
        return stepCounterAccelerometer;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        super.onSensorChanged(event);
        stepCounterService.updateOnSensorChanged();
    }
}
