package com.jstesta.osmapp;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.jstesta.osmapp.render.OSMGLSurfaceView;

public class OSMMainActivity extends Activity {

    private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = new OSMGLSurfaceView(this);
        setContentView(mGLView);
    }
}
