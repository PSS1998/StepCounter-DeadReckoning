package com.example.stepcounter.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.stepcounter.Constants;
import com.example.stepcounter.ExtraFunctions;
import com.example.stepcounter.Filter;
import com.example.stepcounter.LocalDirection;
import com.example.stepcounter.sensors.Orientation;
import com.example.stepcounter.Point;
import com.example.stepcounter.graph.ScatterPlot;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class RoutingService extends Service {

    public static final String dbName = "StepCounter";
    public static final String stepDbName = "stepCounts";

    public static final String routePoints = "routePoints";

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    //    private static int stepFlag = 0;
    private Handler mHandler = new Handler();
    private Timer mTimer;
    private static float rotation = 0;

    Orientation orientation;

    public int stepCount;

    private float userHeight;

    Filter.medianFilter magneticHeading;

    private static ScatterPlot scatterPlot;

    private int outOfStartingZone = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        setSharedPreferences();
        userHeight = sharedPreferences.getFloat(StepCounterService.height, Constants.DEFAULT_HEIGHT);
        stepCount = 0;
        magneticHeading = new Filter.medianFilter();
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orientation = Orientation.getInstance(sensorManager);
        scatterPlot = ScatterPlot.getInstance();
        setTimer();

    }

    @SuppressLint("CommitPrefEdits")
    private void setSharedPreferences() {
        sharedPreferences = getApplicationContext().getSharedPreferences(dbName, 0);
        editor = sharedPreferences.edit();
    }

    private void setTimer() {
        if (mTimer != null)
            mTimer.cancel();
        else
            mTimer = new Timer();

        mTimer.scheduleAtFixedRate(new UpdateGraph(), 0, Constants.ORIENTATION_PERIOD);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class UpdateGraph extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    orientation.updateOrientationAngles();
                    float[] orientationAngles = orientation.getOrientationAngles();
                    double gyroHeading = LocalDirection.getOrientationBasedOnGyroscope();
                    if(gyroHeading == -10){
                        gyroHeading = orientationAngles[0];
                    }
                    float compHeading = Filter.calcComplementaryHeading(orientationAngles[0], (float)gyroHeading);
                    magneticHeading.addValue(compHeading);
                    float degrees = ExtraFunctions.radsToDegrees(compHeading);
                    rotation = degrees;

                    int stepCounts = sharedPreferences.getInt(stepDbName, 0);

                    if(stepCounts - stepCount > 0) {
                        stepCount = stepCounts;
                        updateRoute(calculatePoint());
                    }
                }
            });
        }
    }

    public static float getRotation () {
        return rotation;
    }

    public void updateRoute (Point point) {
        scatterPlot.addPoint(point.getPointY(), point.getPointX());
    }

    public Point calculatePoint() {
        float pointX = scatterPlot.getLastYPoint();
        float pointY = scatterPlot.getLastXPoint();
        float magHeading = 0;
        magHeading = magneticHeading.get();
        magneticHeading.clear();
        pointX += (float) (ExtraFunctions.calculateDistance(1, userHeight) * Math.cos(magHeading));
        pointY += (float) (ExtraFunctions.calculateDistance(1, userHeight) * Math.sin(magHeading));
        checkReturnToStartingPoint(pointX, pointY);
        return new Point(pointX, pointY);
    }

    public static ScatterPlot getScatter() {
        return scatterPlot;
    }

//    public static int getSteps() {
//        stepFlag = (stepFlag + 1) % 5;
//        if(stepFlag == 4) {
//            return 1;
//        }
//        return 0;
//    }

    public void checkReturnToStartingPoint(float pointX, float pointY){
        if(outOfStartingZone == 0) {
            if(((pointX*pointX) + (pointY*pointY)) > 5) {
                outOfStartingZone = 1;
            }
        }
        if(outOfStartingZone == 1) {
            if(((pointX*pointX) + (pointY*pointY)) < 5) {
                outOfStartingZone = 0;
                Toast.makeText(getApplicationContext(), "You have returned to your starting point!", Toast.LENGTH_LONG).show();
            }
        }
    }

}
