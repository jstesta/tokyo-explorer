package com.jstesta.osmapp.input;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v4.view.MotionEventCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.jstesta.osmapp.render.Camera;
import com.jstesta.osmapp.render.OSMGLRenderer;
import com.jstesta.osmapp.render.Vector3f;
import com.jstesta.osmapp.render.Vector4f;

public class InputManager {

    private static final String TAG = InputManager.class.getName();
    private static final int MULTI_TOUCH_MODE_THRESHOLD = 100;
    private GLSurfaceView glSurfaceView;
    private OSMGLRenderer renderer;
    private Camera camera;
    private int secondaryPointerIndex = -1;
    private boolean secondaryPointerDown = false;
    private int previousX = 0;
    private int previousY = 0;
    private MultiTouchMode multiTouchMode;

    public InputManager(GLSurfaceView glSurfaceView, Context context, OSMGLRenderer r, Camera c) {
        this.glSurfaceView = glSurfaceView;
        renderer = r;
        camera = c;
    }

    // Given an action int, returns a string description
    private static String actionToString(int action) {
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                return "Down";
            case MotionEvent.ACTION_MOVE:
                return "Move";
            case MotionEvent.ACTION_POINTER_DOWN:
                return "Pointer Down";
            case MotionEvent.ACTION_UP:
                return "Up";
            case MotionEvent.ACTION_POINTER_UP:
                return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE:
                return "Outside";
            case MotionEvent.ACTION_CANCEL:
                return "Cancel";
        }
        return "";
    }

    public boolean onTouchEvent(MotionEvent e) {

        int action = MotionEventCompat.getActionMasked(e);
        int index = MotionEventCompat.getActionIndex(e);

        //Log.d(TAG, "index: " + index);
        //Log.d(TAG, "action: " + actionToString(action));
        int xPos = -1;
        int yPos = -1;
        xPos = (int) e.getX();
        yPos = (int) e.getY();
        //Log.d(TAG, "x: " + xPos + ", y: " + yPos);

        //Log.d(TAG, "count: " + e.getPointerCount());

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (MultiTouchMode.TILT == multiTouchMode && secondaryPointerDown && index == 0) {
                    // Tilt
                    float yDiff = yPos - previousY;
                    yDiff /= 4;
                    //camera.rotateZ(yDiff);
                    camera.rotate(new Vector4f(1, 0, 0, -yDiff));
                } else if (MultiTouchMode.ZOOM == multiTouchMode && secondaryPointerDown && index == 0) {
                    // Zoom
                    int centerX = glSurfaceView.getWidth() / 2;
                    int centerY = glSurfaceView.getHeight() / 2;

                    int xPosRelCenter = Math.abs(xPos - centerX);
                    int yPosRelCenter = Math.abs(yPos - centerY);

                    int prevXPosRelCenter = Math.abs(previousX - centerX);
                    int prevYPosRelCenter = Math.abs(previousY - centerY);

                    if (xPosRelCenter < prevXPosRelCenter || yPosRelCenter < prevYPosRelCenter) {
                        // Zoom in
                        camera.scale(1.05f);
                    } else if (xPosRelCenter > prevXPosRelCenter || yPosRelCenter > prevYPosRelCenter) {
                        // Zoom out
                        camera.scale(0.95f);
                    }
                } else if (!secondaryPointerDown) {
                    // Move forward and back
                    int distanceX = xPos - previousX;
                    int distanceY = yPos - previousY;
                    camera.move(distanceY);
                    camera.rotate(new Vector4f(0, 0, 1, distanceX / 50f));
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                secondaryPointerDown = true;
                secondaryPointerIndex = index;

                int mainPointerY = (int) e.getY(e.getPointerId(0));
                int secondaryPointerY = (int) e.getY(e.getPointerId(index));

                int diff = Math.abs(secondaryPointerY - mainPointerY);

                multiTouchMode = diff <= MULTI_TOUCH_MODE_THRESHOLD
                        ? MultiTouchMode.TILT
                        : MultiTouchMode.ZOOM;

                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                secondaryPointerDown = false;
                break;
            case MotionEvent.ACTION_OUTSIDE:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        previousX = xPos;
        previousY = yPos;

        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            // Control Big C parameter
            case KeyEvent.KEYCODE_C:
                if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
                    renderer.decreaseBigC();
                } else {
                    renderer.increaseBigC();
                }
                break;
            // Control Little C parameter
            case KeyEvent.KEYCODE_V:
                if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
                    renderer.decreaseLittleC();
                } else {
                    renderer.increaseLittleC();
                }
                break;
            // Move down
            case KeyEvent.KEYCODE_S:
                camera.translate(new Vector3f(0, 0, -5));
                break;
            // Move up
            case KeyEvent.KEYCODE_W:
                camera.translate(new Vector3f(0, 0, 5));
                break;
            default:
                return false;
        }

        return true;
    }

    private enum MultiTouchMode {
        TILT,
        ZOOM,
    }
}
