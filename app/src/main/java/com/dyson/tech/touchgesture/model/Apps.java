package com.dyson.tech.touchgesture.model;

import android.gesture.Gesture;
import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Apps implements Serializable {
    private Drawable icon;
    private String name;
    private String strPackage;
    private Gesture gesture;
    public Apps() {
    }

    public Apps(Drawable icon, String name, String strPackage, Gesture gesture) {
        this.icon = icon;
        this.name = name;
        this.strPackage = strPackage;
        this.gesture = gesture;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStrPackage() {
        return strPackage;
    }

    public void setStrPackage(String strPackage) {
        this.strPackage = strPackage;
    }

    public Gesture getGesture() {
        return gesture;
    }

    public void setGesture(Gesture gesture) {
        this.gesture = gesture;
    }
}
