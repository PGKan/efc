package org.pgstyle.efc.model;

public final class Waypoint {

    public static Waypoint origin() {
        return Waypoint.point(0, 0);
    }

    public static Waypoint point(double x, double z) {
        return Waypoint.point(x, z, 128);
    }

    public static Waypoint point(double x, double z, int h) {
        return new Waypoint(false, x, 0, z, h);
    }

    public static Waypoint location(double x, double y, double z) {
        return Waypoint.location(x, y, z, 128);
    }

    public static Waypoint location(double x, double y, double z, int h) {
        return new Waypoint(true, x, y, z, h);
    }

    private Waypoint(boolean l, double x, double y, double z, int h) {
        this.l = l;
        this.x = x;
        this.y = y;
        this.z = z;
        this.h = h;
    }

    private final boolean l;
    private final double x;
    private final double y;
    private final double z;
    private final int h;

    public boolean isLocation() {
        return this.l;
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.isLocation() ? this.y : 0;
    }

    public double z() {
        return this.z;
    }

    public int h() {
        return this.h;
    }

    public double absolute() {
        return Math.sqrt(this.x() * this.x() + this.z() * this.z());
    }

    public Waypoint normalise() {
        double length = this.absolute();
        return new Waypoint(this.isLocation(), this.x() / length, 0, this.z() / length, this.h());
    }

    public Waypoint negate() {
        return new Waypoint(this.isLocation(), -this.x(), -this.y(), -this.z(), this.h());
    }

    public Waypoint translate(Waypoint location) {
        return this.translate(location.x(), location.y(), location.z());
    }

    public Waypoint translate(double x, double y, double z) {
        return new Waypoint(this.isLocation(), this.x() + x, this.y() + y, this.z() + z, this.h());
    }

    @Override
    public String toString() {
        if (this.isLocation()) {
            return String.format("L%.0f/%.0f/%.0f/%d", this.x(), this.y(), this.z(), this.h());
        }
        else {
            return String.format("P%.0f/%.0f/%d", this.x(), this.z(), this.h());
        }
    }

}
