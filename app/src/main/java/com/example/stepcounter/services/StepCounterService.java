package com.example.stepcounter.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.stepcounter.Constants;
import com.example.stepcounter.InPocketDetector;
import com.example.stepcounter.MainActivity;
import com.example.stepcounter.R;

import java.util.Timer;
import java.util.TimerTask;


public class StepCounterService extends Service {
    public static final String dbName = "StepCounter";
    public static final String stepDbName = "stepCounts";
    private Handler mHandler = new Handler();
    private Timer mTimer;
    private int bufferStep = 0;
    private double MagnitudePrevious = 0;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private boolean isStepDetectorSensorPresent = false;
    private float StepNum = 0;

    int on_foot = 0;
    int walking = 0;
    int running = 0;

    // for debugging
    int ignore_activity_recognition = 1;

    //notification
    public static final String CHANNEL_ID = "124578";
    private static final int NOTIFICATION_ID = 513;

    BroadcastReceiver broadcastReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        setSharedPreferences();
        setTimer();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = null;
        if (sensorManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
                    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
//                    isStepDetectorSensorPresent = true;
                }
            }
            if (!isStepDetectorSensorPresent) {
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
        }

        SensorEventListener stepDetector = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent != null) {
                    if (isStepDetectorSensorPresent) {
                        StepNum++;
                    }
                    else {
                        float x_acceleration = sensorEvent.values[0];
                        float y_acceleration = sensorEvent.values[1];
                        float z_acceleration = sensorEvent.values[2];

                        double Magnitude = Math.sqrt(x_acceleration * x_acceleration + y_acceleration * y_acceleration + z_acceleration * z_acceleration);
                        double MagnitudeDelta = Magnitude - MagnitudePrevious;

                        MagnitudePrevious = Magnitude;
                        if(ignore_activity_recognition == 1){
                            if (InPocketDetector.pocket == 0 && MagnitudeDelta > 0.8 && MagnitudeDelta < 2.5) {
                                bufferStep++;
                            }
                            if (InPocketDetector.pocket == 1 && MagnitudeDelta > 4 && MagnitudeDelta < 13) {
                                bufferStep++;
                            }
                        }
                        else {
                            if (on_foot == 1) {
                                if (walking == 1) {
                                    if (InPocketDetector.pocket == 0 && MagnitudeDelta > 0.8 && MagnitudeDelta < 2.5) {
                                        bufferStep++;
                                    }
                                    if (InPocketDetector.pocket == 1 && MagnitudeDelta > 4 && MagnitudeDelta < 13) {
                                        bufferStep++;
                                    }
                                }
                                if (running == 1 && MagnitudeDelta > 16 && MagnitudeDelta < 35) {
                                    bufferStep++;
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        if (sensorManager != null) {
            sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // TODO: 4/22/2021 show error sensor not found
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int on_foot_confidence = intent.getIntExtra("on_foot_confidence", 0);
                    int walking_confidence = intent.getIntExtra("walking_confidence", 0);
                    int running_confidence = intent.getIntExtra("running_confidence", 0);
                    handleUserActivity(on_foot_confidence, walking_confidence, running_confidence);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));

        startTracking();

    }

    @SuppressLint("CommitPrefEdits")
    private void setSharedPreferences() {
        sharedPreferences = getApplicationContext().getSharedPreferences(dbName, 0);
        editor = sharedPreferences.edit();
    }

    private void setTimer() {
        if (mTimer != null)
            mTimer.cancel();
        else
            mTimer = new Timer();

        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, 200);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int initStepCount = 0;
        Notification notification = getMyActivityNotification(String.valueOf(initStepCount), String.valueOf((int) (initStepCount / 1.5)), String.valueOf(calculateCalories(initStepCount)));
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int stepCounts = sharedPreferences.getInt(stepDbName, 0);
                    if (isStepDetectorSensorPresent) {
                        stepCounts += StepNum;
                        editor.putInt(stepDbName, stepCounts);
                        StepNum = 0;
                    }
                    else {
                        if (bufferStep < 6 && bufferStep > 0) {
                            stepCounts++;
                            editor.putInt(stepDbName, stepCounts);
                        }
                        bufferStep = 0;
                    }

                    updateNotification(stepCounts);

                    editor.apply();
                }
            });
        }
    }

    //NOTIFICATION
    private void updateNotification(int stepCount) {
        try {
            updateNotification(String.valueOf(stepCount), String.valueOf((int) (stepCount / 1.4)), String.valueOf(calculateCalories(stepCount)));

        } catch (Exception e) {
            // TODO: 4/22/2021 show error
        }
    }



    private Notification getMyActivityNotification(String steps, String km, String calories) {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        RemoteViews collapsedView;
        collapsedView = new RemoteViews(getPackageName(),
                R.layout.notification_layout);

        collapsedView.setTextViewText(R.id.notification_steps, steps);
        collapsedView.setTextViewText(R.id.notification_km, km);
        collapsedView.setTextViewText(R.id.notification_calleri, calories);


        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_android_24)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_android_24))
                .setCustomContentView(collapsedView)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))
                .build();
    }

    private void updateNotification(String steps, String km, String calories) {
        Notification notification = getMyActivityNotification(steps, km, calories);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }




    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "StepCounter Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    public static int calculateCalories(Integer stepCounts) {
        int m = 70;//kg
        int a = 5;//m/s2
        double h = 1.78;
        return (int) (stepCounts * ((0.035 * m) + ((a / h) * (0.029 * m))) / 150);
    }



    private void handleUserActivity(int on_foot_confidence, int walking_confidence, int running_confidence) {
        on_foot = 0;
        walking = 1;
        running = 0;
        if(on_foot_confidence > 70){
            on_foot = 1;
            if(running_confidence > walking_confidence){
                running = 1;
                walking = 0;
            }
        }
    }

    private void startTracking() {
        Intent intent = new Intent(StepCounterService.this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }

    private void stopTracking() {
        Intent intent = new Intent(StepCounterService.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }




}
