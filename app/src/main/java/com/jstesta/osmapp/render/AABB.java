package com.jstesta.osmapp.render;

/**
 * Created by joseph.testa on 5/22/2017.
 * <p>
 * Axis-aligned bounding box.
 */
public class AABB {
    protected final Point c;
    protected final int dim;
    protected final int hd;

    public AABB(Point center, int dimension) {
        c = center;
        dim = dimension;
        hd = dimension / 2;
    }

    public Point getCenter() {
        return c;
    }

    public int getDimension() {
        return dim;
    }

    public int getHd() {
        return hd;
    }

    @Override
    public String toString() {
        return "AABB{" +
                "center=" + c +
                ", dimension=" + dim +
                '}';
    }
}
