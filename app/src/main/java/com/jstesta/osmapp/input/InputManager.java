package com.jstesta.osmapp.input;

import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.jstesta.osmapp.render.Camera;
import com.jstesta.osmapp.render.OSMGLRenderer;
import com.jstesta.osmapp.util.Vector3f;

/**
 * Created by joseph.testa on 5/19/2017.
 */

public class InputManager extends GestureDetector.SimpleOnGestureListener {

    private static final String TAG = InputManager.class.getName();

    private OSMGLRenderer renderer;
    private Camera camera;

    public InputManager(OSMGLRenderer r, Camera c) {
        renderer = r;
        camera = c;
    }

    @Override
    public boolean onDown(MotionEvent event) {
//        Log.d(TAG, "onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                            float distanceX, float distanceY) {
//        Log.d(TAG, "onScroll: " + distanceX + "," + distanceY);
        Vector3f motion = new Vector3f(distanceX, -distanceY, 0);
        camera.translate(motion);
        return true;
    }
}
