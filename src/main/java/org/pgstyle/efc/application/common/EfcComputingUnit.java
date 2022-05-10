package org.pgstyle.efc.application.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.pgstyle.efc.application.ApplicationException;
import org.pgstyle.efc.model.Waypoint;

public final class EfcComputingUnit {

    private static final Pattern WAYPOINT = Pattern.compile("[Pp][Xx]?(?<PX>[+-]?\\d+)/[Zz]?(?<PZ>-?[+-]?\\d+)(?:/[Hh]?(?<PH>[+-]?\\d+))?|[Ll][Xx]?(?<LX>[+-]?\\d+)/[Yy]?(?<LY>[+-]?\\d+)/[Zz]?(?<LZ>[+-]?\\d+)(?:/[Hh]?(?<LH>[+-]?\\d+))?");
    private static final Pattern DELIMITER = Pattern.compile("[ \\r\\n]+");
    
    public static List<Waypoint> getWaypoints(String flightPlan) {
        List<Waypoint> list = new ArrayList<>();
        for (String waypoint : EfcComputingUnit.DELIMITER.split(flightPlan)) {
            Matcher matcher = EfcComputingUnit.WAYPOINT.matcher(waypoint);
            if (!waypoint.isEmpty()) {
                if (matcher.matches()) {
                    if (Objects.nonNull(matcher.group("PX"))) {
                        list.add(Waypoint.point(Integer.parseInt(matcher.group("PX")),
                                                Integer.parseInt(matcher.group("PZ")),
                                                Optional.ofNullable(matcher.group("PH")).map(Integer::parseInt).orElse(128)));
                    }
                    else {
                        list.add(Waypoint.location(Integer.parseInt(matcher.group("LX")),
                                                   Integer.parseInt(matcher.group("LY")),
                                                   Integer.parseInt(matcher.group("LZ")),
                                                   Optional.ofNullable(matcher.group("LH")).map(Integer::parseInt).orElse(128)));
                    }
                }
                else {
                    throw new ApplicationException("Syntax error: \"" + waypoint + "\"");
                }
            }
        }
        if (list.isEmpty()) {
            throw new ApplicationException("Empty flight plan");
        }
        return list;
    }

    private EfcComputingUnit() {}
}
