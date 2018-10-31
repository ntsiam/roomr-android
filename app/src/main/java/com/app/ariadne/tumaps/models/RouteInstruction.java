package com.app.ariadne.tumaps.models;

import com.google.android.gms.maps.model.LatLng;

public class RouteInstruction {
    private String instruction;
    private LatLng point;
    private int level;

    public RouteInstruction(String instruction, LatLng point, int level) {
        this.instruction = instruction;
        this.point = point;
        this.level = level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public void setPoint(LatLng point) {
        this.point = point;
    }

    public int getLevel() {
        return level;
    }

    public LatLng getPoint() {
        return point;
    }

    public String getInstruction() {
        return instruction;
    }
}
