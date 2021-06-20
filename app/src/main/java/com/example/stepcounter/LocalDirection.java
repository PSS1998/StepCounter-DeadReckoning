package com.example.stepcounter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import com.example.stepcounter.services.StepCounterService;

import java.util.ArrayList;

public class LocalDirection implements SensorEventListener {

    private SensorManager mySensorManager;
    Sensor gyroscopeSensor;

    private final static int BUFFER_LEN = 64;
    // Ring Buffer
    private static float gyroBuffer[] = new float[BUFFER_LEN];
    private static double gyroBufferTimeStamp[] = new double[BUFFER_LEN];
    private static int buffHead = 0;
    private static int buffTail = 0;
    private static int buffNumVals = 0;
    private static float gyroCurrentValue[] = {0, 0, 0};
    private static double gyroTimeStamp = 0;
    private static double timestamp = 0;
    private final static float TURN_MINIMUM = 1; // degrees
    private final static int TURN_BACKOFF = BUFFER_LEN; // samples
    private static int backoffTimer = 0;
    private static final double NS2S = 1.0f / 1000000000.0f;
    private static float estimatedTurn = 0;
    private static float lastEstimatedTurn = 0;
    private static float initialHeading;
    private static float currentHeading;
    private static Context contextt;
    Orientation orientation;
    int initialHeadingSet = 0;

    public LocalDirection(Context context){
        mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscopeSensor == null){
        }else{
            mySensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        }

        contextt = context;

        orientation = Orientation.getInstance(mySensorManager);
        orientation.updateOrientationAngles();
        float[] orientationAngles = orientation.getOrientationAngles();
        initialHeading = orientationAngles[0];
        currentHeading = initialHeading;
    }

    // Ring buffer subroutines
    public void addToBuffer(float val, double timestamp){
        gyroBuffer[buffHead] = val;
        gyroBufferTimeStamp[buffHead] = timestamp;
        if( buffNumVals < BUFFER_LEN ) {
            buffNumVals++;
        }
        if( buffNumVals < BUFFER_LEN ) {
            buffHead = (buffHead + 1)%BUFFER_LEN;
        } else {
            buffHead = (buffHead + 1)%BUFFER_LEN;
            buffTail = (buffTail + 1)%BUFFER_LEN;
        }
    }

    public float getBufferSum(){
        float sum = 0;
        for( int i=buffTail; i!=buffHead; i=(i+1)%BUFFER_LEN ){
            sum += gyroBuffer[i] * gyroBufferTimeStamp[i];
        }
        return (float)(sum);
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            // grab current data and throw it into an array
            gyroCurrentValue[0] = event.values[0];
            gyroCurrentValue[1] = event.values[1];
            gyroCurrentValue[2] = event.values[2];

            if (timestamp == 0)
                timestamp = event.timestamp;
            gyroTimeStamp = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

            if((initialHeading == 0.0) && (initialHeadingSet == 0)){
                orientation.updateOrientationAngles();
                float[] orientationAngles = orientation.getOrientationAngles();
                initialHeading = orientationAngles[0];
                currentHeading = initialHeading;
                if(initialHeading != 0.0){
                    initialHeadingSet = 1;
                }
            }

            currentHeading -= gyroTimeStamp*gyroCurrentValue[1];

            // TODO: decide Y-axis or Z-axis should be used here based on inclination of device
            // add the Y-axis gyro component to the buffer
            addToBuffer(gyroCurrentValue[1], gyroTimeStamp);

            // calculate the current cumulative sum
            float bufferSum = getBufferSum();

            estimatedTurn = bufferSum;

            // only try to calculate turns if we're not in backoff
            if( backoffTimer <= 0 ){
                if( Math.abs(bufferSum) > TURN_MINIMUM){
                    float lastTwoTurnRadian = lastEstimatedTurn+estimatedTurn;
                    if(Math.abs(Math.abs(lastTwoTurnRadian) - Math.abs(Math.PI)) < 0.2*Math.PI){
                        Toast.makeText(contextt, "180 degree turn detected", Toast.LENGTH_LONG).show();
                    }
                    if(Math.abs(Math.abs(lastTwoTurnRadian) - Math.abs(2*Math.PI)) < 0.2*Math.PI){
                        Toast.makeText(contextt, "360 degree turn detected", Toast.LENGTH_LONG).show();
                    }
//                    currentHeading += estimatedTurn;
                    lastEstimatedTurn = estimatedTurn;
                    // backoff
                    backoffTimer = TURN_BACKOFF;
                }

            }else{
                backoffTimer--;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    public static float getOrientationBasedOnGyroscope(){
        return currentHeading;
    }

}
