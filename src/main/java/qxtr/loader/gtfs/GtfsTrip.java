package qxtr.loader.gtfs;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class GtfsTrip {

    public GtfsTrip(String id) {
        this.id=id;
    }
    public String id;

    public String routeId;

    public TreeSet<GtfsStopInTrip> stops=new TreeSet<>(new Comparator<GtfsStopInTrip>() {
        @Override
        public int compare(GtfsStopInTrip o1, GtfsStopInTrip o2) {
            return o1.getSequence()-o2.getSequence();
        }
    });

    public List<String> timeTableIds=new LinkedList<>();

    public JourneyPatternDefinition getDefinition() {
        JourneyPatternDefinition result=new JourneyPatternDefinition();
        int i=0;
        for (var stop:stops) {
            result.stops.add(stop.getStopId());
            if (!stop.isAlightAllowed()) {
                result.alightRestrictions.add(i);
            }
            if (!stop.isBoardAllowed()) {
                result.boardRestrictions.add(i);
            }
            i++;
        }
        return result;
    }

}
