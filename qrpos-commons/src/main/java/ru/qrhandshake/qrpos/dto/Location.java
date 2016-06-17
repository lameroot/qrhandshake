package ru.qrhandshake.qrpos.dto;

/**
 * Created by lameroot on 18.05.16.
 */
public class Location {

    private double lat;
    private double lon;

    public Location(){}
    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
