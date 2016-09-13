/**
 *
 */
package com.robert.pin.bean;

/**
 * 地图坐标
 */
public class Coordinate{
    //经度
    private double longitude;
    //维度
    private double latitude;

    public Coordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
