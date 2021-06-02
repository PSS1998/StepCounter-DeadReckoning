package com.example.stepcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.stepcounter.services.StepCounterService;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;

import static java.lang.Thread.sleep;


public class StepCounterDebugActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_activity_step_counter);

        Intent intent = new Intent(this, StepCounterService.class);
        startService(intent);

        if(StepCounterService.mSeries1 == null) {
            StepCounterService.mSeries1 = new LineGraphSeries<>();
        }
        if(StepCounterService.mSeries2 == null) {
            StepCounterService.mSeries2 = new LineGraphSeries<>();
        }

        //Graph for showing raw acceleration magnitude signal
        GraphView graph = (GraphView) this.findViewById(R.id.graph);
        graph.addSeries(StepCounterService.mSeries1);
        graph.setTitle("Accelerator Signal");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Signal Value");
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(60);

        //Graph for showing smoothed acceleration magnitude signal
        GraphView graph2 = (GraphView) this.findViewById(R.id.graph2);
        graph2.setTitle("Smoothed Signal");
        graph2.addSeries(StepCounterService.mSeries2);
        graph2.getGridLabelRenderer().setVerticalAxisTitle("Signal Value");
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(60);

    }

}

