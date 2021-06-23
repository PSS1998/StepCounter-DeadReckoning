package com.example.stepcounter;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stepcounter.graph.ScatterPlot;
import com.example.stepcounter.services.RoutingService;

import java.util.Timer;
import java.util.TimerTask;


public class RoutingActivity extends AppCompatActivity {

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    public static int inRouting = 0;

    private Handler mHandler = new Handler();
    private Timer mTimer;
    private ImageView imageView;
    private ScatterPlot scatterPlot;
    private LinearLayout mLinearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);

        inRouting = 1;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setSharedPreferences();

        mLinearLayout = findViewById(R.id.linearLayoutGraph);
        imageView = findViewById(R.id.compass);

        scatterPlot = RoutingService.getScatter();
        mLinearLayout.addView(scatterPlot.getGraphView(getApplicationContext()));


        setTimer();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        inRouting = 0;
    }

    @SuppressLint("CommitPrefEdits")
    private void setSharedPreferences() {
        sharedPreferences = getApplicationContext().getSharedPreferences(RoutingService.dbName, 0);
        editor = sharedPreferences.edit();
    }

    private void setTimer() {
        if (mTimer != null)
            mTimer.cancel();
        else
            mTimer = new Timer();

        mTimer.scheduleAtFixedRate(new UpdateGraph(), 0, Constants.UI_UPDATE_PERIOD);
    }

    class UpdateGraph extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    float rotation = RoutingService.getRotation();
                    imageView.setRotation(-rotation);
                    viewRoute();
                }
            });
        }
    }

    public void viewRoute () {
        mLinearLayout.removeAllViews();
        mLinearLayout.addView(scatterPlot.getGraphView(getApplicationContext()));

    }


}

