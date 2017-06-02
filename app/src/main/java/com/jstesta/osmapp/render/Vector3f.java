package com.jstesta.osmapp.render;

import java.util.Arrays;

/**
 * Created by joseph.testa on 5/19/2017.
 */

public class Vector3f {
    final float[] vector;

    public Vector3f(float x, float y, float z) {
        vector = new float[]{x, y, z};
    }

    public float getX() {
        return vector[0];
    }

    public float getY() {
        return vector[1];
    }

    public float getZ() {
        return vector[2];
    }

    public void setX(float x) {
        vector[0] = x;
    }

    public void setY(float y) {
        vector[1] = y;
    }

    public void setZ(float z) {
        vector[2] = z;
    }

    public void set(float x, float y, float z) {
        vector[0] = x;
        vector[1] = y;
        vector[2] = z;
    }

    public void add(Vector3f v) {
        vector[0] += v.getX();
        vector[1] += v.getY();
        vector[2] += v.getZ();
    }

    @Override
    public String toString() {
        return "Vector3f{" +
                "vector=" + Arrays.toString(vector) +
                '}';
    }

    public static float distanceBetween(Vector3f a, Vector3f b) {
        float dX = a.getX() - b.getX();
        float dY = a.getY() - b.getY();
        float dZ = a.getZ() - b.getZ();

        return (float) Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public void scale(float factor) {
        vector[0] *= factor;
        vector[1] *= factor;
        vector[2] *= factor;
    }
}
