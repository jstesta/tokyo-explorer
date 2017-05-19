package com.jstesta.osmapp.render.terrain;

import android.opengl.GLES20;
import android.util.Log;

import com.jstesta.osmapp.data.elevation.HGTMap;
import com.jstesta.osmapp.render.OSMGLRenderer;
import com.jstesta.osmapp.render.shape.Triangle;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by joseph.testa on 5/18/2017.
 */

public class SimpleMesh {

    private static final String TAG = SimpleMesh.class.getName();

    private static final int RESOLUTION = 3;
    private static final float ARC_SECOND_METERS = 30.87f;
    private static final float SCALE = ARC_SECOND_METERS * RESOLUTION;

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // The matrix must be included as a modifier of gl_Position.
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private Collection<Triangle> mesh;

    private final int mProgram;

    public SimpleMesh(HGTMap map, int boundingDimension) {
        mesh = new ArrayList<>(2 * (boundingDimension ^ 2));

        for (int y = 1; y <= boundingDimension; y++) {
            for (int x = 1; x <= boundingDimension; x++) {
                float xy = map.get(x, y);
                float x1y = map.get(x + 1, y);
                float xy1 = map.get(x, y + 1);
                float x1y1 = map.get(x + 1, y + 1);

//                Log.d(TAG, "1: " + xy + "," + x1y + "," + xy1);
                mesh.add(new Triangle(new float[]{
                        SCALE * x, SCALE * y, xy,
                        SCALE * (x + 1), SCALE * y, x1y,
                        SCALE * x, SCALE * (y + 1), xy1
                }));
//                Log.d(TAG, "2: " + xy1 + "," + x1y + "," + x1y1);
                mesh.add(new Triangle(new float[]{
                        SCALE * x, SCALE * (y + 1), xy1,
                        SCALE * (x + 1), SCALE * y, x1y,
                        SCALE * (x + 1), SCALE * (y + 1), x1y1
                }));
            }
        }

        // prepare shaders and OpenGL program
        int vertexShader = OSMGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = OSMGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

    }

    public void draw(float[] mMVPMatrix) {

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        for (Triangle t : mesh) {
            t.draw(mProgram, mMVPMatrix);
        }
    }
}
