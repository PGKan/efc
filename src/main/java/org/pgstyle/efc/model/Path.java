package org.pgstyle.efc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Path {
    
    public static Path startsAt(Waypoint start) {
        return new Path(start);
    }

    private Path(Waypoint start) {
        this.waypoints = new ArrayList<>();
        this.to(start);
    }

    private final List<Waypoint> waypoints;

    public Path add(int index, Waypoint waypoint) {
        this.waypoints.add(index, waypoint);
        return this;
    }

    public Path to(Waypoint waypoint) {
        return this.add(this.length(), waypoint);
    }

    public Path remove(int index) {
        if (this.waypoints.size() == 1) {
            throw new IllegalStateException();
        }
        this.waypoints.remove(index);
        return this;
    }

    public int length() {
        return this.waypoints.size();
    }

    public List<Connection> connections() {
        List<Connection> list = new ArrayList<>();
        for (int i = 1; i < this.waypoints.size(); i++) {
            list.add(Connection.between(this.waypoints.get(i - 1), this.waypoints.get(i)));
        }
        return Collections.unmodifiableList(list);
    }

    private <R> Stream<R> connections(Function<Connection, R> function) {
        Stream<R> stream = Stream.empty();
        Iterator<Waypoint> iterator = this.waypoints.iterator();
        Waypoint current = iterator.next();
        while (iterator.hasNext()) {
            Waypoint next = iterator.next();
            stream = Stream.concat(stream, Stream.of(function.apply(Connection.between(current, next))));
            current = next;
        }
        return stream;
    }

    public Stream<Double> distances() {
        return this.connections(Connection::distance);
    }

    public Stream<Double> headings() {
        return this.connections(Connection::heading);
    }

    public Stream<Connection> waypoints() {
        return this.connections(Function.identity());
    }

    @Override
    public String toString() {
        return this.waypoints.stream().map(Objects::toString).collect(Collectors.joining(" -> "));
    }

}
