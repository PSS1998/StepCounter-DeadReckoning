package com.example.stepcounter;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.example.stepcounter.services.StepCounterService;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static final String FIRST_TIME_OPENED_APP = "FIRST_TIME_OPENED_APP";
    public static final String visitedFirstPage = "visitedFirstPage";
    public static final String height = "height";
    public static final String weight = "weight";
    public static final String activityRecognitionEnabled = "activityRecognitionEnabled";


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createDB();
        if (isFirstExperienceWithApp()) {
            if (!hasBeenTutorialCompleted())
                showTutorialPage();
            else
                showInfoPage();
        } else {
            showMainPage();
        }
    }

    @SuppressLint("CommitPrefEdits")
    private void createDB() {
        sharedPreferences = getApplicationContext().getSharedPreferences(StepCounterService.dbName, 0);
        editor = sharedPreferences.edit();
    }

    private boolean isFirstExperienceWithApp() {
        int firstTimeOpened = sharedPreferences.getInt(FIRST_TIME_OPENED_APP, 1);
        return firstTimeOpened == 1;
    }

    private boolean hasBeenTutorialCompleted() {
        int isInfoCompleted = sharedPreferences.getInt(visitedFirstPage, 0);
        return isInfoCompleted == 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void showMainPage() {
        setContentView(R.layout.activity_main);
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
    }

    private void showTutorialPage() {
        setContentView(R.layout.tutorial);
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
        setContentView(R.layout.tutorial_get_info);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startSettings();
            return true;
        } else if (item.getItemId() == R.id.action_debugging) {
            startDebugging();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startSettings() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }
    private void startDebugging() {
        Intent debugIntent = new Intent(MainActivity.this, StepCounterDebugActivity.class);
        startActivity(debugIntent);
    }

    private void setUserPhysicalInfo() {
        float height_info = Constants.DEFAULT_HEIGHT;
        float weight_info = Constants.DEFAULT_WEIGHT;
        int activityRecognitionEnable = 0;
        EditText editTextHeight = findViewById(R.id.Height_tutorial);
        EditText editTextWeight = findViewById(R.id.Weight_tutorial);
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
        editor.putFloat(height, height_info);
        editor.putFloat(weight, weight_info);
        editor.putInt(activityRecognitionEnabled, activityRecognitionEnable);
        editor.putInt(FIRST_TIME_OPENED_APP, 0);
        editor.apply();
    }
}
