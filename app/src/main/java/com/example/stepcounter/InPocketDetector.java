package com.example.stepcounter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.example.stepcounter.services.StepCounterService;

import java.math.BigDecimal;

public class InPocketDetector implements SensorEventListener {

    public static int pocket = 0;

    private SensorManager mySensorManager;
    TextView proximityAvailable, proximityReading, lightAvailable, lightReading, accAvailable, accReading;
    Sensor proximitySensor, lightSensor, accSensor;
    float rp = -1;
    float rl = -1;
    float[] g = {0, 0, 0};
    int inclination = -1;

    StepCounterService main;

    public InPocketDetector(StepCounterService main, Context context){
        this.main = main;
        mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        accSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        proximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (accSensor == null){
        }else{
            mySensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (proximitySensor == null){
        }else{
            mySensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if(lightSensor != null){
            mySensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        } else {
        }



    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            g = new float[3];
            g = event.values.clone();

            double norm_Of_g = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);

            g[0] = (float) (g[0] / norm_Of_g);
            g[1] = (float) (g[1] / norm_Of_g);
            g[2] = (float) (g[2] / norm_Of_g);

            inclination = (int) Math.round(Math.toDegrees(Math.acos(g[2])));

            //Log.v("SSS", g[0]+"  "+g[1]+"  "+g[2]+"  inc "+inclination);
        }
        if(event.sensor.getType()==Sensor.TYPE_PROXIMITY){
            rp=event.values[0];
        }
        if(event.sensor.getType() == Sensor.TYPE_LIGHT){
            rl=event.values[0];
        }
        if((rp!=-1) && (rl!=-1) && (inclination!=-1)){
            detect(rp, rl, g, inclination);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public BigDecimal round(float d) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    public void detect(float prox, float light, float g[], int inc){
        if((prox<1)&&(light<2)&&(g[1]<-0.6)&&( (inc>75)||(inc<100))){
            pocket=1;
        }
        if((prox>=1)&&(light>=2)&&(g[1]>=-0.7)){
            pocket=0;
        }
    }

}