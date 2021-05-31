package com.example.stepcounter.graph.footstep;

import com.example.stepcounter.Vector;

import java.util.Date;

public class FootStep {
    private Vector position;
    private Date time;

    public FootStep(Vector position, Date time) {
        this.position = position;
        this.time = time;
    }

    public Vector getPosition() {
        return position;
    }

    public Date getTime() {
        return time;
    }
}
