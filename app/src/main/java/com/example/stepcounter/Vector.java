package com.example.stepcounter;

import androidx.annotation.NonNull;

public class Vector {
    private static final float ROUND_THRESHOLD = 0.00001f;
    private double x;
    private double y;
    private double z;

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public com.example.stepcounter.Vector multi(double coefficient) {
        this.x *= coefficient;
        this.y *= coefficient;
        this.z *= coefficient;
        return this;
    }

    public com.example.stepcounter.Vector div(double coefficient) {
        this.x /= coefficient;
        this.y /= coefficient;
        this.z /= coefficient;
        return this;
    }

    public com.example.stepcounter.Vector add(com.example.stepcounter.Vector vector) {
        this.x += vector.x;
        this.y += vector.y;
        this.z += vector.z;
        return this;
    }

    public static com.example.stepcounter.Vector add(com.example.stepcounter.Vector firstVector, com.example.stepcounter.Vector secondVector) {
        return new com.example.stepcounter.Vector(firstVector.x + secondVector.x, firstVector.y + secondVector.y, firstVector.z + secondVector.z);
    }

    public double getAbsoluteValue() {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
    }

    public double getThetaIn2D() {
        return Math.atan2(this.y, this.x);
    }

    public static com.example.stepcounter.Vector fromAbsoluteValueIn2D(double value, double angle) {
        return new com.example.stepcounter.Vector(Math.cos(angle), Math.sin(angle), 0).multi(value);
    }

    public com.example.stepcounter.Vector rotate2D(float theta) {
        return new com.example.stepcounter.Vector(this.x * Math.cos(theta) - this.y * Math.sin(theta) , this.y * Math.cos(theta) + this.x * Math.sin(theta), 0);
    }

    @NonNull
    @Override
    public String toString() {
        return "Vector: {X: " + this.x + ", Y: " + this.y + ", Z: " + this.z + "}";
    }

    public static com.example.stepcounter.Vector nullVector() {
        return new com.example.stepcounter.Vector();
    }

    public static com.example.stepcounter.Vector multi(com.example.stepcounter.Vector firstVector, com.example.stepcounter.Vector secondVector) {
        return new com.example.stepcounter.Vector(firstVector.x * secondVector.x, firstVector.y * secondVector.y, firstVector.z * secondVector.z);
    }

    public com.example.stepcounter.Vector round() {
        this.x = Math.abs(this.x) > ROUND_THRESHOLD ? this.x : 0;
        this.y = Math.abs(this.y) > ROUND_THRESHOLD ? this.y : 0;
        this.z = Math.abs(this.z) > ROUND_THRESHOLD ? this.z : 0;
        return this;
    }
}
