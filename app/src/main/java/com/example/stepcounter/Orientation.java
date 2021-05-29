package com.example.stepcounter;

import android.hardware.SensorManager;

public class Orientation {
    private float[] orientationAngles = new float[3];
    private float[] rotationMatrix = new float[9];
    private SensorManager sensorManager;
    private static com.example.stepcounter.Orientation orientation;
    private Orientation(SensorManager sensorManager){
        this.sensorManager = sensorManager;
        Accelerometer.getInstance(sensorManager).start();
        Magnetometer.getInstance(sensorManager).start();

    }

    public static com.example.stepcounter.Orientation getInstance(SensorManager sensorManager) {
        if (orientation == null)
            orientation = new com.example.stepcounter.Orientation(sensorManager);
        return orientation;
    }

    public void updateOrientationAngles() {

        SensorManager.getRotationMatrix(rotationMatrix, null,
                Accelerometer.getInstance(sensorManager).getAcceleration(), Magnetometer.getInstance(sensorManager).getMagnetic_field());
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

    }

    public float[] getOrientationAngles() {
        return orientationAngles;
    }
}
