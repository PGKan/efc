package org.pgstyle.efc.model;

public final class Connection {

    public static Connection between(Waypoint start, Waypoint end) {
        return new Connection(start, end);
    }

    private Connection(Waypoint start, Waypoint end) {
        this.start = start;
        this.end = end;
    }

    private final Waypoint start;
    private final Waypoint end;

    public Waypoint displacement() {
        return this.end.translate(this.start.negate());
    }

    public float distance() {
        return this.displacement().absolute();
    }

    public Waypoint vector() {
        return this.displacement().normalise();
    }

    public float heading() {
        Waypoint vector = this.vector().negate();
        return (float) (Math.atan2(vector.x(), vector.z()) * 180 / Math.PI);
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", this.start, this.end);
    }

}