package com.jstesta.osmapp.render;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class WaterRenderer {

    // =======================================
    // Shaders for water rendering
    // =======================================
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

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private int mProgram = Integer.MIN_VALUE;

    private Vector3f c1 = new Vector3f(0, 0, -1);
    private Vector3f c2 = new Vector3f(92160, 0, -1);
    private Vector3f c3 = new Vector3f(92160, 92160, -1);
    private Vector3f c4 = new Vector3f(0, 92160, -1);

    private float[] color = new float[]{0, 0, 0.4f, 1};


    public void initializeOpenGL() {
        // prepare shaders and OpenGL program
        int vertexShader = OSMGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = OSMGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program

        int[] params = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, params, 0);
        if (params[0] == GLES20.GL_FALSE) {
            OSMGLRenderer.checkGlError("glLinkProgram");
        }

        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        OSMGLRenderer.checkGlError("glAttachShader");
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        OSMGLRenderer.checkGlError("glAttachShader");
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
        OSMGLRenderer.checkGlError("glLinkProgram");
    }


    public void draw(float[] mMVPMatrix) {
        if (mProgram == Integer.MIN_VALUE) {
            throw new IllegalStateException("initializeOpenGL was never called");
        }

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);
        OSMGLRenderer.checkGlError("glUseProgram");

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        OSMGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        OSMGLRenderer.checkGlError("glUniformMatrix4fv");

        drawWater(mMVPMatrix);
    }

    private void drawWater(float[] mMVPMatrix) {

        float[] coords = new float[]{
                c1.getX(), c1.getY(), c1.getZ(),
                c2.getX(), c2.getY(), c2.getZ(),
                c3.getX(), c3.getY(), c3.getZ(),
                c4.getX(), c4.getY(), c4.getZ(),
        };

        drawTriangleFan(coords);
    }

    private void drawTriangleFan(float[] coords) {

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                coords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(coords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        int vertexCount = coords.length / COORDS_PER_VERTEX;
        ByteBuffer ib = ByteBuffer.allocateDirect(vertexCount * 2);
        ib.order(ByteOrder.nativeOrder());

        ShortBuffer indexBuffer = ib.asShortBuffer();
        short[] indices = new short[vertexCount];
        for (short i = 0; i < vertexCount; i++) {
            indices[i] = i;
        }
        indexBuffer.put(indices);
        indexBuffer.position(0);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        OSMGLRenderer.checkGlError("glGetAttribLocation");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        OSMGLRenderer.checkGlError("glEnableVertexAttribArray");

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, vertexBuffer);
        OSMGLRenderer.checkGlError("glVertexAttribPointer");

        // get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        OSMGLRenderer.checkGlError("glGetUniformLocation");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        OSMGLRenderer.checkGlError("glUniform4fv");

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, vertexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        OSMGLRenderer.checkGlError("glDrawElements");

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        OSMGLRenderer.checkGlError("glDisableVertexAttribArray");
    }
}
