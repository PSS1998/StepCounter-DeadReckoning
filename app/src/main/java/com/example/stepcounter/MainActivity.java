package com.example.stepcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.stepcounter.services.StepCounterService;

public class MainActivity extends AppCompatActivity {
    private boolean running = false;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getApplicationContext().getSharedPreferences(StepCounterService.dbName, 0);
        editor = sharedPreferences.edit();

        startStepCounter();

        Button resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetStepCountData();
            }
        });
    }

    private void resetStepCountData() {
        editor.putInt(StepCounterService.stepDbName, 0);
        editor.apply();
    }

    private void startStepCounter() {
        Intent intent = new Intent(MainActivity.this, StepCounterService.class);
        startService(intent);
    }
}