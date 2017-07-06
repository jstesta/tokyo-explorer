package com.jstesta.osmapp.render;

public class ViewFrame {

    private final Vector4f v;

    public ViewFrame(float left, float right, float bottom, float top) {
        v = new Vector4f(left, right, bottom, top);
    }

    public float getLeft() {
        return v.getX();
    }

    public float getRight() {
        return v.getY();
    }

    public float getBottom() {
        return v.getZ();
    }

    public float getTop() {
        return v.getW();
    }

    public void setLeft(float n) {
        v.setX(n);
    }

    public void setRight(float n) {
        v.setY(n);
    }

    public void setBottom(float n) {
        v.setZ(n);
    }

    public void setTop(float n) {
        v.setW(n);
    }
}
