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

    public static float calculateDistance(int num_steps) {
        double distance = num_steps*SettingsActivity.height*0.3937*0.414*2.54e-2;
        return (float) distance;
    }

    public static int floorMod(int divided, int divisor) {
        int result = divided % divisor;
        if (result < 0) {
            result += divisor;
        }
        return result;
    }

}
