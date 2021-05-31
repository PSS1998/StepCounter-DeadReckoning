package com.example.stepcounter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stepcounter.graph.Plotter;
import com.example.stepcounter.graph.ScatterPlot;
import com.example.stepcounter.graph.Tracker;
import com.example.stepcounter.graph.footstep.FootStep;
import com.github.mikephil.charting.charts.ScatterChart;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


@RequiresApi(api = Build.VERSION_CODES.O)
public class GraphActivity extends AppCompatActivity {
    public static final String dbName = "StepCounter";
    public static final String stepDbName = "stepCounts";
    private MapScaleView mapScaleView;
    private ScatterChart scatterChart;
    private LinearLayout mLinearLayout;
    Plotter plotter;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Handler mHandler = new Handler();
    private Timer mTimer;

    Orientation orientation;

    ArrayList<Float> magneticHeading = new ArrayList<Float>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);

        setSharedPreferences();

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orientation = Orientation.getInstance(sensorManager);

        mLinearLayout = findViewById(R.id.linearLayoutGraph);
        mLinearLayout.setBackgroundColor(Color.WHITE);
        mapScaleView = findViewById(R.id.mapScaleView);
        scatterChart = findViewById(R.id.map);
        plotter = new Plotter(scatterChart);

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

        mTimer.scheduleAtFixedRate(new UpdateGraph(), 0, 2000);
    }

    class UpdateGraph extends TimerTask {
        private float getHeading() {
            float magHeading = 0;
            if(magneticHeading.size() > 3)
                magHeading = magneticHeading.get(magneticHeading.size()-4);
            else if (magneticHeading.size() > 0)
                magHeading = magneticHeading.get(magneticHeading.size()-1);
            return magHeading;
        }

        private boolean hasNewFootStep() {
            int stepCounts = sharedPreferences.getInt(stepDbName, 0);
            return Tracker.getInstance().getFootsteps().size() < stepCounts;
        }

        private void saveNewFootStep() {
            float degree = getHeading();
            FootStep newFootStep = Tracker.getInstance().generateNextFootStep(degree);
            Tracker.getInstance().addFootStep(newFootStep);
        }

        private void updateMap() {
            ArrayList<FootStep> footSteps = Tracker.getInstance().getFootsteps();
            Vector center = Tracker.getInstance().getCenterFootStep();
            Vector farthestPositionFromCenter = Tracker.getInstance().getFarthestPositionFromCenter();
            plotter.plotFootSteps(footSteps, center, farthestPositionFromCenter.getAbsoluteValue());
            magneticHeading.clear();
        }

        private void updateOrientation() {
            orientation.updateOrientationAngles();
            float[] orientationAngles = orientation.getOrientationAngles();
            magneticHeading.add(orientationAngles[0]);
        }

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateOrientation();
                    if (hasNewFootStep())
                        saveNewFootStep();
                    updateMap();
                }
            });

        }
    }
}

