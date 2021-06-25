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
import java.util.Date;

public class TurningDetector implements Publisher, Subscriber {
    private static final double ERROR_THRESHOLD = 0.1 * Math.PI;

    private final ArrayList<Subscriber> subscribers = new ArrayList<>();
    private final float turningDegree;
    private final float timeThreshold;
    private final ArrayList<Pair<Long, Float>> directions = new ArrayList<>();
    private float currentTurn = 0f;
    private final SensorListener sensorListener;


    public TurningDetector(SensorManager sensorManager, float turningDegree, float timeThreshold) {
        this.turningDegree = turningDegree;
        this.timeThreshold = timeThreshold;
        this.sensorListener = findSensorListener(sensorManager);
        if (this.sensorListener != null)
            this.sensorListener.start(SensorManager.SENSOR_DELAY_GAME);
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

    private void addDirection() {
        long timestamp = this.sensorListener.getTimestamp();
        float orientation = this.sensorListener.getOrientationValues()[0];
        this.directions.add(new Pair<>(timestamp, orientation));
    }

    private void updateDirectionsData() {
        long now = new Date().getTime();
        while (!this.directions.isEmpty() && this.directions.get(0).first < now - this.timeThreshold)
            this.directions.remove(0);
    }

    private void calculateCurrentTurnDegree() {
        float maxTurn = 0;
        float lastDirection = this.directions.get(this.directions.size() - 1).second;
        for (Pair<Long, Float> direction : this.directions)
            if (Math.abs(direction.second - lastDirection) > maxTurn)
                maxTurn = Math.abs(direction.second - lastDirection);
        this.currentTurn = maxTurn;
    }

    private boolean hasTurned() {
        return Math.abs(this.currentTurn - this.turningDegree) <= TurningDetector.ERROR_THRESHOLD;
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
        this.addDirection();
        this.updateDirectionsData();
        this.calculateCurrentTurnDegree();
        if (this.hasTurned()) {
            this.directions.clear();
            this.publish();
        }
    }
}
