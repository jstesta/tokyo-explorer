package com.jstesta.osmapp.render;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

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
    private float scale = 1.0f;
    private float fovY = 50.0f;

    // Perspective/frustum
    private ViewFrame frame;
    private float nearPlane = 1.0f;
    private float farPlane = 100000.0f;

    // Matrices
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private GLSurfaceView view;

    public Camera(GLSurfaceView view) {

        this.view = view;

        position = new Vector3f(0, 0, 2000);
        center = new Vector3f(0, 100, 1800);
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

        updateFrustum();
    }

    private void updateFrustum() {
        // this projection matrix is applied to object coordinates in the onDrawFrame() method
//        Matrix.frustumM(mProjectionMatrix, 0,
//                scale * frame.getLeft(), scale * frame.getRight(), scale * frame.getBottom(), scale * frame.getTop(),
//                nearPlane, farPlane);
        Matrix.perspectiveM(mProjectionMatrix, 0, fovY, frame.getRight(), nearPlane, farPlane);
    }

    public void translate(Vector3f v) {
        v.scale(5);
        position.add(v);
        center.add(v);
    }

    public void scale(float factor) {
        fovY = clamp(factor * fovY, 10, 90);
        //Log.d(TAG, String.valueOf(fovY));
        updateFrustum();
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public Vector3f getPosition() {
        return position;
    }
}
