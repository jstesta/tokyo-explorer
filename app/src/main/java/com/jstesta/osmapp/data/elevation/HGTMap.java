package com.jstesta.osmapp.data.elevation;

import android.content.Context;

import com.jstesta.osmapp.render.AABB;
import com.jstesta.osmapp.render.Point;
import com.jstesta.osmapp.render.Vector3f;

/**
 * Created by joseph.testa on 5/18/2017.
 */

public class HGTMap {
    private static final String TAG = HGTMap.class.getName();

    public static final float UNIT_OF_MEASURE = 90.0f;

    public static final int SAMPLE = 1024;

    private static final int BOUND = SAMPLE + 1;
    // To set up 0-based indexing (there are actually BOUND+1 points in width and height)
    private static final int ZERO_BOUND = SAMPLE;

    private short[] map;

    private float maxHeight;

    /**
     * Takes a DIMxDIM slice of the supplied oMap
     */
    private HGTMap(short[] oMap) {

        int skip = HGTLoader.DIM - BOUND;

        this.map = new short[BOUND * BOUND];

        int myIndex = 0;
        int oIndex = 0;
        short max = 0;
        for (int y = 0; y < BOUND; y++) {
            for (int x = 0; x < BOUND; x++) {
                short h = oMap[oIndex++];

                // Set data voids to height = 0
                if (h == Short.MIN_VALUE) {
                    h = 0;
                }

                map[myIndex++] = h;
                max =  h > max ? h : max;
            }
            oIndex += skip;
        }

        maxHeight = max;
    }

    /**
     * <p>
     * Because SRTM DEM stores rows from north to south, this method automatically adjusts
     * y coordinates to match the OpenGL ES coordinate system:<br>
     * https://developer.android.com/guide/topics/graphics/opengl.html#coordinate-mapping
     * <p>
     * Uses zero-based x,y coordinates
     */
    public float get(int x, int y) {
        if ((x < 0 || x > ZERO_BOUND) || (y < 0 || y > ZERO_BOUND)) {
            throw new IndexOutOfBoundsException("[" + x + "," + y + "]");
        }

        // Translate the zero-based x,y coordinate to the 1-d array position
        int arrayPos = x + (ZERO_BOUND * (ZERO_BOUND - y));

        return map[arrayPos];
    }

    public float get(Point p) {
        return get(p.getX(), p.getY());
    }

    /**
     * Using zero-based x,y coordinates
     */
    public int get1DPosition(int x, int y) {
        if ((x < 0 || x > ZERO_BOUND) || (y < 0 || y > ZERO_BOUND)) {
            throw new IndexOutOfBoundsException("[" + x + "," + y + "]");
        }

        return x + (ZERO_BOUND * (ZERO_BOUND - y));
    }

    public int get1DPosition(Point p) {
        return get1DPosition(p.getX(), p.getY());
    }

    public Point getPointFor1DPosition(int p) {
        return new Point(p % ZERO_BOUND, ZERO_BOUND - (p / ZERO_BOUND));
    }

    public static HGTMap create(Context c, int resource) {
        short[] map = HGTLoader.load(c, resource);
        return new HGTMap(map);
    }

    public static Vector3f getWorldCoordinate(HGTMap m, int x, int y) {
        return new Vector3f(x * UNIT_OF_MEASURE, y * UNIT_OF_MEASURE, m.get(x, y));
    }

    public static Vector3f getWorldCoordinate(HGTMap m, Point p) {
        return new Vector3f(p.getX() * UNIT_OF_MEASURE, p.getY() * UNIT_OF_MEASURE, m.get(p.getX(), p.getY()));
    }

    // In zero-based index space
    public AABB getAABB() {
        int half = ZERO_BOUND / 2;
        return new AABB(new Point(half, half), ZERO_BOUND);
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public boolean pointIsInBounds(Point p) {
        return pointIsInBounds(p.getX(), p.getY());
    }

    public boolean pointIsInBounds(int x, int y) {
        return ! ((x < 0 || x > ZERO_BOUND) || (y < 0 || y > ZERO_BOUND));
    }
}
