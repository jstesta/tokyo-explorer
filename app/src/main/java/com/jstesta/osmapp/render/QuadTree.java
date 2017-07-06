package com.jstesta.osmapp.render;

import com.jstesta.osmapp.data.elevation.HGTMap;

import java.util.HashMap;

public class QuadTree {
    private static final String TAG = QuadTree.class.getName();

    HGTMap m;
    AABB aabb;

    QuadTree northWest;
    QuadTree northEast;
    QuadTree southWest;
    QuadTree southEast;

    public QuadTree(HGTMap map, AABB aabb) {
        this.m = map;
        this.aabb = aabb;
    }

    public void subdivide(HashMap<Integer, Boolean> matrix, Camera camera, int bigC, int littleC) {

        float f = getF(camera, bigC, littleC);

        int arrayIndex = m.get1DPosition(aabb.c);

        // Subdivide only if f < 1
        if (f >= 1) {
            matrix.put(arrayIndex, false);
            return;
        }

        int halfHalfDim = aabb.hd / 2;
        if (aabb.hd == 1) {
            return;
        }

        matrix.put(arrayIndex, true);

        // NW
        AABB aabbNW = new AABB(new Point(aabb.c.x - halfHalfDim, aabb.c.y + halfHalfDim), aabb.hd);
        northWest = new QuadTree(m, aabbNW);

        // NE
        AABB aabbNE = new AABB(new Point(aabb.c.x + halfHalfDim, aabb.c.y + halfHalfDim), aabb.hd);
        northEast = new QuadTree(m, aabbNE);

        // SE
        AABB aabbSE = new AABB(new Point(aabb.c.x + halfHalfDim, aabb.c.y - halfHalfDim), aabb.hd);
        southEast = new QuadTree(m, aabbSE);

        // SW
        AABB aabbSW = new AABB(new Point(aabb.c.x - halfHalfDim, aabb.c.y - halfHalfDim), aabb.hd);
        southWest = new QuadTree(m, aabbSW);

        // Recursively subdivide
        northWest.subdivide(matrix, camera, bigC, littleC);
        northEast.subdivide(matrix, camera, bigC, littleC);
        southEast.subdivide(matrix, camera, bigC, littleC);
        southWest.subdivide(matrix, camera, bigC, littleC);
    }

    // Calculate the f parameter
    private float getF(Camera c, int bigC, int littleC) {
        float l = getDistanceFromCamera(c);
        float d = getEdgeLength();

        float maxCd21 = Math.max(getD2() * littleC, 1);

        float f = l / (d * (float) bigC * maxCd21);

        return f;
    }

    private float getMaxDh() {
        float max = 0;
        float dh1 = Math.abs(((getNW() + getNE()) / 2f) - getN());
        max = dh1 > max ? dh1 : max;
        float dh2 = Math.abs(((getNE() + getSE()) / 2f) - getE());
        max = dh2 > max ? dh2 : max;
        float dh3 = Math.abs(((getSE() + getSW()) / 2f) - getS());
        max = dh3 > max ? dh3 : max;
        float dh4 = Math.abs(((getSW() + getNW()) / 2f) - getW());
        max = dh4 > max ? dh4 : max;
        float dh5 = Math.abs(((getNW() + getSE()) / 2f) - getC());
        max = dh5 > max ? dh5 : max;
        float dh6 = Math.abs(((getNE() + getSW()) / 2f) - getC());
        max = dh6 > max ? dh6 : max;

        return max;
    }

    private float getDistanceFromCamera(Camera c) {
        return Vector3f.distanceBetween(c.getPosition(), HGTMap.getWorldCoordinate(m, aabb.c.x, aabb.c.y));
    }

    private float getD2() {
        return getMaxDh() / getEdgeLength();
    }

    private float getNW() {
        return m.get(aabb.c.x - aabb.hd, aabb.c.y + aabb.hd);
    }

    private float getN() {
        return m.get(aabb.c.x, aabb.c.y + aabb.hd);
    }

    private float getNE() {
        return m.get(aabb.c.x + aabb.hd, aabb.c.y + aabb.hd);
    }

    private float getW() {
        return m.get(aabb.c.x - aabb.hd, aabb.c.y);
    }

    private float getC() {
        return m.get(aabb.c.x, aabb.c.y);
    }

    private float getE() {
        return m.get(aabb.c.x + aabb.hd, aabb.c.y);
    }

    private float getSW() {
        return m.get(aabb.c.x - aabb.hd, aabb.c.y - aabb.hd);
    }

    private float getS() {
        return m.get(aabb.c.x, aabb.c.y - aabb.hd);
    }

    private float getSE() {
        return m.get(aabb.c.x + aabb.hd, aabb.c.y - aabb.hd);
    }

    private float getEdgeLength() {
        return aabb.dim * HGTMap.UNIT_OF_MEASURE;
    }
}
