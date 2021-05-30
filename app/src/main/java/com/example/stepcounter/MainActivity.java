package com.example.stepcounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.stepcounter.services.StepCounterService;

public class MainActivity extends AppCompatActivity {
    private boolean running = false;
//    private SharedPreferences sharedPreferences;
//    private SharedPreferences.Editor editor;

    TextView textView;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        textView = (TextView) findViewById(R.id.info);
        button = (Button) findViewById(R.id.updateButton);
        Orientation orientation = Orientation.getInstance(sensorManager);

        button.setOnClickListener(new View.OnClickListener() {
            boolean isStarted = false;

            @Override
            public void onClick(View view) {
                orientation.updateOrientationAngles();
                float[] orientationAngles = orientation.getOrientationAngles();
                textView.setText("x:" + orientationAngles[0] + " y:" + orientationAngles[1] + " z:" + orientationAngles[2]);
            }
        });

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                1);

//        sharedPreferences = getApplicationContext().getSharedPreferences(StepCounterService.dbName, 0);
//        editor = sharedPreferences.edit();

//        startStepCounter();

//        Button resetButton = findViewById(R.id.reset);
//        resetButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                resetStepCountData();
//            }
//        });

        Button graphButton = findViewById(R.id.graphButton);
        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(myIntent);
            }
        });

        Button startWalking = findViewById(R.id.stratWalking);
        startWalking.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent stepsIntent = new Intent(MainActivity.this, StepCounterActivity.class);
                startActivity(stepsIntent);
            }
        });

    }

//    private void resetStepCountData() {
//        editor.putInt(StepCounterService.stepDbName, 0);
//        editor.apply();
//    }

//    private void startStepCounter() {
//        Intent intent = new Intent(MainActivity.this, StepCounterService.class);
//        startService(intent);
//    }
}