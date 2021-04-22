package com.example.stepcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.stepcounter.services.StepCounterService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startStepCounter();
    }


    private void startStepCounter() {
        Intent intent = new Intent(MainActivity.this, StepCounterService.class);
        startService(intent);
    }
}