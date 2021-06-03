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
import androidx.annotation.RequiresApi;

import com.example.stepcounter.Constants;
import com.example.stepcounter.ExtraFunctions;
import com.example.stepcounter.InPocketDetector;
import com.example.stepcounter.MainActivity;
import com.example.stepcounter.R;
import com.example.stepcounter.RoutingActivity;
import com.example.stepcounter.SettingsActivity;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    public static int ignore_activity_recognition = 1;

    //notification
    public static final String CHANNEL_ID = "124578";
    private static final int NOTIFICATION_ID = 513;

    BroadcastReceiver broadcastReceiver;


    private static int SMOOTHING_WINDOW_SIZE = 20;

    private float mRawAccelValues[] = new float[3];

    // smoothing accelerometer signal variables
    private float mAccelValueHistory[][] = new float[3][SMOOTHING_WINDOW_SIZE];
    private float mRunningAccelTotal[] = new float[3];
    private float mCurAccelAvg[] = new float[3];
    private int mCurReadIndex = 0;

    public static float mStepCounter = 0;

    private double mGraph1LastXValue = 0d;
    private double mGraph2LastXValue = 0d;

    public static LineGraphSeries<DataPoint> mSeries1;
    public static LineGraphSeries<DataPoint> mSeries2;

    private double lastMag = 0d;
    private double avgMag = 0d;
    private double netMag = 0d;

    //peak detection variables
    private double lastXPoint = 1d;
    private int windowSize = 10;

    private static final int STEP_DELAY_NS = 250000000;
    private long timeNs = 0;
    private long lastStepTimeNs = 0;

    InPocketDetector inPocketDetector;
    Context context = this;


    @Override
    public void onCreate() {
        super.onCreate();

        inPocketDetector = new InPocketDetector(this, context);

        if(StepCounterService.mSeries1 == null) {
            StepCounterService.mSeries1 = new LineGraphSeries<>();
        }
        if(StepCounterService.mSeries2 == null) {
            StepCounterService.mSeries2 = new LineGraphSeries<>();
        }

        System.out.println(StepCounterService.mSeries1);

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
                        timeNs = sensorEvent.timestamp;
                        mRawAccelValues[0] = sensorEvent.values[0];
                        mRawAccelValues[1] = sensorEvent.values[1];
                        mRawAccelValues[2] = sensorEvent.values[2];

                        lastMag = Math.sqrt(Math.pow(mRawAccelValues[0], 2) + Math.pow(mRawAccelValues[1], 2) + Math.pow(mRawAccelValues[2], 2));

                        for (int i = 0; i < 3; i++) {
                            mRunningAccelTotal[i] = mRunningAccelTotal[i] - mAccelValueHistory[i][mCurReadIndex];
                            mAccelValueHistory[i][mCurReadIndex] = mRawAccelValues[i];
                            mRunningAccelTotal[i] = mRunningAccelTotal[i] + mAccelValueHistory[i][mCurReadIndex];
                            mCurAccelAvg[i] = mRunningAccelTotal[i] / SMOOTHING_WINDOW_SIZE;
                        }
                        mCurReadIndex++;
                        if(mCurReadIndex >= SMOOTHING_WINDOW_SIZE){
                            mCurReadIndex = 0;
                        }

                        avgMag = Math.sqrt(Math.pow(mCurAccelAvg[0], 2) + Math.pow(mCurAccelAvg[1], 2) + Math.pow(mCurAccelAvg[2], 2));

                        netMag = lastMag - avgMag; //removes gravity effect

                        //update graph data points
                        mGraph1LastXValue += 1d;
                        mSeries1.appendData(new DataPoint(mGraph1LastXValue, lastMag), true, 60);

                        mGraph2LastXValue += 1d;
                        mSeries2.appendData(new DataPoint(mGraph2LastXValue, netMag), true, 60);

                        peakDetection();

//                        if(ignore_activity_recognition == 1){
//                            if (InPocketDetector.pocket == 0 && MagnitudeDelta > 0.8 && MagnitudeDelta < 2.5) {
//                                bufferStep++;
//                            }
//                            if (InPocketDetector.pocket == 1 && MagnitudeDelta > 4 && MagnitudeDelta < 13) {
//                                bufferStep++;
//                            }
//                        }
//                        else {
//                            if (on_foot == 1) {
//                                if (walking == 1) {
//                                    if (InPocketDetector.pocket == 0 && MagnitudeDelta > 0.8 && MagnitudeDelta < 2.5) {
//                                        bufferStep++;
//                                    }
//                                    if (InPocketDetector.pocket == 1 && MagnitudeDelta > 4 && MagnitudeDelta < 13) {
//                                        bufferStep++;
//                                    }
//                                }
//                                if (running == 1 && MagnitudeDelta > 16 && MagnitudeDelta < 35) {
//                                    bufferStep++;
//                                }
//                            }
//                        }
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

        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, 250);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int initStepCount = 0;
        Notification notification = getMyActivityNotification(String.valueOf(initStepCount), String.valueOf((int)ExtraFunctions.calculateDistance(initStepCount)), String.valueOf(calculateCalories(initStepCount)));
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
                            System.out.println(stepCounts);
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
            updateNotification(String.valueOf(stepCount), String.valueOf((int)ExtraFunctions.calculateDistance(stepCount)), String.valueOf(calculateCalories(stepCount)));
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
        double h = SettingsActivity.height;
        return (int) (stepCounts * ((0.035 * m) + ((a / h) * (0.029 * m))) / 150);
    }



    private void handleUserActivity(int on_foot_confidence, int walking_confidence, int running_confidence) {
        on_foot = 0;
        walking = 1;
        running = 0;
        if(on_foot_confidence > Constants.CONFIDENCE){
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


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void peakDetection(){

        double stepThreshold = 1d;
        double noiseThreshold = 13d;

        if(SettingsActivity.activityRecognitionEnable == 1){
            ignore_activity_recognition = 0;
        }
        if(SettingsActivity.activityRecognitionEnable == 0){
            ignore_activity_recognition = 1;
        }

        if(on_foot == 0 && RoutingActivity.inRounting == 1 && ignore_activity_recognition == 0){
            ignore_activity_recognition = 1;
        }

        if(ignore_activity_recognition == 1){
            if (InPocketDetector.pocket == 0) {
                stepThreshold = 1d;
                noiseThreshold = 2.5d;
            }
            if (InPocketDetector.pocket == 1) {
                stepThreshold = 4d;
                noiseThreshold = 13d;
            }
        }
        else {
            if (walking == 1) {
                if (InPocketDetector.pocket == 0) {
                    stepThreshold = 1d;
                    noiseThreshold = 2.5d;
                }
                if (InPocketDetector.pocket == 1) {
                    stepThreshold = 4d;
                    noiseThreshold = 13d;
                }
            }
            if (running == 1) {
                stepThreshold = 16d;
                noiseThreshold = 35d;
            }
        }

        double highestValX = mSeries2.getHighestValueX();

        if(highestValX - lastXPoint < windowSize){
            return;
        }

        Iterator<DataPoint> valuesInWindow = mSeries2.getValues(lastXPoint,highestValX);

        lastXPoint = highestValX;

        double forwardSlope = 0d;
        double downwardSlope = 0d;

        List<DataPoint> dataPointList = new ArrayList<DataPoint>();
        valuesInWindow.forEachRemaining(dataPointList::add); //This requires API 24 or higher

        int foundStep = 0;

        for(int i = 0; i<dataPointList.size(); i++){
            if(i == 0) continue;
            else if(i < dataPointList.size() - 1){
                forwardSlope = dataPointList.get(i+1).getY() - dataPointList.get(i).getY();
                downwardSlope = dataPointList.get(i).getY() - dataPointList.get(i - 1).getY();

                if(forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i).getY() > stepThreshold && dataPointList.get(i).getY() < noiseThreshold){
                    foundStep = 1;
                }
            }
        }
        if(foundStep == 1){
            if(timeNs - lastStepTimeNs > STEP_DELAY_NS) {
                if (ignore_activity_recognition == 0 && on_foot == 1) {
                    lastStepTimeNs = timeNs;
                    bufferStep += 1;
                }
                if (ignore_activity_recognition == 1) {
                    lastStepTimeNs = timeNs;
                    bufferStep += 1;
                }
            }
        }
    }

}
