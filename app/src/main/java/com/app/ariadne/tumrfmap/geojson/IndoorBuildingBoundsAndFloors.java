package com.app.ariadne.tumrfmap.geojson;

public class IndoorBuildingBoundsAndFloors {
    private BoundingBox boundingBox;
    private int minFloor;
    private int maxFloor;

    public IndoorBuildingBoundsAndFloors(BoundingBox boundingBox, int minFloor, int maxFloor) {
        this.boundingBox = boundingBox;
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
    }

    public int getMinFloor() { return this.minFloor; }
    public int getMaxFloor() { return this.maxFloor; }
    public BoundingBox getBoundingBox() { return this.boundingBox; }
}
