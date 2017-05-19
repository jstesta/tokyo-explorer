package com.jstesta.osmapp.render;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.jstesta.osmapp.util.Vector3f;

/**
 * Created by joseph.testa on 5/19/2017.
 */

public class Camera {
    private static final String TAG = Camera.class.getName();

    // Position, orientation, and focus
    private Vector3f position;
    private Vector3f center;
    private Vector3f up;
    private float mAngle;

    // Perspective/frustum
    private ViewFrame frame;
    private float nearPlane = 1.0f;
    private float farPlane = 1000.0f;

    // Matrices
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    // Touch support
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    private GLSurfaceView view;

    public Camera(GLSurfaceView view) {

        this.view = view;

        position = new Vector3f(0, -100, 300);
        center = new Vector3f(0, 300, 150);
        up = new Vector3f(0, 0, 1);

        frame = new ViewFrame(-1, 1, -1, 1);
    }

    public float[] getViewMatrix() {
        return mViewMatrix;
    }

    public float[] getProjectionMatrix() {
        return mProjectionMatrix;
    }

    public float[] getRotationMatrix() {
        return mRotationMatrix;
    }

    public void update() {
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                position.getX(), position.getY(), position.getZ(),
                center.getX(), center.getY(), center.getZ(),
                up.getX(), up.getY(), up.getZ());
    }

    public void updateFrustum(float ratio) {

        frame.setLeft(-ratio);
        frame.setRight(ratio);

        // this projection matrix is applied to object coordinates in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0,
                frame.getLeft(), frame.getRight(), frame.getBottom(), frame.getTop(),
                nearPlane, farPlane);
    }

    public void translate(Vector3f v) {
        position.add(v);
        center.add(v);
    }
}
