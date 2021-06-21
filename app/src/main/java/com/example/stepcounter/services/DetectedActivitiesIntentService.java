package com.example.stepcounter.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import com.example.stepcounter.Constants;

public class DetectedActivitiesIntentService  extends IntentService {

    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        int on_foot = 0;
        int on_foot_confidence = 0;
        int walking = 0;
        int walking_confidence = 0;
        int running = 0;
        int running_confidence = 0;

        for (DetectedActivity activity : detectedActivities) {
            if(activity.getType() == DetectedActivity.ON_FOOT){
                on_foot_confidence = activity.getConfidence();
            }
            if(activity.getType() == DetectedActivity.WALKING){
                walking_confidence = activity.getConfidence();
            }
            if(activity.getType() == DetectedActivity.RUNNING){
                running_confidence = activity.getConfidence();
            }
//            Log.i(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
        }

        ArrayList<Integer> activitiesConfidence = new ArrayList<Integer>();;
        activitiesConfidence.add(on_foot_confidence);
        activitiesConfidence.add(walking_confidence);
        activitiesConfidence.add(running_confidence);

        broadcastActivity(activitiesConfidence);
    }

    private void broadcastActivity(ArrayList<Integer> activitiesConfidence) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("on_foot_confidence", activitiesConfidence.get(0));
        intent.putExtra("walking_confidence", activitiesConfidence.get(1));
        intent.putExtra("running_confidence", activitiesConfidence.get(2));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
