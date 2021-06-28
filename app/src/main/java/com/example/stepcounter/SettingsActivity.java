package com.example.stepcounter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stepcounter.services.StepCounterService;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button saveButton = (Button) findViewById(R.id.button_save);
        createDB();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float height_info = sharedPreferences.getFloat(MainActivity.height, Constants.DEFAULT_HEIGHT);
                float weight_info = sharedPreferences.getFloat(MainActivity.weight, Constants.DEFAULT_WEIGHT);
                int activityRecognitionEnable = 0;
                EditText editTextHeight = findViewById(R.id.Height);
                EditText editTextWeight = findViewById(R.id.Weight);
                @SuppressLint("UseSwitchCompatOrMaterialCode")
                Switch activityRecognitionEnableSwitch = findViewById(R.id.recognitionEnSwitch);

                String height_text = editTextHeight.getText().toString().trim();
                String weight_text = editTextWeight.getText().toString().trim();

                if (!height_text.isEmpty()) {
                    height_info = Float.parseFloat(height_text);
                }
                if (!weight_text.isEmpty()) {
                    weight_info = Float.parseFloat(weight_text);
                }
                if(activityRecognitionEnableSwitch.isChecked()){
                    activityRecognitionEnable = 1;
                }

                editor.putFloat(MainActivity.height, height_info);
                editor.putFloat(MainActivity.weight, weight_info);
                editor.putInt(MainActivity.activityRecognitionEnabled, activityRecognitionEnable);
                editor.apply();

                startActivity(new Intent(SettingsActivity.this, MainActivity.class));

            }
        });

    }


    @SuppressLint("CommitPrefEdits")
    private void createDB() {
        sharedPreferences = getApplicationContext().getSharedPreferences(StepCounterService.dbName, 0);
        editor = sharedPreferences.edit();
    }

}