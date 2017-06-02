package com.jstesta.osmapp.input;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.jstesta.osmapp.render.Camera;
import com.jstesta.osmapp.render.OSMGLRenderer;
import com.jstesta.osmapp.render.Vector3f;

/**
 * Created by joseph.testa on 5/19/2017.
 */

public class InputManager {

    private static final String TAG = InputManager.class.getName();

    private OSMGLRenderer renderer;
    private Camera camera;

    private GestureDetectorCompat mDetector;
    private ScaleGestureDetector mScaleDetector;

    public InputManager(Context context, OSMGLRenderer r, Camera c) {
        renderer = r;
        camera = c;


        mDetector = new GestureDetectorCompat(context, new Detector());
        mScaleDetector = new ScaleGestureDetector(context, new ScaleDetector());

    }

    public boolean onTouchEvent(MotionEvent e) {
        mScaleDetector.onTouchEvent(e);
        return this.mDetector.onTouchEvent(e);
    }

    private class Detector extends GestureDetector.SimpleOnGestureListener {

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

    private class ScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            camera.scale(scaleGestureDetector.getScaleFactor());
            return true;
        }
    }
}
