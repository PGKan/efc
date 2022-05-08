package org.pgstyle.efc.model;

public final class Waypoint {

    public static Waypoint at(float x, float z) {
        return Waypoint.at(x, z, 128);
    }

    public static Waypoint at(float x, float z, int h) {
        return new Waypoint(false, x, 0, z, h);
    }

    public static Waypoint at(float x, float y, float z) {
        return new Waypoint(true, x, y, z, 0);
    }

    public static Waypoint at(float x, float y, float z, int h) {
        return new Waypoint(true, x, y, z, h);
    }

    private Waypoint(boolean l, float x, float y, float z, int h) {
        this.l = l;
        this.x = x;
        this.y = y;
        this.z = z;
        this.h = h;
    }

    private final boolean l;
    private final float x;
    private final float y;
    private final float z;
    private final int h;

    public boolean isLocation() {
        return this.l;
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }

    public float h() {
        return this.h;
    }

    public float absolute() {
        return (float) Math.sqrt(this.x() * this.x() + this.y() * this.y() + this.z() * this.z());
    }

    public Waypoint normalise() {
        float length = this.absolute();
        return Waypoint.at(this.x() / length, -this.y() / length, -this.z() / length);
    }

    public Waypoint negate() {
        return Waypoint.at(-this.x(), -this.y(), -this.z());
    }

    public Waypoint translate(Waypoint location) {
        return this.translate(location.x(), location.y(), location.z());
    }

    public Waypoint translate(float x, float y, float z) {
        return Waypoint.at(this.x() + x, this.y() + y, this.z() + z);
    }

    @Override
    public String toString() {
        if (this.isLocation()) {
            return String.format("[%.1f,%.1f,%.1f/%d]", this.x(), this.y(), this.z(), this.h());
        }
        else {
            return String.format("[%.1f,%.1f/%d]", this.x(), this.z(), this.h());
        }
    }

}
