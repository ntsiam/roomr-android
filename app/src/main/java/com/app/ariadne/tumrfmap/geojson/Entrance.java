package com.app.ariadne.tumrfmap.geojson;

public class Entrance {
    String address;
    String building;
    String city;
    String plz;
    LatLngWithTags entranceLatLngWithTags;

    public Entrance(String address, String building, String city, String plz) {
        this.address = address;
        this.building = building;
        this.city = city;
        this.plz = plz;
    }

    public Entrance(String address, String building, String city, String plz, LatLngWithTags entranceLatLngWithTags) {
        this(address, building, city, plz);
        this.entranceLatLngWithTags = entranceLatLngWithTags;
    }


    public String getAddress() {
        return  address;
    }

    public String getBuilding() {
        return building;
    }

    public String getCity() {
        return city;
    }

    public String getPlz() {
        return plz;
    }

    public LatLngWithTags getEntranceLatLngWithTags() {
        return entranceLatLngWithTags;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public void setEntranceLatLngWithTags(LatLngWithTags entranceLatLngWithTags) {
        this.entranceLatLngWithTags = entranceLatLngWithTags;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
