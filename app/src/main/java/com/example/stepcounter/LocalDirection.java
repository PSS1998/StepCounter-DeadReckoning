package com.example.stepcounter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import com.example.stepcounter.sensors.Gyroscope;
import com.example.stepcounter.sensors.Orientation;
import com.example.stepcounter.sensors.RotationVector;


public class LocalDirection  {
    private static LocalDirection localDirection;
    private static int gyroNotAvailible = 0;

    private Gyroscope gyroscope;
    private RotationVector rotationSensor;

    private final static int BUFFER_LEN = 64;
    // Ring Buffer
    private float gyroBuffer[] = new float[BUFFER_LEN];
    private double gyroBufferTimeStamp[] = new double[BUFFER_LEN];
    private int buffHead = 0;
    private int buffTail = 0;
    private int buffNumVals = 0;
    public static final double NS2S = 1.0f / 1000000000.0f;
    private static float gyroCurrentValue[] = {0, 0, 0};
    private static double gyroTimeStamp = 0;
    private static double timestamp = 0;
    private final static float TURN_MINIMUM = (float)0.0174533; // degrees
    private final static int TURN_BACKOFF = BUFFER_LEN; // samples
    private static int backoffTimer = 0;

    private float estimatedTurn = 0;
    private float lastEstimatedTurn = 0;
    private float initialHeading = 0;
    public float initialHeadingBias = 0;
    private static float currentHeading = 0;
    private static Context context;
    Orientation orientation;
    int initialHeadingSet = 0;
    int counterCompass = Constants.LPF_ALPHA;


    private LocalDirection(Context context){
        SensorManager mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (mySensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) == null){
            if (mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null){
                gyroNotAvailible = 1;
            }else{
                gyroscope = Gyroscope.getInstance(mySensorManager, this);
                gyroscope.start(SensorManager.SENSOR_DELAY_GAME);
            }
        }
        else {
            rotationSensor = RotationVector.getInstance(mySensorManager, this);
            rotationSensor.start(SensorManager.SENSOR_DELAY_GAME);
        }

        LocalDirection.context = context;
        orientation = Orientation.getInstance(mySensorManager);
    }

    public static LocalDirection getInstance(Context context) {
        if (localDirection == null) {
            localDirection = new LocalDirection(context);
        }
        return localDirection;
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

    public float getBufferMinusSum(){
        float sum = 0;
        for( int i=buffTail; i!=ExtraFunctions.floorMod((buffHead-1),BUFFER_LEN); i=(i+1)%BUFFER_LEN ){
            sum += gyroBuffer[i]-gyroBuffer[(i+1)%BUFFER_LEN];
        }
        return (float)(sum);
    }

    private void checkTurning(){
        // only try to calculate turns if we're not in backoff
        if( backoffTimer <= 0 ){
            if( Math.abs(estimatedTurn) > TURN_MINIMUM){
                float lastTwoTurnRadian = lastEstimatedTurn+estimatedTurn;
                if(Math.abs(Math.abs(lastTwoTurnRadian) - Math.abs(2*Math.PI)) < 0.2 * Math.PI){
                    Toast.makeText(context, "360 degree turn detected", Toast.LENGTH_LONG).show();
                    estimatedTurn = 0;
                }
                else {
                    if (Math.abs(Math.abs(lastTwoTurnRadian) - Math.abs(Math.PI)) < 0.2 * Math.PI) {
                        Toast.makeText(context, "180 degree turn detected", Toast.LENGTH_LONG).show();
                        estimatedTurn = 0;
                    }
                }
                lastEstimatedTurn = estimatedTurn;
                // backoff
                backoffTimer = TURN_BACKOFF;
            }

        }else{
            backoffTimer--;
        }
    }


    public void updateOnGyroscopeChanged() {
        gyroCurrentValue = gyroscope.getGyroscopeValues();
        gyroTimeStamp = gyroscope.getGyroscopeTimestamp();
        timestamp = gyroscope.getTimestamp();

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

        // add the Y-axis gyro component to the buffer
        addToBuffer(gyroCurrentValue[1], gyroTimeStamp);

        // calculate the current cumulative sum
        float bufferSum = getBufferSum();

        estimatedTurn = bufferSum;

        checkTurning();
    }

    public void updateOnRotationVectorChanged() {
//        gyroTimeStamp = rotationSensor.getGyroscopeTimestamp();
        timestamp = rotationSensor.getTimestamp();
        float[] orientationVals = rotationSensor.getOrientationValues();

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

        // add the Y-axis gyro component to the buffer
        addToBuffer(orientationVals[0], gyroTimeStamp);

        // calculate the current cumulative sum
        float bufferSum = getBufferMinusSum();

        estimatedTurn = bufferSum;

        checkTurning();
    }

    public static float getOrientationBasedOnGyroscope(){
        if(gyroNotAvailible == 0) {
            return currentHeading;
        }
        else{
            return -10;
        }
    }

}
