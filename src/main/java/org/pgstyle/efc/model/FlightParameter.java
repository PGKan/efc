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
        if (!this.connections.isEmpty()) {
            this.start = this.connections.get(0).start();
            this.end = this.connections.get(this.connections.size() - 1).end();
            this.distances = this.connections.stream().collect(Collectors.toMap(Function.identity(), Connection::distance));
            this.headings = this.connections.stream().collect(Collectors.toMap(Function.identity(), Connection::heading));
            double totalDistance = this.distances.values().stream().collect(Collectors.summingDouble(d -> d));
            double totalElevation = Connection.between(this.start, this.end).displacement().y();
            this.elevations = this.connections.stream().collect(Collectors.toMap(Function.identity(), c -> totalElevation * (int) c.distance() / totalDistance));
            this.dxdzFuels = this.connections.stream().collect(Collectors.toMap(Function.identity(), c -> 0.0));
            this.dyFuels = this.connections.stream().collect(Collectors.toMap(Function.identity(), c -> 0.0));
            this.nonStopFuel = 0;
            this.suggestedFuel = 0;
            this.fullPowerFuel = 0;
        }
        else {
            this.start = null;
            this.end = null;
            this.distances = Collections.EMPTY_MAP;
            this.headings = Collections.EMPTY_MAP;
            this.elevations = Collections.EMPTY_MAP;
            this.dxdzFuels = Collections.EMPTY_MAP;
            this.dyFuels = Collections.EMPTY_MAP;
            this.nonStopFuel = 0;
            this.suggestedFuel = 0;
            this.fullPowerFuel = 0;
        }
    }

    private final String name;
    private final List<Connection> connections;

    private transient Waypoint start;
    private transient Waypoint end;
    private transient Map<Connection, Double> distances;
    private transient Map<Connection, Double> headings;
    private transient Map<Connection, Double> elevations;
    private transient Map<Connection, Double> dxdzFuels;
    private transient Map<Connection, Double> dyFuels;
    private transient double nonStopFuel;
    private transient int suggestedFuel;
    private transient int fullPowerFuel;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("** Flight Director/Route ").append(this.name).append(EfcUtils.NEWLINE);
        builder.append(this.start).append(" >>> ").append(this.end).append(EfcUtils.NEWLINE);
        builder.append(String.format(" >   Non-Stop Fuel| %1$.0f (%1$.3f)", this.nonStopFuel)).append(EfcUtils.NEWLINE);
        builder.append(String.format(" >  Suggested Fuel| %d", this.suggestedFuel)).append(EfcUtils.NEWLINE);
        builder.append(String.format(" > Full Power Fuel| %d", this.fullPowerFuel)).append(EfcUtils.NEWLINE);
        builder.append("------------------------------------------------------------").append(EfcUtils.NEWLINE);
        for (int i = 0; i < this.connections.size(); i++) {
            builder.append("Route ").append(this.name).append(String.format("%02d%n", i));
            Connection connection = this.connections.get(i);
            builder.append(connection).append(EfcUtils.NEWLINE);
            builder.append(String.format(" >       Heading|%+04.0f (%03.0f)%n", this.headings.get(connection), this.headings.get(connection) + 180));
            builder.append(String.format(" >      Distance| %.0f%n", this.distances.get(connection)));
            builder.append(String.format(" >     Elevation|%+04.0f%n", this.elevations.get(connection)));
            builder.append(String.format(" > Distance Fuel| %1$.0f (%1$.3f)%n", this.dxdzFuels.get(connection)));
            builder.append(String.format(" > Altitude Fuel| %1$.0f (%1$.3f)%n", this.dyFuels.get(connection)));
            builder.append("------------------------------------------------------------").append(EfcUtils.NEWLINE);
        }
        return builder.toString();
    }



}
