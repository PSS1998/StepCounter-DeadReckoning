package com.example.stepcounter;


public class Constants {

    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 100;
    public static final int CONFIDENCE = 50;

    public static final int PLOT_POINTS_SIZE = 5;

    public static final int DEFAULT_HEIGHT = 168;
    public static final int DEFAULT_WEIGHT = 60;

    public static final int ORIENTATION_PERIOD = 70; //millisecond
    public static final int STEP_COUNTER_PERIOD = 200; //millisecond
    public static final int UI_UPDATE_PERIOD = 1000; //millisecond

    public static final double STEP_THRESHOLD_INHAND = 0.8d;
    public static final double STEP_NOISE_THRESHOLD_INHAND = 2.5d;
    public static final double STEP_THRESHOLD_INPOCKET = 4d;
    public static final double STEP_NOISE_THRESHOLD_INPOCKET = 13d;
    public static final double STEP_THRESHOLD_RUNNING = 16d;
    public static final double STEP_NOISE_THRESHOLD_RUNNING = 35d;

    public static final int LPF_ALPHA = 16; //adjust sensitivity of filter

}
