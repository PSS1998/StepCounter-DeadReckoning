package com.example.stepcounter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;


public class LocalDirection implements SensorEventListener {

    private SensorManager mySensorManager;
    Sensor gyroscopeSensor;
    Sensor rotationSensor;

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
    private final static float TURN_MINIMUM = (float)0.0174533; // degrees
    private final static int TURN_BACKOFF = BUFFER_LEN; // samples
    private static int backoffTimer = 0;
    private static final double NS2S = 1.0f / 1000000000.0f;
    private static float estimatedTurn = 0;
    private static float lastEstimatedTurn = 0;
    private static float initialHeading;
    private static float initialHeadingBias = 0;
    private static float currentHeading;
    private static Context contextt;
    Orientation orientation;
    int initialHeadingSet = 0;
    private static float mRotationMatrix[] = new float[16];
    int counterCompass = Constants.LPF_ALPHA;

    public LocalDirection(Context context){
        mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        rotationSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if (rotationSensor == null){
            gyroscopeSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (gyroscopeSensor == null){
            }else{
                mySensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }else{
            mySensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        }

        contextt = context;

        orientation = Orientation.getInstance(mySensorManager);
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

            // add the Y-axis gyro component to the buffer
            addToBuffer(orientationVals[0], gyroTimeStamp);

            // calculate the current cumulative sum
            float bufferSum = getBufferMinusSum();

            estimatedTurn = bufferSum;

            // only try to calculate turns if we're not in backoff
            if( backoffTimer <= 0 ){
                if( Math.abs(bufferSum) > TURN_MINIMUM){
                    float lastTwoTurnRadian = lastEstimatedTurn+estimatedTurn;
                    if(Math.abs(Math.abs(lastTwoTurnRadian) - Math.abs(2*Math.PI)) < 0.2 * Math.PI){
                        Toast.makeText(contextt, "360 degree turn detected", Toast.LENGTH_LONG).show();
                    }
                    else {
                        if (Math.abs(Math.abs(lastTwoTurnRadian) - Math.abs(Math.PI)) < 0.2 * Math.PI) {
                            Toast.makeText(contextt, "180 degree turn detected", Toast.LENGTH_LONG).show();
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

            // add the Y-axis gyro component to the buffer
            addToBuffer(gyroCurrentValue[1], gyroTimeStamp);

            // calculate the current cumulative sum
            float bufferSum = getBufferSum();

            estimatedTurn = bufferSum;

            // only try to calculate turns if we're not in backoff
            if( backoffTimer <= 0 ){
                if( Math.abs(bufferSum) > TURN_MINIMUM){
                    float lastTwoTurnRadian = lastEstimatedTurn+estimatedTurn;
                    if(Math.abs(Math.abs(lastTwoTurnRadian) - Math.abs(2*Math.PI)) < 0.2 * Math.PI){
                        Toast.makeText(contextt, "360 degree turn detected", Toast.LENGTH_LONG).show();
                    }
                    else {
                        if (Math.abs(Math.abs(lastTwoTurnRadian) - Math.abs(Math.PI)) < 0.2 * Math.PI) {
                            Toast.makeText(contextt, "180 degree turn detected", Toast.LENGTH_LONG).show();
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    public static float getOrientationBasedOnGyroscope(){
        return currentHeading;
    }

}
