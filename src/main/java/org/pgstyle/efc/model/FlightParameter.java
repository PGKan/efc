package org.pgstyle.efc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
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
            this.totalDistance = this.distances.values().stream().collect(Collectors.summingDouble(Double::doubleValue));
            this.totalElevation = Connection.between(this.start, this.end).displacement().y();
            this.elevations = this.connections.stream().collect(Collectors.toMap(Function.identity(), c -> this.totalElevation * (int) c.distance() / this.totalDistance));
            this.dxdzFuels = this.connections.stream().collect(Collectors.toMap(Function.identity(), c -> {
                double fuel = 0;
                if (c.start().h() > 70) {
                    fuel = c.distance() > 700 ? c.distance() / 720 + 0.0278 : c.distance() / 700;
                }
                else if (c.start().h() > 20) {
                    int gc = c.start().h() * 10;
                    fuel = c.distance() > gc ? c.distance() / (1.029 * gc) - 0.029: c.distance() / gc;
                }
                else if (c.start().h() > 5) {
                    fuel = c.distance() / 120;
                }
                else {
                    fuel = c.distance() / 60;
                }
                return fuel;
            }));
            this.dyFuels = this.elevations.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue() / 70));
            this.nonStopFuel = Stream.concat(this.dxdzFuels.values().stream(), this.dyFuels.values().stream()).collect(Collectors.summingDouble(Double::doubleValue));
            this.suggestedFuel = (int) Math.round(this.nonStopFuel) + this.connections.size();
            this.fullPowerFuel = (int) Math.ceil(this.totalDistance / 60);
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
    private transient double totalDistance;
    private transient double totalElevation;
    private transient double nonStopFuel;
    private transient int suggestedFuel;
    private transient int fullPowerFuel;

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getTotalElevation() {
        return totalElevation;
    }

    public double getNonStopFuel() {
        return nonStopFuel;
    }

    public int getSuggestedFuel() {
        return suggestedFuel;
    }

    public int getFullPowerFuel() {
        return fullPowerFuel;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(String.format("** Flight Director/Route ", this.name));
        builder.append(String.format("%s >>> %s%n", this.start, this.end));
        builder.append(String.format(" >  Total Distance|%1$ .0f%n", this.totalDistance));
        builder.append(String.format(" > Total Elevation|%+04.0f%n", this.totalElevation));
        builder.append(String.format(" >   Non-Stop Fuel|% .0f (%.3f)%n", Math.max(0, Math.ceil(this.nonStopFuel)), this.nonStopFuel));
        builder.append(String.format(" >  Suggested Fuel|% d%n", Math.max(0, this.suggestedFuel)));
        builder.append(String.format(" > Full Power Fuel|% d%n", this.fullPowerFuel));
        builder.append(String.format("------------------------------------------------------------%n"));
        for (int i = 0; i < this.connections.size(); i++) {
            builder.append("* Waypoint ").append(this.name).append(String.format("%02d%n", i));
            Connection connection = this.connections.get(i);
            builder.append(connection).append(EfcUtils.NEWLINE);
            builder.append(String.format(" >       Heading|%+04.0f (%03.0f)%n", this.headings.get(connection), this.headings.get(connection) + 180));
            builder.append(String.format(" >      Distance|% .0f%n", this.distances.get(connection)));
            builder.append(String.format(" >     Elevation|%+04.0f%n", this.elevations.get(connection)));
            builder.append(String.format(" > Distance Fuel|%1$ .0f (%1$.3f)%n", this.dxdzFuels.get(connection)));
            builder.append(String.format(" > Altitude Fuel|% .0f (%.3f)%n", Math.ceil(this.dyFuels.get(connection)), this.dyFuels.get(connection)));
            builder.append(String.format("------------------------------------------------------------%n"));
        }
        return builder.toString();
    }



}
