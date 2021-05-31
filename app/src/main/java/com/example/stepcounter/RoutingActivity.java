package com.example.stepcounter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stepcounter.graph.ScatterPlot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class RoutingActivity extends AppCompatActivity {

    public static final String dbName = "StepCounter";

    public static final String routePoints = "routePoints";

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    private static int stepState = 0;
    private static int stepFlag = 0;
    private Handler mHandler = new Handler();
    private Timer mTimer;
    private ImageView imageView;

    Orientation orientation;

    public int stepCount;

    ArrayList<Float> magneticHeading = new ArrayList<Float>();

    private ScatterPlot scatterPlot;

    private LinearLayout mLinearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);

        setSharedPreferences();

        stepCount = 0;

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orientation = Orientation.getInstance(sensorManager);

        //defining views
        mLinearLayout = findViewById(R.id.linearLayoutGraph);
        imageView = findViewById(R.id.compass);

        //setting up graph with origin
        scatterPlot = new ScatterPlot("Position");
        scatterPlot.addPoint(0, 0);
//        scatterPlot = ScatterPlot.getInstance();
        mLinearLayout.addView(scatterPlot.getGraphView(getApplicationContext()));


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
                    float rotation = degrees;
                    degrees = Filter.moving_average_heading(degrees);

                    TextView textView = (TextView) findViewById(R.id.textView);
                    textView.setText(String.valueOf(degrees));

//                    int stepCounts = sharedPreferences.getInt(stepDbName, 0);
                    int stepCounts = getSteps();
                    System.out.println(stepCounts);
                    imageView.setRotation(rotation);
                    if(stepCounts > 0) {
//                    if(stepCount - stepCounts> 0) {
//                        stepCount = stepCounts;
                        updateRoute(calculatePoint());




                    }
                }
            });
        }
    }

    public void updateRoute (Point point) {
        scatterPlot.addPoint(point.getPointY(), point.getPointX());
        mLinearLayout.removeAllViews();
        mLinearLayout.addView(scatterPlot.getGraphView(getApplicationContext()));
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


    public static int getSteps() {
        stepFlag = (stepFlag + 1) % 5;
        stepState = (stepState + 1) % 50;
        if (stepState < 50) { // 1 2 2 1 2 2 1 2 2
            if(stepFlag == 4) {
                return 1;
            }
            return 0;
        }
//        else if (stepState < 100) {
//            if(stepFlag == 4) {
//                return 1;
//            }
//            return 0;
//        }

        return 0;
    }




}

