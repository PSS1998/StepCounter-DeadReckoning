package com.example.stepcounter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stepcounter.graph.ScatterPlot;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class GraphActivity extends AppCompatActivity {

    public static final String dbName = "StepCounter";
    public static final String stepDbName = "stepCounts";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Handler mHandler = new Handler();
    private Timer mTimer;

    Orientation orientation;

    public int stepCount;

    ArrayList<Float> magneticHeading = new ArrayList<Float>();

    private ScatterPlot scatterPlot;

    private LinearLayout mLinearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        setSharedPreferences();

        stepCount = 0;

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orientation = Orientation.getInstance(sensorManager);

        //defining views
        mLinearLayout = findViewById(R.id.linearLayoutGraph);

        //setting up graph with origin
        scatterPlot = new ScatterPlot("Position");
        scatterPlot.addPoint(0, 0);
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
                    degrees = Filter.moving_average_heading(degrees);

                    TextView textView = (TextView) findViewById(R.id.textView);
                    textView.setText(String.valueOf(degrees));

                    int stepCounts = sharedPreferences.getInt(stepDbName, 0);
                    if((stepCounts - stepCount) > 0) {
                        stepCount = stepCounts;
                        float oPointX = scatterPlot.getLastYPoint();
                        float oPointY = scatterPlot.getLastXPoint();
                        float magHeading = 0;
                        if(magneticHeading.size() > 3)
                            magHeading = magneticHeading.get(magneticHeading.size()-4);
                        else
                            magHeading = magneticHeading.get(magneticHeading.size()-1);
                        magneticHeading.clear();
                        oPointX += (float)(10 * Math.cos(magHeading));
                        oPointY += (float)(10 * Math.sin(magHeading));
                        scatterPlot.addPoint(oPointY, oPointX);
                        mLinearLayout.removeAllViews();
                        mLinearLayout.addView(scatterPlot.getGraphView(getApplicationContext()));
                    }
                }
            });
        }
    }


}

