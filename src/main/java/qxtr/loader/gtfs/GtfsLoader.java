package qxtr.loader.gtfs;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import qxtr.loader.LoaderInput;
import qxtr.loader.LoaderInterface;
import qxtr.model.transport.dataset.DataSet;
import qxtr.model.transport.dataset.DataSetImport;
import qxtr.model.transport.dataset.common.IdentifiedDSEntity;
import qxtr.model.transport.dataset.schedules.*;
import qxtr.model.transport.dataset.topology.*;
import qxtr.utils.CsvReader;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GtfsLoader implements LoaderInterface {

    public GtfsLoader(DataSet dataSet) {
        this.dataSetImport = new DataSetImport(dataSet);
    }

    @Override
    public DataSetImport load(DataSet dataSet, LoaderInput input) {
        return null;
    }

    private DataSetImport dataSetImport;

    private final IdentityHashMap<Class<? extends IdentifiedDSEntity>,HashMap<String,? extends IdentifiedDSEntity>> identifiedDSEntities=new IdentityHashMap<>();

    private final IdentityHashMap<VehicleJourney, Route> vehicleJourneyRoutes = new IdentityHashMap<>();

    /**
     * Record used to put the data read in the time.txt file for further JourneyPattern and VehicleJourney creation
     *
     * The equals and hashCode are defined in order to be used by the JourneyPatternDifferentiator:
     * They take into account only the stop, alightAllowed and boardAllowed fields.
     */
    public record VehicleJourneyStop(Stop stop, short position, int aimedArrival, int aimedDeparture,
                                     boolean alightAllowed, boolean boardAllowed) implements Comparable<VehicleJourneyStop> {
        @Override
        public int compareTo(VehicleJourneyStop o) {
            return position-o.position;
        }


        @Override
        public boolean equals(Object o) {
            if (o==this) return true;
            if (o==null) return false;
            // Don't check the class because it will never be called with another class
            var that=(VehicleJourneyStop)o;
            // Don't use position nor schedules because we just want equivalence for JourneyPattern
            return stop==that.stop && alightAllowed==that.alightAllowed && boardAllowed==that.boardAllowed;
        }

        @Override
        public int hashCode() {
            // Don't use position nor schedules because we just want equivalence for JourneyPattern
            return Objects.hash(stop,alightAllowed,boardAllowed);
        }

        public boolean dontWaitAtStop() {
            return aimedArrival==aimedDeparture;
        }
    }

    private final IdentityHashMap<VehicleJourney,TreeSet<VehicleJourneyStop>> vehicleJourneyStops=new IdentityHashMap<>();


    private void addVehicleJourneyStop(VehicleJourney vehicleJourney,VehicleJourneyStop vehicleJourneyStop) {
        vehicleJourneyStops.computeIfAbsent(vehicleJourney,v->new TreeSet<>()).add(vehicleJourneyStop);
    }

    private <T extends IdentifiedDSEntity> T getIdentifiedDSEntity(Class<T> clazz,String identifier) {
        return  getIdentifiedDSEntityMap(clazz)
                .computeIfAbsent(identifier,id-> { try {
                    return clazz.getConstructor(DataSetImport.class, String.class).newInstance(dataSetImport, id);
                  } catch (Exception e) {
                    e.printStackTrace();
                    return null;}
                });
    }

    @SuppressWarnings("unchecked")
    private <T extends IdentifiedDSEntity> HashMap<String,T> getIdentifiedDSEntityMap(Class<T> clazz) {
        return (HashMap<String, T>) identifiedDSEntities
                .computeIfAbsent(clazz,aClass -> new HashMap<String,T>());
    }

    private final static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public DataSetImport load(InputStream zipData) throws IOException {
        ZipInputStream zstream = new ZipInputStream(zipData);
        ZipEntry entry;
        while ((entry = zstream.getNextEntry()) != null) {
            switch (entry.getName()) {
                case "agency.txt" -> loadAgencies(zstream);
                case "stops.txt" -> loadStops(zstream);
                case "routes.txt" -> loadRoutes(zstream);
                case "trips.txt" -> loadTrips(zstream);
                case "stop_times.txt" -> loadTimes(zstream);
                case "calendar.txt" -> loadCalendar(zstream);
                case "calendar_dates.txt" -> loadCalendarDates(zstream);
            }
        }
        zstream.close();
        makeJourneyPatterns();
        return dataSetImport;
    }

    private static Calendar cal=Calendar.getInstance();
    long start;

    private void log(String message) {
        System.out.print(message+"...");
        start=System.nanoTime();
    }

    private void done() {
        var duration=System.nanoTime()-start;
        System.out.format("\t\tdone in %,16d nanoseconds\n",duration);
    }

    private void loadAgencies(InputStream agencyStream) throws IOException {
        log("Loading Agencies");
        var reader=new CsvReader(agencyStream);
        int AGENCY_ID=reader.columnIndex("agency_id");
        int AGENCY_NAME=reader.columnIndex("agency_name");
        reader.forEach(line -> getIdentifiedDSEntity(Agency.class,line.get(AGENCY_ID)).setName(line.get(AGENCY_NAME)));
        done();
    }

    private void loadStops(InputStream stopsStream) throws IOException {
        log("Loading Stops");
        CsvReader.reader(stopsStream).forEach(line -> {
            var type = line.get("location_type", "0");
            Point point = null;
            if (type.equals("0") || type.equals("1")) {
                String lat = line.get("stop_lat", null);
                String lon = line.get("stop_lon", null);
                if (lat != null && lon != null) {
                    point = geometryFactory.createPoint(new Coordinate(Float.parseFloat(lon), Float.parseFloat(lat)));
                }
            }
            var stopName=line.get("stop_name");
            var stopId = line.get("stop_id");
            if (type.equals("0")) {
                Stop stop = getIdentifiedDSEntity(Stop.class,stopId);
                stop.setName(stopName);
                String stopGroupId = line.get("parent_station","");
                if (!stopGroupId.isEmpty()) {
                    stop.setStopGroup(getIdentifiedDSEntity(StopGroup.class,stopGroupId));
                } else {
                    var stopGroup=getIdentifiedDSEntity(StopGroup.class,stopName);
                    stopGroup.setName(stopName);
                    stop.setStopGroup(stopGroup);
                }
                if (point != null) {
                    stop.setLocation(point);
                }
            } else if (type.equals("1")) {

                var stopGroup = getIdentifiedDSEntity(StopGroup.class,stopId);
                stopGroup.setName(stopName);
                if (point != null) {
                    stopGroup.setLocation(point);
                }
            }
        });
        done();
    }

    private static int parseTime(String text) {
        var vals=text.split(":");
        return Integer.parseInt(vals[0])*3600+Integer.parseInt(vals[1])*60+Integer.parseInt(vals[2]);
    }

    private void loadTimes(InputStream stopTimesStream) throws IOException {
        log("Loading Times");
        var reader=new CsvReader(stopTimesStream);
        var TRIP_ID=reader.columnIndex("trip_id");
        var ARRIVAL_TIME=reader.columnIndex("arrival_time");
        var DEPARTURE_TIME=reader.columnIndex("departure_time");
        var STOP_SEQUENCE=reader.columnIndex("stop_sequence");
        var PICKUP_TYPE=reader.columnIndex("pickup_type");
        var DROP_OFF_TYPE=reader.columnIndex("drop_off_type");
        var STOP_ID=reader.columnIndex("stop_id");
        reader.forEach(line -> {
            addVehicleJourneyStop(
                    getIdentifiedDSEntity(VehicleJourney.class,line.get(TRIP_ID)),
                    new VehicleJourneyStop(
                        getIdentifiedDSEntity(Stop.class,line.get(STOP_ID)),
                        Short.parseShort(line.get(STOP_SEQUENCE)),
                        parseTime(line.get(ARRIVAL_TIME)),
                        parseTime(line.get(DEPARTURE_TIME)),
                        !line.get(DROP_OFF_TYPE, "0").equals("1"),
                        !line.get(PICKUP_TYPE, "0").equals("1")));
        });
        done();
    }

    private void loadCalendar(InputStream calendarStream) throws IOException {
        log("Loading Calendar");
        CsvReader.reader(calendarStream).forEach(line -> {
            var start = LocalDate.parse(line.get("start_date"), DateTimeFormatter.BASIC_ISO_DATE);
            var end = LocalDate.parse(line.get("end_date"), DateTimeFormatter.BASIC_ISO_DATE);
            byte days = 0;
            for (var day : DayOfWeek.values()) {
                if (line.get(day.name().toLowerCase(), "0").equals("1")) {
                    days |= ((byte) 1 << (day.getValue() - 1));
                }
            }
            var timeTable = getIdentifiedDSEntity(TimeTable.class,line.get("service_id"));
            timeTable.setValidDays(days);
            timeTable.getPeriods().add(new Period(start, end));
        });
        done();
    }

    private void loadCalendarDates(InputStream calendarDatesStream) throws IOException {
        log("Loading Dates");
        CsvReader.reader(calendarDatesStream).forEach(line -> {
            var timeTable = getIdentifiedDSEntity(TimeTable.class,line.get("service_id"));
            var date = LocalDate.parse(line.get("date"), DateTimeFormatter.BASIC_ISO_DATE);
            boolean in_out = line.get("exception_type", "1").equals("1");
            timeTable.getDays().add(new TimeTableDay(date, in_out));
        });
        done();
    }

    private void loadTrips(InputStream tripStream) throws IOException {
        log("Loading Trips");
        CsvReader.reader(tripStream).forEach(line -> {
            var vehicleJourney = getIdentifiedDSEntity(VehicleJourney.class,line.get("trip_id"));
            vehicleJourney.getTimeTables().add(getIdentifiedDSEntity(TimeTable.class,line.get("service_id")));
            var direction=line.get("direction_id","0");
            var routeId=line.get("route_id")+':'+direction;
            var route=getIdentifiedDSEntity(Route.class,routeId);
            if (route.getLine()==null) {
                route.setLine(getIdentifiedDSEntity(Line.class,line.get("route_id")));
            }
            vehicleJourneyRoutes.put(vehicleJourney, route);
        });
        done();
    }

    private void loadRoutes(InputStream routesStream) throws IOException {
        log("Loading Routes");
        CsvReader.reader(routesStream).forEach(line -> {
            var theLine = getIdentifiedDSEntity(Line.class,line.get("route_id"));
            theLine.setShortName(line.get("route_short_name", null));
            theLine.setName(line.get("route_long_name", null));
            theLine.setAgency(getIdentifiedDSEntity(Agency.class,line.get("agency_id")));
        });
        done();
    }

    /**
     * Class used to discriminate the VehicleJourneys
     *
     * The equals ensure that the route is the same and that the stops are the same,
     * in the same order and with the same board and alight restrictions, ie it is the same JourneyPattern
     *
     * The hash code is coherent of course
     *
     */
    private class JourneyPatternDifferentiator {

        public final VehicleJourney vehicleJourney;
        public final Route route;
        /*
         * Using stops.toArray() because the TreeSet hashCode() doesn't take into account the order of the elements.
         *
         * The VehicleJourneyStop hashCode() doesn't use the position (We are not sure that the positions starts
         * at 0 and doesn't contain 'holes').
         *
         * So otherwise it could easily lead to same hashcode for 2 different patterns if the journey contains
         * the same stops in another order (For example in the probable case of a first pattern and the
         * corresponding return pattern).
         *
         * This same hashCode() would lead to unnecessary equals() calls.
         *
         */
        public final VehicleJourneyStop[] stops;

        public JourneyPatternDifferentiator(VehicleJourney vehicleJourney) {
            this.vehicleJourney = vehicleJourney;
            this.route=vehicleJourneyRoutes.get(vehicleJourney);
            this.stops=vehicleJourneyStops.get(vehicleJourney).toArray(new VehicleJourneyStop[0]);
        }

        /**
         * @param o
         * @return true if the route is the same and the VehicleJourneys have the same structure => Same JourneyPattern
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            // Don't check the class because it will never be called with another class
            JourneyPatternDifferentiator that = (JourneyPatternDifferentiator) o;
            if (that.route!=route) return false;
            return Arrays.equals(stops,that.stops);
        };

        //Cache the hash for performance improvment
        Integer hash=null;

        @Override
        public int hashCode() {
            if (hash==null) {
                hash = Objects.hash(route, Arrays.hashCode(stops));
            }
            return hash;
        }
    }

    private void makeJourneyPatterns() {
        log("Making Patterns");

        HashMap<JourneyPatternDifferentiator, List<VehicleJourney>> journeys = new HashMap<>();

        getIdentifiedDSEntityMap(VehicleJourney.class).forEach((id,vehicleJourney)-> {
            journeys.computeIfAbsent(new JourneyPatternDifferentiator(vehicleJourney), k->new ArrayList<>()).add(vehicleJourney);
        });

        IdentityHashMap<Route,Integer> nbJourneyPatternsFound=new IdentityHashMap<>();

        journeys.forEach((differentiator,vehicleJourneys)-> {

            var route=differentiator.route;


            //create the journey pattern
            int nb=nbJourneyPatternsFound.computeIfAbsent(route,r->0);
            nbJourneyPatternsFound.put(route,nb+1);
            String journeyPatternId=route.getExternalId()+':'+nb;
            var journeyPattern=getIdentifiedDSEntity(JourneyPattern.class,journeyPatternId);
            journeyPattern.setRoute(route);


            //create the JourneyPatternStops
            AtomicInteger i= new AtomicInteger(0);
            journeyPattern.setJourneyPatternStops(
                    Arrays.stream(differentiator.stops)
                            .map(vehicleJourneyStop -> {
                                String journeyPatternStopId=journeyPatternId+':'+(i.get());
                                var journeyPatternStop=getIdentifiedDSEntity(JourneyPatternStop.class,journeyPatternStopId);
                                journeyPatternStop.setStop(vehicleJourneyStop.stop);
                                journeyPatternStop.setAlightAllowed(vehicleJourneyStop.alightAllowed);
                                journeyPatternStop.setBoardAllowed(vehicleJourneyStop.boardAllowed);
                                journeyPatternStop.setPosition((short) i.getAndIncrement());
                                return journeyPatternStop;
                            }).toList());


            //set the JourneyPatternStops for all the vehicleJourneys, recompute their positions in order to start at 0 and eliminate holes
            vehicleJourneys.forEach(vehicleJourney -> {
                vehicleJourney.setJourneyPattern(journeyPattern);
                var stops=vehicleJourneyStops.get(vehicleJourney);
                boolean waitAtStop=!stops.stream().allMatch(VehicleJourneyStop::dontWaitAtStop);
                int[] arrivals=stops.stream().mapToInt(VehicleJourneyStop::aimedArrival).toArray();
                if (!waitAtStop) {
                    vehicleJourney.setSchedules(new VehicleJourneySchedules(arrivals));
                } else {
                    int[] departures=stops.stream().mapToInt(VehicleJourneyStop::aimedDeparture).toArray();
                    vehicleJourney.setSchedules(new VehicleJourneySchedules(arrivals,departures));
                }
            });


        });
        done();
    }

}
