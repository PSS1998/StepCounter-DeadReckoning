package com.example.stepcounter.services;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Pair;

import com.example.stepcounter.observers.Publisher;
import com.example.stepcounter.observers.Subscriber;
import com.example.stepcounter.sensors.Gyroscope;
import com.example.stepcounter.sensors.RotationVector;
import com.example.stepcounter.sensors.SensorListener;

import java.util.ArrayList;

public class TurningDetector implements Publisher, Subscriber {
    private static final int BUCKET_DEGREE = 5;

    private final SensorListener sensorListener;
    private final float turningDegree;
    private final float timeThreshold;
    private final ArrayList<Subscriber> subscribers = new ArrayList<>();

    private int bucketNumbers;
    private final ArrayList<Long> buckets = new ArrayList<>();
    private Pair<Long, Float> currentDegree;


    public TurningDetector(SensorManager sensorManager, float turningDegree, float timeThreshold) {
        this.turningDegree = turningDegree;
        this.timeThreshold = 1000 * timeThreshold;
        this.sensorListener = this.findSensorListener(sensorManager);
        if (this.sensorListener != null)
            this.sensorListener.start(SensorManager.SENSOR_DELAY_GAME);
        this.initialBuckets();
    }

    private void initialBuckets() {
        this.bucketNumbers = this.degreeToBucketNumber(2 * Math.PI);
        for (int i = 0; i < bucketNumbers; i++)
            this.buckets.add(0L);
    }

    private void reInitiateBuckets() {
        this.buckets.clear();
        this.initialBuckets();
    }

    private int degreeToBucketNumber(double radianDegree) {
        double degree = Math.toDegrees(radianDegree);
        return (int) Math.floor(degree / BUCKET_DEGREE);
    }

    private SensorListener findSensorListener(SensorManager sensorManager) {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) != null) {
            RotationVector.getInstance(sensorManager).register(this);
            return RotationVector.getInstance(sensorManager);
        }
        else if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            Gyroscope.getInstance(sensorManager).register(this);
            return Gyroscope.getInstance(sensorManager);
        }
        return null;
    }

    private void setCurrentDegree() {
        long timestamp = this.sensorListener.getTimestamp() / 1000000;
        float orientation = this.sensorListener.getOrientationValues()[0];
        this.currentDegree = new Pair<>(timestamp, orientation);
    }

    private void updateBuckets() {
        int bucketIndex = this.degreeToBucketNumber(this.currentDegree.second) % this.bucketNumbers;
        this.buckets.set(bucketIndex, this.currentDegree.first);
    }

    private boolean hasTurnedClockwise() {
        int currentBucketIndex = this.degreeToBucketNumber(this.currentDegree.second);
        for (int i = 1; i <= this.degreeToBucketNumber(this.turningDegree); i++) {
            int bucketIndex = (currentBucketIndex - i + this.bucketNumbers) % this.bucketNumbers;
            if (this.currentDegree.first - this.buckets.get(bucketIndex) > this.timeThreshold)
                return false;
        }
        return true;
    }

    private boolean hasTurnedCounterClockwise() {
        int currentBucketIndex = this.degreeToBucketNumber(this.currentDegree.second);
        for (int i = 1; i <= this.degreeToBucketNumber(this.turningDegree); i++) {
            int bucketIndex = (currentBucketIndex + i) % this.bucketNumbers;
            if (this.currentDegree.first - this.buckets.get(bucketIndex) > this.timeThreshold)
                return false;
        }
        return true;
    }

    private boolean hasTurned() {
        return this.hasTurnedClockwise() || this.hasTurnedCounterClockwise();
    }

    public void register(Subscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    @Override
    public void publish() {
        for (Subscriber subscriber: this.subscribers)
            subscriber.update();
    }

    @Override
    public void update() {
        this.setCurrentDegree();
        if (this.hasTurned()) {
            this.reInitiateBuckets();
            this.publish();
        }
        this.updateBuckets();
    }
}
