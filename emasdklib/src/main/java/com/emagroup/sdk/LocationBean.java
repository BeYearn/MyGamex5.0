package com.emagroup.sdk;

/**
 * Created by Administrator on 2017/2/9.
 *
 * 包含位置信息
 */

public class LocationBean {

    public String country = "N/A";
    public String city = "N/A";
    double latitude = 0.0;
    double longitude = 0.0;
    double altitude = 0.0;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
