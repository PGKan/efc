package org.pgstyle.efc.application.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.pgstyle.efc.model.Waypoint;

public final class EfcComputingUnit {
    
    public static List<Waypoint> getWaypoints(String flightPlan) {
        List<Waypoint> list = new ArrayList<>();
        for (String waypoint : Pattern.compile("[;\\r\\n]+").split(flightPlan)) {
            String[] items = waypoint.split("/");
            if (items[0].startsWith("P")) {
                list.add(Waypoint.point(Integer.parseInt(items[0].substring(1)), Integer.parseInt(items[1]), items.length >= 3 && items[2].startsWith("H") ? Integer.parseInt(items[2].substring(1)) : 128));
            }
            else if (items[0].startsWith("L")) {
                list.add(Waypoint.location(Integer.parseInt(items[0].substring(1)), Integer.parseInt(items[1]), Integer.parseInt(items[2]), items.length >= 4 && items[3].startsWith("H") ? Integer.parseInt(items[3].substring(1)) : 128));
            }
        }
        return list;
    }

    private EfcComputingUnit() {}
}
