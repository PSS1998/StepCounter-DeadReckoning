package com.example.stepcounter;

import java.util.ArrayList;

public class Filter {

    private static ArrayList<Float[]> moving_acceleration;
    private static ArrayList<Float[]> moving_geomagnetic;
    private static ArrayList<Float> moving_heading;
    private static final float moving_average_size=12;//change

    private static final float ALPHA = 1/16F;//adjust sensitivity


    public static float moving_average_heading(float heading){
        if(moving_heading == null){
            moving_heading = new ArrayList<>();
            for (int i = 0; i < moving_average_size; i++) {
                moving_heading.add(0F);
            }
            return 0F;
        }

        moving_heading.remove(0);
        moving_heading.add(heading);

        float moving_average = 0f;
        for (int i = 0; i < moving_average_size; i++) {
            moving_average += moving_heading.get(i);
        }
        moving_average = moving_average/moving_average_size;

        return moving_average;
    }

    public static float[] moving_average_acceleration(float[] acceleration) {
        if(moving_acceleration == null){
            moving_acceleration = new ArrayList<>();
            for (int i = 0; i < moving_average_size; i++) {
                moving_acceleration.add(new Float[]{0F,0F,0F});
            }
            return new float[]{0F,0F,0F};
        }

        moving_acceleration.remove(0);
        moving_acceleration.add(new Float[]{acceleration[0],acceleration[1],acceleration[2]});
        return moving_average(moving_acceleration);
    }

    public static float[] moving_average_geomagnetic(float[] geomagnetic) {
        if(moving_geomagnetic == null){
            moving_geomagnetic = new ArrayList<>();
            for (int i = 0; i < moving_average_size; i++) {
                moving_geomagnetic.add(new Float[]{0F,0F,0F});
            }
            return new float[]{0F,0F,0F};
        }

        moving_geomagnetic.remove(0);
        moving_geomagnetic.add(new Float[]{geomagnetic[0],geomagnetic[1],geomagnetic[2]});
        return moving_average(moving_geomagnetic);
    }

    private static float[] moving_average(ArrayList<Float[]> moving_values){
        float[] moving_average = new float[]{0F,0F,0F};
        for (int i = 0; i < moving_average_size; i++) {
            moving_average[0] += moving_values.get(i)[0];
            moving_average[1] += moving_values.get(i)[1];
            moving_average[2] += moving_values.get(i)[2];
        }
        moving_average[0] = moving_average[0]/moving_average_size;
        moving_average[1] = moving_average[1]/moving_average_size;
        moving_average[2] = moving_average[2]/moving_average_size;
        return moving_average;
    }

    public static float[] LPF(float[] input, float[] output) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

}
