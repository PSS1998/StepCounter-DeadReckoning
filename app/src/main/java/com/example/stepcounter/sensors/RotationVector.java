package com.example.stepcounter.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.example.stepcounter.LocalDirection;

public class RotationVector extends SensorListener {
    private double timestamp;
//    private double gyroscopeTimestamp;
    static private RotationVector rotationVector;

    private static float[] rotationMatrix = new float[16];
    private LocalDirection localDirection;
    private float[] orientationVals = {0, 0, 0};

    public RotationVector(SensorManager sensorManager, LocalDirection localDirection) {
        super(sensorManager);
        this.localDirection = localDirection;
    }

    public static RotationVector getInstance(SensorManager sensorManager, LocalDirection localDirection) {
        if (rotationVector == null)
            rotationVector = new RotationVector(sensorManager,localDirection);
        return rotationVector;
    }

    public Sensor createSensor() {
        return super.sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
    }

    public double getTimestamp() {
        return timestamp;
    }

    public float[] getOrientationValues() {
        return orientationVals;
    }



//    public double getGyroscopeTimestamp() {
//        return gyroscopeTimestamp;
//    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Convert the rotation-vector to a 4x4 matrix.
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientationVals);

        orientationVals[0] -= LocalDirection.initialHeadingBias;
        orientationVals[0] = (float)((orientationVals[0] < 0) ? (orientationVals[0] + (2.0 * Math.PI)) : orientationVals[0]);

        if (timestamp == 0)
            timestamp = event.timestamp;
//        gyroscopeTimestamp = (event.timestamp - timestamp) * LocalDirection.NS2S;
        timestamp = event.timestamp;
        localDirection.updateOnRotationVectorChanged();
    }
}