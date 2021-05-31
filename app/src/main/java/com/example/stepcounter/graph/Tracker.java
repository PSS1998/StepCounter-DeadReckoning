package com.example.stepcounter.graph;

import com.example.stepcounter.Vector;
import com.example.stepcounter.graph.footstep.FootStep;

import java.util.ArrayList;
import java.util.Date;

public class Tracker {
    private static Tracker tracker;
    private static final float STEP_COEFFICIENT = 10;
    private ArrayList<FootStep> footsteps = new ArrayList<>();
    private Vector centerFootStep = Vector.nullVector();
    private Vector farthestPositionFromCenter = Vector.nullVector();

    public static Tracker getInstance() {
        if (tracker == null) {
            tracker = new Tracker();
            tracker.addFootStep(new FootStep(Vector.nullVector(), new Date()));
        }
        return tracker;
    }

    public ArrayList<FootStep> getFootsteps() {
        return footsteps;
    }

    public Vector getCenterFootStep() {
        return centerFootStep;
    }

    public Vector getFarthestPositionFromCenter() {
        return farthestPositionFromCenter;
    }

    public void addFootStep(double x, double y) {
        FootStep footStep = new FootStep(new Vector(x, y, 0), new Date());
        this.addFootStep(footStep);
    }

    public void addFootStep(FootStep footStep) {
        footsteps.add(footStep);
        centerFootStep.multi(footsteps.size() - 1).add(footStep.getPosition()).div(footsteps.size());
        this.updateFarthestPositionFromCenter();
        this.update();
    }

    private void updateFarthestPositionFromCenter() {
        double maximumDistance = 0;
        Vector farthestPositionFromCenter = Vector.nullVector();
        for (FootStep footStep: this.footsteps)
            if (maximumDistance < centerFootStep.distanceFrom(footStep.getPosition()))
                farthestPositionFromCenter = footStep.getPosition();
        this.farthestPositionFromCenter = farthestPositionFromCenter;
    }

    public FootStep generateNextFootStep(float degree) {
        FootStep lastFootstep = this.footsteps.get(footsteps.size() -1);
        Vector position = new Vector(lastFootstep.getPosition()).add(new Vector(Math.cos(degree), Math.sin(degree), 0).multi(STEP_COEFFICIENT));
        return new FootStep(position, new Date());
    }

    private void update() {

    }
}
