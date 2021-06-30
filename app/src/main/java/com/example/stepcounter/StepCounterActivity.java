package com.example.stepcounter;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.stepcounter.services.RoutingService;
import com.example.stepcounter.services.StepCounterService;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class StepCounterActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private CircularProgressBar progress;
    private TextView stepsText;
    private TextView distanceText;
    private TextView caloryText;
    private TextView activityTypeText;
    private Handler mHandler = new Handler();
    private Timer mTimer;
    private SharedPreferences.Editor editor;
    private Intent routeIntent;
    private Intent stepCounterIntent;
    private float userHeight;
    private float userWeight;
    private ImageButton graphButton;
    private Button stopButton;
    private GraphButtonAnimator graphButtonAnimator;
    private final Timer graphAnimatorTimer = new Timer();
    private final int graphButtonAnimatorInterval = 10000;


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
        activityTypeText = findViewById(R.id.activity_type);

        startStepCounter();
        startRouting();
        setTimer();
        sharedPreferences = getApplicationContext().getSharedPreferences(StepCounterService.dbName, 0);
        editor = sharedPreferences.edit();
        userHeight = sharedPreferences.getFloat(StepCounterService.height, Constants.DEFAULT_HEIGHT);
        userWeight = sharedPreferences.getFloat(StepCounterService.weight, Constants.DEFAULT_WEIGHT);

        Button resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetStepCountData();
                stopButton.setEnabled(true);
                stopButton.setAlpha(1);
            }
        });

        this.graphButton = findViewById(R.id.graphButton);
        this.graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(StepCounterActivity.this, RoutingActivity.class);
                startActivity(myIntent);
            }
        });

        this.stopButton = findViewById(R.id.stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopStepCountData();
                stopButton.setEnabled(false);
                stopButton.setAlpha(0.5f);
            }
        });
        this.graphButtonAnimator = new GraphButtonAnimator();
        this.graphAnimatorTimer.schedule(this.graphButtonAnimator, 0, this.graphButtonAnimatorInterval);
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

        mTimer.scheduleAtFixedRate(new updateInfoTimer(), 0, Constants.UI_UPDATE_PERIOD);
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

    public class GraphButtonAnimator extends TimerTask {
        private int RUN_NUMBER = 5;
        private int counter = 0;
        @Override
        public void run() {
            if (counter >= RUN_NUMBER) {
                graphAnimatorTimer.cancel();
                return;
            }
            graphButton.animate().scaleX(0.5f).scaleY(0.5f).rotation(360).setDuration(1000).withEndAction(new Runnable() {
                @Override
                public void run() {
                    graphButton.animate().scaleX(1).scaleY(1).rotation(0).setDuration(1000);
                }
            });
        }
    }

    private void startStepCounter() {
        stepCounterIntent = new Intent(this, StepCounterService.class);
        startService(stepCounterIntent);
    }

    private void startRouting() {
        routeIntent = new Intent(this, RoutingService.class);
        startService(routeIntent);
    }

    private void updateStepCounter(String steps, String km, String calories) {
        stepsText.setText(steps + " steps");
        distanceText.setText(km);
        caloryText.setText(calories);
        activityTypeText.setText(StepCounterService.activityType);
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

    private void stopStepCountData() {
        editor.putInt(StepCounterService.stepDbName, 0);
        RoutingService.getScatter().clearPoints();
        stopService(stepCounterIntent);
        stopService(routeIntent);
        editor.apply();
        StepCounterService.deactivate();
    }

    private void resetStepCountData() {
        editor.putInt(StepCounterService.stepDbName, 0);
        RoutingService.getScatter().clearPoints();
        stopService(stepCounterIntent);
        startService(stepCounterIntent);
        stopService(routeIntent);
        startService(routeIntent);
        editor.apply();
        StepCounterService.activate();
    }
}