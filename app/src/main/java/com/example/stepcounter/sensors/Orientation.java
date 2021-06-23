package com.example.stepcounter.sensors;

import android.hardware.SensorManager;

import com.example.stepcounter.TSAGeoMag;

import java.util.GregorianCalendar;

public class Orientation {
    private float[] orientationAngles = new float[3];
    private float[] rotationMatrix = new float[9];
    private SensorManager sensorManager;
    private static Orientation orientation;
    private Orientation(SensorManager sensorManager){
        this.sensorManager = sensorManager;
        CompassAccelerometer.getInstance(sensorManager).start();
        Magnetometer.getInstance(sensorManager).start();

    }

    public static Orientation getInstance(SensorManager sensorManager) {
        if (orientation == null)
            orientation = new Orientation(sensorManager);
        return orientation;
    }

    public void updateOrientationAngles() {

        SensorManager.getRotationMatrix(rotationMatrix, null,
                CompassAccelerometer.getInstance(sensorManager).getAcceleration(), Magnetometer.getInstance(sensorManager).getMagnetic_field());
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        orientationAngles[0] += calculateMagneticDeclination();
        orientationAngles[0] = (float)((orientationAngles[0]>Math.PI) ? (orientationAngles[0] - 2*Math.PI) : (orientationAngles[0]<-Math.PI) ? (orientationAngles[0] + 2*Math.PI) : orientationAngles[0]);
    }

    public float[] getOrientationAngles() {
        return orientationAngles;
    }

    // TODO: Get lat, long and altitude values from Location Service
    private static float calculateMagneticDeclination() {
        TSAGeoMag geoMag = new TSAGeoMag();
        double declination = geoMag.getDeclination(35.6892, 51.3890, geoMag.decimalYear(new GregorianCalendar()), 1.189);
        declination = Math.toRadians(declination);
        return (float)declination;
    }

}
