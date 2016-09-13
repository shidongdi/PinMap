package com.robert.pin.bean;

/**
 * 讲解点信息
 */
public class ScenicBean {
    private double longitude;
    private double latitude;
    private String scenic_name;
    private int seq;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getScenic_name() {
        return scenic_name;
    }

    public void setScenic_name(String scenic_name) {
        this.scenic_name = scenic_name;
    }

}
