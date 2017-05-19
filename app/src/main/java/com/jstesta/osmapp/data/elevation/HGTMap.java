package com.jstesta.osmapp.data.elevation;

import android.content.Context;
import android.util.Log;

/**
 * Created by joseph.testa on 5/18/2017.
 */

public class HGTMap {
    private static final String TAG = HGTMap.class.getName();

    private static final int DIM = 1201;

    private short[] map;

    private HGTMap(short[] map) {
        this.map = map;
    }

    /**
     * <p>
     * Automatically adjusts to match the OpenGL ES coordinate system:<br>
     * https://developer.android.com/guide/topics/graphics/opengl.html#coordinate-mapping
     */
    public float get(int x, int y) {
        if ((x < 1 || x > DIM) || (y < 1 || y > DIM)) {
            throw new IndexOutOfBoundsException("[" + x + "," + y + "]");
        }

        int pos = (x - 1) + (DIM - y);

        return map[pos];
    }

    public static HGTMap create(Context c, int resource) {
        short[] map = HGTLoader.load(c, resource);
        return new HGTMap(map);
    }
}
