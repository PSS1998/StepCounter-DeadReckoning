package com.example.stepcounter;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.stepcounter.services.RoutingService;
import com.example.stepcounter.services.StepCounterService;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.Timer;
import java.util.TimerTask;

public class StepCounterActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private CircularProgressBar progress;
    private TextView stepsText;
    private TextView distanceText;
    private TextView caloryText;
    private Handler mHandler = new Handler();
    private Timer mTimer;
    private SharedPreferences.Editor editor;
    private Intent routeIntent;
    private float userHeight;
    private float userWeight;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        setSharedPreferences();
        progress = findViewById(R.id.progressBar);
        stepsText = findViewById(R.id.stepsInfo);
        distanceText = findViewById(R.id.distanceInfo);
        caloryText = findViewById(R.id.caloryInfo);

        startStepCounter();
        startRouting();
        setTimer();
        sharedPreferences = getApplicationContext().getSharedPreferences(StepCounterService.dbName, 0);
        editor = sharedPreferences.edit();
        userHeight = sharedPreferences.getFloat(StepCounterService.height, 168);
        userWeight = sharedPreferences.getFloat(StepCounterService.weight, 60);

        Button resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetStepCountData();
            }
        });

        Button graphButton = findViewById(R.id.graphButton);
        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(StepCounterActivity.this, RoutingActivity.class);
                startActivity(myIntent);
            }
        });

    }

    @SuppressLint("CommitPrefEdits")
    private void setSharedPreferences() {
        sharedPreferences = getApplicationContext().getSharedPreferences(StepCounterService.dbName, 0);
    }


    private void setTimer() {
        if (mTimer != null)
            mTimer.cancel();
        else
            mTimer = new Timer();

        mTimer.scheduleAtFixedRate(new updateInfoTimer(), 0, 1000);
    }

    class updateInfoTimer extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updatestepCounterLayout(sharedPreferences.getInt(StepCounterService.stepDbName, 0));
                }
            });
        }
    }
    
    private void startStepCounter() {
        Intent intent = new Intent(this, StepCounterService.class);
        startService(intent);
    }

    private void startRouting() {
        routeIntent = new Intent(this, RoutingService.class);
        startService(routeIntent);
    }

    private void updateStepCounter(String steps, String km, String calories) {
        stepsText.setText(steps);
        distanceText.setText(km);
        caloryText.setText(calories);
        progress.setProgressWithAnimation(Integer.parseInt(steps), (long) 1000);
        if ((Integer.parseInt(steps) / progress.getProgressMax()) < 0.25) {
            progress.setProgressBarColor(Color.YELLOW);
        }
        else if ((Integer.parseInt(steps) / progress.getProgressMax()) < 0.5) {
            progress.setProgressBarColor(Color.BLUE);
        }
        else {
            progress.setProgressBarColor(Color.GREEN);
        }
    }

    private void updatestepCounterLayout(int stepCount) {
        try {
            updateStepCounter(String.valueOf(stepCount), String.valueOf((int)ExtraFunctions.calculateDistance(stepCount, userHeight)), String.valueOf(ExtraFunctions.calculateCalories(stepCount, userWeight, userHeight)));

        } catch (Exception e) {
            // TODO: 4/22/2021 show error
        }
    }

    private void resetStepCountData() {
        editor.putInt(StepCounterService.stepDbName, 0);
//        editor.putString(RoutingService.routePoints, "");
        RoutingService.getScatter().clearPoints();
        stopService(routeIntent);
        startService(routeIntent);
        editor.apply();
    }


}