package com.jstesta.osmapp.render.terrain;

import android.opengl.GLES20;

import com.jstesta.osmapp.data.elevation.HGTMap;
import com.jstesta.osmapp.render.AABB;
import com.jstesta.osmapp.render.OSMGLRenderer;
import com.jstesta.osmapp.render.Point;
import com.jstesta.osmapp.render.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by joseph.testa on 5/29/2017.
 */

public class TerrainRenderer {
    private static final String TAG = TerrainRenderer.class.getName();

    private static final int RESOLUTION = 3;
    private static final float ARC_SECOND_METERS = 30.87f;
    private static final float SCALE = ARC_SECOND_METERS * RESOLUTION;

    private static final int CAPACITY = 4;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // =======================================
    // Shaders for terrain rendering
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

    private HGTMap m;
    private int mProgram = Integer.MIN_VALUE;

    private HashMap<Integer, Boolean> quadTreeMatrix = new HashMap<>();

    public TerrainRenderer(HGTMap map) {
        m = map;
    }

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

    public void setQuadTreeMatrix(HashMap<Integer, Boolean> quadTreeMatrix) {
        this.quadTreeMatrix = quadTreeMatrix;
    }

    public void draw(float[] mMVPMatrix) {
        if (mProgram == Integer.MIN_VALUE) {
            throw new IllegalStateException("initializeOpenGL was never called");
        }

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        OSMGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        OSMGLRenderer.checkGlError("glUniformMatrix4fv");

        AABB fullAABB = m.getAABB();

        draw(fullAABB, mMVPMatrix);
    }

    private void draw(AABB aabb, float[] mMVPMatrix) {

        Point center = aabb.getCenter();

        int arrayIndex = m.get1DPosition(center);

        if (!quadTreeMatrix.containsKey(arrayIndex)) {
            return;
        }

        boolean set = quadTreeMatrix.get(arrayIndex);

        if (set) { // We need to draw something

            // Realize quads at the next level
            int halfDim = aabb.getHd();
            int qrtrDim = halfDim / 2;

            Point nwCenter = new Point(center.getX() - qrtrDim, center.getY() + qrtrDim);
            Point swCenter = new Point(center.getX() - qrtrDim, center.getY() - qrtrDim);
            Point seCenter = new Point(center.getX() + qrtrDim, center.getY() - qrtrDim);
            Point neCenter = new Point(center.getX() + qrtrDim, center.getY() + qrtrDim);

            Boolean nwSet = quadTreeMatrix.get(m.get1DPosition(nwCenter));
            Boolean swSet = quadTreeMatrix.get(m.get1DPosition(swCenter));
            Boolean seSet = quadTreeMatrix.get(m.get1DPosition(seCenter));
            Boolean neSet = quadTreeMatrix.get(m.get1DPosition(neCenter));

            Point nCenter = new Point(center.getX(), center.getY() + aabb.getDimension());
            Point wCenter = new Point(center.getX() - aabb.getDimension(), center.getY());
            Point sCenter = new Point(center.getX(), center.getY() - aabb.getDimension());
            Point eCenter = new Point(center.getX() + aabb.getDimension(), center.getY());

            float[] coords = new float[3];
            int toAdd, index = 0;

            addVertex(coords, index, center);
            index += COORDS_PER_VERTEX;

            if (nwSet == null || !nwSet) { // Draw NW fan

                toAdd = 3;

                boolean skipN = false;
                if (m.pointIsInBounds(nCenter)) {
                    Boolean isSet = quadTreeMatrix.get(m.get1DPosition(nCenter));
                    skipN = isSet != null && !isSet;
                    if (skipN) {
                        toAdd--;
                    }
                }

                boolean skipW = false;
                if (m.pointIsInBounds(wCenter)) {
                    Boolean isSet = quadTreeMatrix.get(m.get1DPosition(wCenter));
                    skipW = isSet != null && !isSet;
                    if (skipW) {
                        toAdd--;
                    }
                }

                coords = Arrays.copyOf(coords, coords.length + (COORDS_PER_VERTEX * toAdd));

                if (!skipN) {
                    addVertex(coords, index, center.getX(), center.getY() + halfDim);
                    index += COORDS_PER_VERTEX;
                }

                addVertex(coords, index, center.getX() - halfDim, center.getY() + halfDim);
                index += COORDS_PER_VERTEX;

                if (!skipW) {
                    addVertex(coords, index, center.getX() - halfDim, center.getY());
                    index += COORDS_PER_VERTEX;
                }
            }

            if (swSet == null || !swSet) { // Draw SW fan

                toAdd = 2;

                boolean skipS = false;
                if (m.pointIsInBounds(sCenter)) {
                    Boolean isSet = quadTreeMatrix.get(m.get1DPosition(sCenter));
                    skipS = isSet != null && !isSet;
                    if (skipS) {
                        toAdd--;
                    }
                }

                // Check if need to include W point
                boolean drawW = false;
                if (coords.length == 3) {
                    drawW = true;
                    toAdd++;
                }

                coords = Arrays.copyOf(coords, coords.length + (COORDS_PER_VERTEX * toAdd));

                if (drawW) {
                    addVertex(coords, index, center.getX() - halfDim, center.getY());
                    index += COORDS_PER_VERTEX;
                }

                addVertex(coords, index, center.getX() - halfDim, center.getY() - halfDim);
                index += COORDS_PER_VERTEX;

                if (!skipS) {
                    addVertex(coords, index, center.getX(), center.getY() - halfDim);
                    index += COORDS_PER_VERTEX;
                }
            } else {
                if (coords.length > 3) {
                    // Draw what we have up to now and reset the coords collection
                    drawTriangleFan(coords, mMVPMatrix, aabb.getDimension());
                    coords = new float[3];
                    index = 0;
                    addVertex(coords, index, center);
                    index += COORDS_PER_VERTEX;
                }
            }

            if (seSet == null || !seSet) { // Draw SE fan

                toAdd = 2;

                boolean skipE = false;
                if (m.pointIsInBounds(eCenter)) {
                    Boolean isSet = quadTreeMatrix.get(m.get1DPosition(eCenter));
                    skipE = isSet != null && !isSet;
                    if (skipE) {
                        toAdd--;
                    }
                }

                // Check if need to include S point
                boolean drawS = false;
                if (coords.length == 3) {
                    drawS = true;
                    toAdd++;
                }

                coords = Arrays.copyOf(coords, coords.length + (COORDS_PER_VERTEX * toAdd));

                if (drawS) {
                    addVertex(coords, index, center.getX(), center.getY() - halfDim);
                    index += COORDS_PER_VERTEX;
                }

                addVertex(coords, index, center.getX() + halfDim, center.getY() - halfDim);
                index += COORDS_PER_VERTEX;

                if (!skipE) {
                    addVertex(coords, index, center.getX() + halfDim, center.getY());
                    index += COORDS_PER_VERTEX;
                }
            } else {
                if (coords.length > 3) {
                    // Draw what we have up to now and reset the coords collection
                    drawTriangleFan(coords, mMVPMatrix, aabb.getDimension());
                    coords = new float[3];
                    index = 0;
                    addVertex(coords, index, center);
                    index += COORDS_PER_VERTEX;
                }
            }

            if (neSet == null || !neSet) { // Draw NE fan

                toAdd = 2;

                boolean skipN = false;
                if (m.pointIsInBounds(nCenter)) {
                    Boolean isSet = quadTreeMatrix.get(m.get1DPosition(nCenter));
                    skipN = isSet != null && !isSet;
                    if (skipN) {
                        toAdd--;
                    }
                }

                // Check if need to include E point
                boolean drawE = false;
                if (coords.length == 3) {
                    drawE = true;
                    toAdd++;
                }

                // Check if need to include NW point
                boolean drawNW = false;
                if (nwSet == null || !nwSet) {
                    drawNW = true;
                    toAdd++;
                }

                coords = Arrays.copyOf(coords, coords.length + (COORDS_PER_VERTEX * toAdd));

                if (drawE) {
                    addVertex(coords, index, center.getX() + halfDim, center.getY());
                    index += COORDS_PER_VERTEX;
                }

                addVertex(coords, index, center.getX() + halfDim, center.getY() + halfDim);
                index += COORDS_PER_VERTEX;

                if (!skipN) {
                    addVertex(coords, index, center.getX(), center.getY() + halfDim);
                    index += COORDS_PER_VERTEX;
                }

                if (drawNW) {
                    addVertex(coords, index, center.getX() - halfDim, center.getY() + halfDim);
                }
            }

            if (coords.length > 3) {
                drawTriangleFan(coords, mMVPMatrix, aabb.getDimension());
            }

            if (aabb.getDimension() == 2) {
                return;
            }

            AABB nwAABB = new AABB(nwCenter, halfDim);
            AABB swAABB = new AABB(swCenter, halfDim);
            AABB seAABB = new AABB(seCenter, halfDim);
            AABB neAABB = new AABB(neCenter, halfDim);

            if (nwSet != null && nwSet) {
                draw(nwAABB, mMVPMatrix);
            }

            if (swSet != null && swSet) {
                draw(swAABB, mMVPMatrix);
            }

            if (seSet != null && seSet) {
                draw(seAABB, mMVPMatrix);
            }

            if (neSet != null && neSet) {
                draw(neAABB, mMVPMatrix);
            }
        }
    }

    private void drawTriangleFan(float[] coords, float[] mMVPMatrix, int dim) {

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

        float avgHeight = 0;
        for (int i = 1; i <= vertexCount; i++) {
            avgHeight += coords[(i * 3) - 1];
        }
        avgHeight /= vertexCount;

        // Set color with red, green, blue and alpha (opacity) values
        float color[] = {.1f, avgHeight / m.getMaxHeight(), .1f, 1};

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

    private void addVertex(float[] coords, int index, Point p) {
        addVertex(coords, index, p.getX(), p.getY());
    }

    private void addVertex(float[] coords, int index, int x, int y) {
        coords[index++] = x * SCALE;
        coords[index++] = y * SCALE;
        coords[index++] = m.get(x, y);
    }
}
