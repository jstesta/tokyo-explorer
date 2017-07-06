package com.jstesta.osmapp.render;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

/**
 * Created by joseph.testa on 5/19/2017.
 */
public class Camera {
    private static final String TAG = Camera.class.getName();
    // Matrices
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    // Position, focus, and orientation
    private Vector3f position;
    private Vector3f lookAt;
    private Vector3f up;

    private float mAngle;
    private float fovY = 50.0f;

    private Vector3f defaultForward = new Vector3f(0, 1, 0);

    // Perspective/frustum
    private ViewFrame frame;
    private float nearPlane = 1.0f;
    private float farPlane = 50000.0f;

    private GLSurfaceView view;

    public Camera(GLSurfaceView view) {

        this.view = view;

        position = new Vector3f(0, 0, 2000);
        lookAt = position.plus(defaultForward);
        up = new Vector3f(0, 0, 1);

        frame = new ViewFrame(-1, 1, -1, 1);

        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public float[] getViewMatrix() {
        return mViewMatrix;
    }

    public float[] getProjectionMatrix() {
        return mProjectionMatrix;
    }

    public void update() {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                position.getX(), position.getY(), position.getZ(),
                lookAt.getX(), lookAt.getY(), lookAt.getZ(),
                up.getX(), up.getY(), up.getZ());

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(mViewMatrix, 0, mViewMatrix, 0, mRotationMatrix, 0);
    }

    public void updateFrustum(float ratio) {

        frame.setLeft(-ratio);
        frame.setRight(ratio);

        updateFrustum();
    }

    private void updateFrustum() {
        Matrix.perspectiveM(mProjectionMatrix, 0, fovY, frame.getRight(), nearPlane, farPlane);
    }

    public void move(float dist) {
        Vector3f forward = normalize(lookAt.minus(position));
        forward.scale(dist);

        forward.scale(5);
        position.add(forward);
        lookAt.add(forward);
    }

    public void translate(Vector3f v) {
        v.scale(5);
        position.add(v);
        lookAt.add(v);
    }

    public void scale(float factor) {
        fovY = clamp(factor * fovY, 10, 90);
        //Log.d(TAG, String.valueOf(fovY));
        updateFrustum();
    }

    public Vector3f getPosition() {
        return position;
    }

    public void rotate(Vector4f rotate) {
        //Log.d(TAG, "rotate: " + rotate);

        Vector3f forward = lookAt.minus(position);
        //Log.d(TAG, "forward: " + forward);

        int xP = rotate.getX() != 0 ? 1 : 0;
        int yP = rotate.getY() != 0 ? 1 : 0;
        int zP = rotate.getZ() != 0 ? 1 : 0;

        float angle = rotate.getW();

        float[] i = new float[]{1, 1, 1, 1};
        float[] m = new float[16];
        float[] r = new float[4];

        Matrix.setIdentityM(m, 0);
        Matrix.translateM(m, 0, -forward.getX(), -forward.getY(), -forward.getZ());
        Matrix.rotateM(m, 0, angle, xP, yP, zP);
        Matrix.translateM(m, 0, forward.getX(), forward.getY(), forward.getZ());
        Matrix.multiplyMV(r, 0, m, 0, i, 0);
        //Log.d(TAG, "r: " + printArray(r));

        lookAt = lookAt.plus(new Vector3f(r[0] - 1, r[1] - 1, r[2] - 1));
        //Log.d(TAG, "lookAt: " + lookAt);
    }

    private String printArray(float[] o) {
        StringBuilder sb = new StringBuilder();
        for (Object anO : o) {
            sb.append(" ").append(String.valueOf(anO));
        }
        return sb.toString();

    }

    private Vector3f normalize(Vector3f v) {
        float len = (float) Math.sqrt((v.getX() * v.getX()) + (v.getY() * v.getY()) + (v.getZ() * v.getZ()));
        return new Vector3f(
                v.getX() / len,
                v.getY() / len,
                v.getZ() / len
        );
    }
}
