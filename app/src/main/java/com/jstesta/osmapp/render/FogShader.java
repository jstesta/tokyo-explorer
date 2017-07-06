package com.jstesta.osmapp.render;

import android.opengl.GLES20;

/**
 * Created by joseph.testa on 7/6/2017.
 */

public class FogShader {


    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
                    "float density = 0.0005;"+
                    "const float LOG2 = 1.442695;"+
                    "float z = gl_FragCoord.z / gl_FragCoord.w;"+
                    "float fogFactor = exp2( -density * density * z * z * LOG2 );" +
                    "fogFactor = clfamp(fogFactor, 0.0, 1.0);"+

                    "vec4 frag_color = vColor * pixel;"+
                    "vec4 fog_color = vec4(1,1,1,0);"+

            "  gl_FragColor = mix(fog_color, frag_color, fogFactor);" +
            "}";

    private int mProgram;



    public void initializeOpenGL() {
        // prepare shaders and OpenGL program
        int fragmentShader = OSMGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program

        int[] params = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, params, 0);
        if (params[0] == GLES20.GL_FALSE) {
            OSMGLRenderer.checkGlError("glLinkProgram");
        }

        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        OSMGLRenderer.checkGlError("glAttachShader");
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
        OSMGLRenderer.checkGlError("glLinkProgram");
    }
}
