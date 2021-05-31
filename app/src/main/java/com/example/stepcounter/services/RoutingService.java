package com.example.stepcounter.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.example.stepcounter.Orientation;
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

    ArrayList<Float> magneticHeading = new ArrayList<Float>();

    private static ScatterPlot scatterPlot;

    @Override
    public void onCreate() {
        super.onCreate();
        setSharedPreferences();
        stepCount = 0;
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

        mTimer.scheduleAtFixedRate(new UpdateGraph(), 0, 200);
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
                    magneticHeading.add(orientationAngles[0]);
                    float degrees = (float)((orientationAngles[0] < 0) ? (2.0 * Math.PI + orientationAngles[0]) : orientationAngles[0]);
                    degrees *= (180.0 / Math.PI);
                    rotation = degrees;
//                    degrees = Filter.moving_average_heading(degrees);
//
//                    TextView textView = (TextView) findViewById(R.id.textView);
//                    textView.setText(String.valueOf(degrees));

                    int stepCounts = sharedPreferences.getInt(stepDbName, 0);

                    if(stepCounts - stepCount > 0) {
                        System.out.println(stepCounts);
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
        if(magneticHeading.size() > 3)
            magHeading = magneticHeading.get(magneticHeading.size()-4);
        else
            magHeading = magneticHeading.get(magneticHeading.size()-1);
        magneticHeading.clear();
        pointX += (float)(10 * Math.cos(magHeading));
        pointY += (float)(10 * Math.sin(magHeading));
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




}
