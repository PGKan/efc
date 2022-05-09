package org.pgstyle.efc.model;

public final class Waypoint {

    public static Waypoint point(int x, int z) {
        return Waypoint.point(x, z, 128);
    }

    public static Waypoint point(int x, int z, int h) {
        return new Waypoint(false, x, 0, z, h);
    }

    public static Waypoint location(int x, int y, int z) {
        return Waypoint.location(x, y, z, 128);
    }

    public static Waypoint location(int x, int y, int z, int h) {
        return new Waypoint(true, x, y, z, h);
    }

    private Waypoint(boolean l, int x, int y, int z, int h) {
        this.l = l;
        this.x = x;
        this.y = y;
        this.z = z;
        this.h = h;
    }

    private final boolean l;
    private final int x;
    private final int y;
    private final int z;
    private final int h;

    public boolean isLocation() {
        return this.l;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.isLocation() ? this.y : 0;
    }

    public int z() {
        return this.z;
    }

    public int h() {
        return this.h;
    }

    public int absolute() {
        return (int) Math.sqrt(this.x() * this.x() + this.y() * this.y() + this.z() * this.z());
    }

    public Waypoint normalise() {
        int length = this.absolute();
        return new Waypoint(this.isLocation(), this.x() / length, -this.y() / length, -this.z() / length, this.h());
    }

    public Waypoint negate() {
        return new Waypoint(this.isLocation(), -this.x(), -this.y(), -this.z(), this.h());
    }

    public Waypoint translate(Waypoint location) {
        return this.translate(location.x(), location.y(), location.z());
    }

    public Waypoint translate(int x, int y, int z) {
        return new Waypoint(this.isLocation(), this.x() + x, this.y() + y, this.z() + z, this.h());
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
