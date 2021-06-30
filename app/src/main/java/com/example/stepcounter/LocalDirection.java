package com.example.stepcounter;

import android.content.Context;
import android.hardware.SensorManager;
import android.widget.Toast;

import com.example.stepcounter.observers.Subscriber;
import com.example.stepcounter.sensors.Orientation;
import com.example.stepcounter.services.TurningDetector;


public class LocalDirection {
    private static final float HALF_TURN_TIME = 3f;
    private static final float COMPLETE_TURN_TIME = 7f;

    private static LocalDirection localDirection;
    private final TurningDetector turning180Detector;
    private final TurningDetector turning360Detector;

    private final Context context;
    Orientation orientation;


    private LocalDirection(Context context) {
        this.context = context;
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        orientation = Orientation.getInstance(sensorManager);
        this.turning180Detector = new TurningDetector(sensorManager, (float)Math.PI, HALF_TURN_TIME);
        this.turning360Detector = new TurningDetector(sensorManager, 2 * (float)Math.PI, COMPLETE_TURN_TIME);
        this.turning180Detector.register(new Turning180DegreeAlarm());
        this.turning360Detector.register(new Turning360DegreeAlarm());
    }

    public static LocalDirection getInstance(Context context) {
        if (localDirection == null)
            localDirection = new LocalDirection(context);
        return localDirection;
    }

    class Turning180DegreeAlarm implements Subscriber {
        @Override
        public void update() {
            Toast.makeText(context, "180 degree turn detected", Toast.LENGTH_LONG).show();
        }
    }

    class Turning360DegreeAlarm implements Subscriber {
        @Override
        public void update() {
            Toast.makeText(context, "360 degree turn detected", Toast.LENGTH_LONG).show();
        }
    }
}
