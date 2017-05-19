package com.jstesta.osmapp.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;

import com.jstesta.osmapp.R;
import com.jstesta.osmapp.data.elevation.HGTMap;
import com.jstesta.osmapp.input.InputManager;
import com.jstesta.osmapp.render.OSMGLRenderer;

/**
 * Created by joseph.testa on 5/16/2017.
 */

public class OSMGLSurfaceView extends GLSurfaceView {

    private final OSMGLRenderer mRenderer;

    private HGTMap map;
    private Camera camera;
    private GestureDetectorCompat mDetector;

    public OSMGLSurfaceView(Context context){
        super(context);

        camera = new Camera(this);

        map = HGTMap.create(context, R.raw.n35e139);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new OSMGLRenderer(camera, map);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        mDetector = new GestureDetectorCompat(context, new InputManager(mRenderer, camera));
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return this.mDetector.onTouchEvent(e);
    }
}
