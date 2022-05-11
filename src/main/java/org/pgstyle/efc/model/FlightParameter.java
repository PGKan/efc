package org.pgstyle.efc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pgstyle.efc.application.common.EfcUtils;

public final class FlightParameter {

    public static FlightParameter of(String name, List<Connection> connections) {
        return new FlightParameter(name, Collections.unmodifiableList(new ArrayList<>(connections)));
    }

    private FlightParameter(String name, List<Connection> connections) {
        this.name = name;
        this.connections = connections;
        this.start = this.connections.get(0).start();
        this.end = this.connections.get(this.connections.size() - 1).end();
        this.distances = this.connections.stream().collect(Collectors.toMap(Function.identity(), c -> (int) c.distance()));
        this.headings = this.connections.stream().collect(Collectors.toMap(Function.identity(), c -> (int) c.heading()));
        int totalDistance = this.distances.values().stream().collect(Collectors.summingInt(i -> i));
        int totalElevation = Connection.between(this.start, this.end).displacement().y();
        this.elevations = this.connections.stream().collect(Collectors.toMap(Function.identity(), c -> totalElevation * (int) c.distance() / totalDistance));
        this.dxdzFuels = this.connections.stream().collect(Collectors.toMap(Function.identity(), c -> 0f));
        this.dyFuels = this.connections.stream().collect(Collectors.toMap(Function.identity(), c -> 0f));
        this.nonStopFuel = 0;
        this.suggestedFuel = 0;
        this.fullPowerFuel = 0;
    }

    private final String name;
    private final List<Connection> connections;

    private transient Waypoint start;
    private transient Waypoint end;
    private transient Map<Connection, Integer> distances;
    private transient Map<Connection, Integer> headings;
    private transient Map<Connection, Integer> elevations;
    private transient Map<Connection, Float> dxdzFuels;
    private transient Map<Connection, Float> dyFuels;
    private transient float nonStopFuel;
    private transient int suggestedFuel;
    private transient int fullPowerFuel;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Flight Director/Route ").append(this.name).append(EfcUtils.NEWLINE);
        builder.append(this.start).append(" >>> ").append(this.end).append(EfcUtils.NEWLINE);
        builder.append("Non-Stop Fuel:   ").append(String.format("%1$.3f (%1$.0f)", this.nonStopFuel)).append(EfcUtils.NEWLINE);
        builder.append("Suggested Fuel:  ").append(String.format("%d", this.suggestedFuel)).append(EfcUtils.NEWLINE);
        builder.append("Full Power Fuel: ").append(String.format("%d", this.suggestedFuel)).append(EfcUtils.NEWLINE);
        for (int i = 0; i < this.connections.size(); i++) {
            builder.append("Route ").append(this.name).append(String.format("%03d", i)).append(EfcUtils.NEWLINE);
            Connection connection = this.connections.get(i);
            builder.append(connection).append(EfcUtils.NEWLINE);
            builder.append("MC Heading:    ").append(String.format("%+03d", this.headings.get(connection))).append(EfcUtils.NEWLINE);
            builder.append("True Heading:  ").append(String.format("%03d", this.headings.get(connection) + 180)).append(EfcUtils.NEWLINE);
            builder.append("Distance:      ").append(this.distances.get(connection)).append(EfcUtils.NEWLINE);
            builder.append("Elevation:     ").append(String.format("%+d", this.elevations.get(connection))).append(EfcUtils.NEWLINE);
            builder.append("Distance Fuel: ").append(String.format("%1$.3f (%1$.0f)", this.dxdzFuels.get(connection))).append(EfcUtils.NEWLINE);
            builder.append("Altitude Fuel: ").append(String.format("%1$.3f (%1$.0f)", this.dyFuels.get(connection))).append(EfcUtils.NEWLINE);
        }
        return builder.toString();
    }



}
