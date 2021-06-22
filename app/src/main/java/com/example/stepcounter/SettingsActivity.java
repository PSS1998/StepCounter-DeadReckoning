package com.example.stepcounter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stepcounter.services.StepCounterService;

public class SettingsActivity extends AppCompatActivity {

    public static float height = 167;
    public static float weight = 60;
    public static int activityRecognitionEnable = 1;

    private EditText editTextHeight;
    private EditText editTextWeight;
    private Switch activityRecognitionEnableSwitch;
    private Button stepCounterDebugButton;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editTextHeight = (EditText) findViewById(R.id.Height);
        editTextWeight = (EditText) findViewById(R.id.Weight);

        activityRecognitionEnableSwitch = (Switch) findViewById(R.id.recognitionEnSwitch);
        saveButton = (Button) findViewById(R.id.button_save);
        stepCounterDebugButton = (Button) findViewById(R.id.button_step_counter_debug);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String height_text = editTextHeight.getText().toString().trim();
                String weight_text = editTextWeight.getText().toString().trim();

                if (!height_text.isEmpty()) {
                    height = Float.parseFloat(height_text);
                }

                if (!weight_text.isEmpty()) {
                    weight = Float.parseFloat(weight_text);
                }

                if(activityRecognitionEnableSwitch.isChecked()){
                    activityRecognitionEnable = 1;
                }
                else{
                    activityRecognitionEnable = 0;
                }

                System.out.println(StepCounterService.ignore_activity_recognition);
                System.out.println(height);

                startActivity(new Intent(SettingsActivity.this, MainActivity.class));

            }
        });

        stepCounterDebugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, StepCounterDebugActivity.class));
            }
        });

    }

}