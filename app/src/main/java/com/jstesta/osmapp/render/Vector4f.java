package com.jstesta.osmapp.render;

import java.util.Arrays;

public class Vector4f extends Vector3f {
    private float w;

    public Vector4f(float x, float y, float z, float w) {
        super(x, y, z);
        this.w = w;
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public void add(Vector4f v) {
        vector[0] += v.vector[0];
        vector[1] += v.vector[1];
        vector[2] += v.vector[2];
        w += v.w;
    }

    @Override
    public String toString() {
        return "Vector4f{" +
                "vector=" + Arrays.toString(vector) + "," +
                "w=" + w +
                '}';
    }
}
