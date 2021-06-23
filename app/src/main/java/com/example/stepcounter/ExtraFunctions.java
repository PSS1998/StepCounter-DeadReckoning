package com.example.stepcounter;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;

public final class ExtraFunctions {

    private ExtraFunctions() {}

    public static float radsToDegrees(double rads) {
        double degrees = (rads < 0) ? (2.0 * Math.PI + rads) : rads;
        degrees *= (180.0 / Math.PI);
        return (float)degrees;
    }

    public static float calculateDistance(int num_steps, float height) {
        double distance = num_steps*height*0.3937*0.414*2.54e-2;
        return (float) distance;
    }

    public static int calculateCalories(int stepCounts, float m, float h) {
        int a = 5;//m/s2
        float height = h/100;
        return (int) (stepCounts * ((0.035 * m) + ((a / height) * (0.029 * m))) / 150);
    }

    public static int floorMod(int divided, int divisor) {
        int result = divided % divisor;
        if (result < 0) {
            result += divisor;
        }
        return result;
    }

}
