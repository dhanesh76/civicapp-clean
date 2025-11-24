package com.visioners.civic.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Small helper utilities for geospatial conversions and validation.
 */
public final class GeoUtils {

    private GeoUtils() {}

    public static Point toPoint(GeometryFactory gf, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("latitude and longitude must be provided");
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude out of range: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude out of range: " + longitude);
        }

        Point p = gf.createPoint(new Coordinate(longitude, latitude)); // x=lon, y=lat
        p.setSRID(4326);
        return p;
    }

    public static Double getLatitude(Point p) {
        return p == null ? null : p.getY();
    }

    public static Double getLongitude(Point p) {
        return p == null ? null : p.getX();
    }
}
