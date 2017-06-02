package com.jstesta.osmapp.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.jstesta.osmapp.data.elevation.HGTMap;
import com.jstesta.osmapp.render.terrain.TerrainRenderer;

import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by joseph.testa on 5/16/2017.
 */

public class OSMGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = OSMGLRenderer.class.getName();

    private HGTMap map;
    private Camera camera;
    private TerrainRenderer terrainRenderer;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];

    public OSMGLRenderer(Camera camera, HGTMap map) {
        this.camera = camera;
        this.map = map;
        this.terrainRenderer = new TerrainRenderer(map);
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(135f / 255, 206f / 255, 235f / 255, 1.0f);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        terrainRenderer.initializeOpenGL();
    }

    public void onDrawFrame(GL10 unused) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        camera.update();

        HashMap<Integer, Boolean> matrix = new HashMap<>();
        QuadTree quadTree = new QuadTree(map, map.getAABB());
//        quadTree.subdivide(matrix, camera, 8, 1);
        quadTree.subdivide(matrix, camera, 4, 1);
        terrainRenderer.setQuadTreeMatrix(matrix);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, camera.getProjectionMatrix(), 0, camera.getViewMatrix(), 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, camera.getRotationMatrix(), 0);

        terrainRenderer.draw(mMVPMatrix);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        camera.updateFrustum((float) width / height);
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        checkGlError("glCreateShader");

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        checkGlError("glShaderSource");
        GLES20.glCompileShader(shader);
        checkGlError("glCompileShader");

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
