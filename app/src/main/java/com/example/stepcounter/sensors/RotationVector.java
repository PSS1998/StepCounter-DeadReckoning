package com.example.stepcounter.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.example.stepcounter.observers.Publisher;
import com.example.stepcounter.observers.Subscriber;

import java.util.ArrayList;

public class RotationVector extends SensorListener implements Publisher {
    static private RotationVector rotationVector;
    private static final ArrayList<Subscriber> subscribers = new ArrayList<>();


    private long timestamp;
    private static float[] rotationMatrix = new float[16];
    private float[] orientationVals = {0, 0, 0};

    public RotationVector(SensorManager sensorManager) {
        super(sensorManager);
    }

    public static RotationVector getInstance(SensorManager sensorManager) {
        if (rotationVector == null)
            rotationVector = new RotationVector(sensorManager);
        return rotationVector;
    }

    public Sensor createSensor() {
        return super.sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float[] getOrientationValues() {
        return orientationVals;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // Convert the rotation-vector to a 4x4 matrix.
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientationVals);
//        orientationVals[0] -= LocalDirection.initialHeadingBias;
        orientationVals[0] = (float)((orientationVals[0] < 0) ? (orientationVals[0] + (2.0 * Math.PI)) : orientationVals[0]);
        if (timestamp == 0)
            timestamp = event.timestamp;
        timestamp = event.timestamp;
        this.publish();
//        localDirection.updateOnRotationVectorChanged();
    }

    public void register(Subscriber subscriber) {
        RotationVector.subscribers.add(subscriber);
    }

    @Override
    public void publish() {
        for (Subscriber subscriber: RotationVector.subscribers)
            subscriber.update();
    }
}