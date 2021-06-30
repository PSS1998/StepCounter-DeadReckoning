package com.example.stepcounter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.stepcounter.sensors.Orientation;


public class GyroOrientation implements SensorEventListener {

    private SensorManager mySensorManager;
    Sensor gyroscopeSensor;
    Sensor rotationSensor;

    private static int gyroNotAvailible = 0;

    private static float gyroCurrentValue[] = {0, 0, 0};
    private static double gyroTimeStamp = 0;
    private static double timestamp = 0;
    private static final double NS2S = 1.0f / 1000000000.0f;
    private float initialHeading = 0;
    private float initialHeadingBias = 0;
    private static float currentHeading = 0;
    Orientation orientation;
    int initialHeadingSet = 0;
    private static float mRotationMatrix[] = new float[16];
    int counterCompass = Constants.LPF_ALPHA;

    public GyroOrientation(Context context){
        mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        rotationSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if (rotationSensor == null){
            gyroscopeSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (gyroscopeSensor == null){
                gyroNotAvailible = 1;
            }else{
                mySensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }else{
            mySensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        }

        orientation = Orientation.getInstance(mySensorManager);
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType()==Sensor.TYPE_GAME_ROTATION_VECTOR){
            float orientationVals[] = {0, 0, 0};
            // Convert the rotation-vector to a 4x4 matrix.
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            SensorManager.getOrientation(mRotationMatrix, orientationVals);

            orientationVals[0] -= initialHeadingBias;
            orientationVals[0] = (float)((orientationVals[0] < 0) ? (orientationVals[0] + (2.0 * Math.PI)) : orientationVals[0]);

            if (timestamp == 0)
                timestamp = event.timestamp;
            gyroTimeStamp = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

            if((initialHeadingSet == 0)){
                orientation.updateOrientationAngles();
                float[] orientationAnglesMagnetic = orientation.getOrientationAngles();
                initialHeading = orientationAnglesMagnetic[0];
                initialHeading = (float)((initialHeading < 0) ? (initialHeading + (2.0 * Math.PI)) : initialHeading);
                if(initialHeading != 0.0){
                    counterCompass -= 1;
                    if(counterCompass == 0) {
                        initialHeadingBias = orientationVals[0];
                        initialHeadingSet = 1;
                    }
                }
            }

            currentHeading = initialHeading+orientationVals[0];

            currentHeading = (float)((currentHeading > 3*Math.PI) ? (currentHeading - 4.0 * Math.PI) : (currentHeading > Math.PI) ? (currentHeading - 2.0 * Math.PI) : (currentHeading < -Math.PI) ? (currentHeading + 2.0 * Math.PI) : currentHeading);
        }
        if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            // grab current data and throw it into an array
            gyroCurrentValue[0] = event.values[0];
            gyroCurrentValue[1] = event.values[1];
            gyroCurrentValue[2] = event.values[2];

            if (timestamp == 0)
                timestamp = event.timestamp;
            gyroTimeStamp = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

            if((initialHeadingSet == 0)){
                orientation.updateOrientationAngles();
                float[] orientationAnglesMagnetic = orientation.getOrientationAngles();
                initialHeading = orientationAnglesMagnetic[0];
                if(initialHeading != 0.0){
                    counterCompass -= 1;
                    if(counterCompass == 0) {
                        currentHeading = initialHeading;
                        initialHeadingSet = 1;
                    }
                }
            }

            currentHeading -= gyroTimeStamp*gyroCurrentValue[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    public static float getOrientationBasedOnGyroscope(){
        if(gyroNotAvailible == 0) {
            return currentHeading;
        }
        else{
            return -10;
        }
    }

}
