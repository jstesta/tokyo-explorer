package com.jstesta.osmapp.util;

import com.jstesta.osmapp.render.Vector2f;

/**
 * Created by joseph.testa on 6/8/2017.
 */

public final class ProjectionUtil {

    private static final double QUARTERPI = Math.PI / 4;

    public static Vector2f mercator(double l, double o) {

        l = l * Math.PI / 180;
        o = o * Math.PI / 180;

        float pX = (float) o;
        float pY = (float) (Math.log(Math.tan(QUARTERPI + 0.5 * l)));

        return new Vector2f(pX, pY);
    }
}
