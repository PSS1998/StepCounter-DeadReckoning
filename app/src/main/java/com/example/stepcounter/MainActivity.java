package com.example.stepcounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stepcounter.services.StepCounterService;


public class MainActivity extends AppCompatActivity {
    private boolean running = false;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static final String firstTimeOpenedApp = "firstTimeOpened";
    public static final String visitedFirstPage = "visitedFirstPage";
    public static final String height = "height";
    public static final String weight = "weight";

    TextView textView;
    Button button;


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSharedPreferences();
        sharedPreferences = getApplicationContext().getSharedPreferences(StepCounterService.dbName, 0);
        editor = sharedPreferences.edit();
        int firstTimeOpened = sharedPreferences.getInt(firstTimeOpenedApp, 1);
        System.out.println(firstTimeOpened);
        if (firstTimeOpened == 1){
            int isInfoCompleted = sharedPreferences.getInt(visitedFirstPage, 0);
            if (isInfoCompleted == 0){
                setContentView(R.layout.tutorial);
                System.out.println("tutorial");
                showFirstPage();
            }
            else {
                System.out.println("tutorial_get_info");
                setContentView(R.layout.tutorial_get_info);
                showInfoPage();
            }

        }
        else {
            setContentView(R.layout.activity_main);
            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    1);


            Button startWalking = findViewById(R.id.startWalking);
            startWalking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent stepsIntent = new Intent(MainActivity.this, StepCounterActivity.class);
                    startActivity(stepsIntent);
                }
            });

            ImageView settings = findViewById(R.id.applicationSettings);
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                }
            });
        }

    }

    private void showFirstPage() {
        Button nextTutorial = findViewById(R.id.nextButton);
        nextTutorial.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editor.putInt(visitedFirstPage, 1);
                editor.apply();
                Intent infoIntent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(infoIntent);
            }
        });

    }

    private void showInfoPage() {
        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                setUserPhysicalInfo();
                Intent infoIntent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(infoIntent);
            }
        });
    }

    @SuppressLint("CommitPrefEdits")
    private void setSharedPreferences() {
        sharedPreferences = getApplicationContext().getSharedPreferences(StepCounterService.dbName, 0);
    }

    private void setUserPhysicalInfo() {
        float height_info = 168;
        float weight_info = 60;
        EditText editTextHeight = (EditText) findViewById(R.id.Height_tutorial);
        EditText editTextWeight = (EditText) findViewById(R.id.Weight_tutorial);

        String height_text = editTextHeight.getText().toString().trim();
        String weight_text = editTextWeight.getText().toString().trim();

        if (!height_text.isEmpty()) {
            height_info = Float.parseFloat(height_text);
        }

        if (!weight_text.isEmpty()) {
            weight_info = Float.parseFloat(weight_text);
        }
        editor.putFloat(height, height_info);
        editor.putFloat(weight, weight_info);
        editor.putInt(firstTimeOpenedApp, 0);
        editor.apply();
    }


}