package com.jstesta.osmapp.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.jstesta.osmapp.R;
import com.jstesta.osmapp.data.elevation.HGTMap;
import com.jstesta.osmapp.input.InputManager;

public class OSMGLSurfaceView extends GLSurfaceView {

    private final OSMGLRenderer mRenderer;

    private HGTMap map;
    private Camera camera;
    private InputManager inputManager;

    public OSMGLSurfaceView(Context context) {
        super(context);

        camera = new Camera(this);

        map = HGTMap.create(context, R.raw.n35e139);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new OSMGLRenderer(camera, map);

        inputManager = new InputManager(this, context, mRenderer, camera);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        this.requestFocus();
        this.setFocusableInTouchMode(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return this.inputManager.onTouchEvent(e);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return inputManager.onKeyDown(keyCode, event);
    }
}
