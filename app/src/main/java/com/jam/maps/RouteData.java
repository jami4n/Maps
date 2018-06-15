package com.jam.maps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jam on 04-05-2017.
 */

public class RouteData {
    LatLng originLocation;
    LatLng destinationLocation;
    LatLng northeastbound;
    LatLng southwestbound;
    String polyline;
    int zoomLevel;

    public RouteData(LatLng originLocation, LatLng destinationLocation, LatLng northeastbound, LatLng southwestbound, String polyline, int zoomLevel) {
        this.originLocation = originLocation;
        this.destinationLocation = destinationLocation;
        this.northeastbound = northeastbound;
        this.southwestbound = southwestbound;
        this.polyline = polyline;
        this.zoomLevel = zoomLevel;
    }

    public LatLng getOriginLocation() {
        return originLocation;
    }

    public void setOriginLocation(LatLng originLocation) {
        this.originLocation = originLocation;
    }

    public LatLng getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(LatLng destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public LatLng getNortheastbound() {
        return northeastbound;
    }

    public void setNortheastbound(LatLng northeastbound) {
        this.northeastbound = northeastbound;
    }

    public LatLng getSouthwestbound() {
        return southwestbound;
    }

    public void setSouthwestbound(LatLng southwestbound) {
        this.southwestbound = southwestbound;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
    }
}
