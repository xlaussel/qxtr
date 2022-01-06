package qxtr.loader.gtfs;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import qxtr.loader.LoaderInput;
import qxtr.loader.LoaderInterface;
import qxtr.model.dataset.DataSet;
import qxtr.model.dataset.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;
import qxtr.model.common.StopTime;
import qxtr.model.schedules.*;
import qxtr.model.topology.*;
import qxtr.utils.CsvReader;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    private record VehicleJourneyStopAuthorization(boolean boardAllowed,boolean alightAllowed) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VehicleJourneyStopAuthorization that = (VehicleJourneyStopAuthorization) o;
            return boardAllowed == that.boardAllowed && alightAllowed == that.alightAllowed;
        }

        @Override
        public int hashCode() {
            return (boardAllowed?1:0)+(alightAllowed?2:0);
        }
    }

    private final IdentityHashMap<VehicleJourneyStop,VehicleJourneyStopAuthorization> vehicleJourneyStopAuthorizations=new IdentityHashMap<>();

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

    public void load(InputStream zipData) throws IOException {
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
    }

    private void log(String message) {
        System.out.println(message);
    }

    private void loadAgencies(InputStream agencyStream) throws IOException {
        log("Loading Agencies");
        var reader=new CsvReader(agencyStream);
        int AGENCY_ID=reader.columnIndex("agency_id");
        int AGENCY_NAME=reader.columnIndex("agency_name");
        reader.forEach(line -> getIdentifiedDSEntity(Agency.class,line.get(AGENCY_ID)).setName(line.get(AGENCY_NAME)));
        log("Done");
    }

    private void loadStops(InputStream stopsStream) throws IOException {
        log("Loading Stop");
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
            if (type.equals("0")) {
                String stopId = line.get("stop_id");
                var stopName=line.get("stop_name");
                Stop stop = getIdentifiedDSEntity(Stop.class,stopId);
                stop.setName(stopName);
                String stopGroupId = line.get("parent_station", null);
                if (stopGroupId != null) {
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
                var stopGroup = getIdentifiedDSEntity(StopGroup.class,line.get("stop_id"));
                stopGroup.setName(line.get("stop_name"));
                if (point != null) {
                    stopGroup.setLocation(point);
                }
            }
        });
        log("Done");
    }

    private void loadTimes(InputStream stopTimesStream) throws IOException {
        log("Loading Times");
        CsvReader.reader(stopTimesStream).forEach(line -> {
            var vehicleJourney = getIdentifiedDSEntity(VehicleJourney.class,line.get("trip_id"));
            StopTime arrivaltime = StopTime.parse(line.get("arrival_time"));
            StopTime departuretime = StopTime.parse(line.get("departure_time"));
            short sequence = Short.parseShort(line.get("stop_sequence"));
            boolean boardAllowed = !line.get("pickup_type", "0").equals("1");
            boolean alightAllowed = !line.get("drop_off_type", "0").equals("1");
            VehicleJourneyStop vehicleJourneyStop = new VehicleJourneyStop(dataSetImport, vehicleJourney, sequence, arrivaltime, departuretime);
            vehicleJourneyStopAuthorizations.put(vehicleJourneyStop,new VehicleJourneyStopAuthorization(boardAllowed,alightAllowed));
            vehicleJourneyStop.setStop(getIdentifiedDSEntity(Stop.class,line.get("stop_id")));
        });
        log("Done");
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
        log("Done");
    }

    private void loadCalendarDates(InputStream calendarDatesStream) throws IOException {
        log("Loading Dates");
        CsvReader.reader(calendarDatesStream).forEach(line -> {
            var timeTable = getIdentifiedDSEntity(TimeTable.class,line.get("service_id"));
            var date = LocalDate.parse(line.get("date"), DateTimeFormatter.BASIC_ISO_DATE);
            boolean in_out = line.get("exception_type", "1").equals("1");
            timeTable.getDays().add(new TimeTableDay(date, in_out));
        });
        log("Done");
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
        log("Done");
    }

    private void loadRoutes(InputStream routesStream) throws IOException {
        log("Loading Routes");
        CsvReader.reader(routesStream).forEach(line -> {
            var theLine = getIdentifiedDSEntity(Line.class,line.get("route_id"));
            theLine.setShortName(line.get("route_short_name", null));
            theLine.setName(line.get("route_long_name", null));
            theLine.setAgency(getIdentifiedDSEntity(Agency.class,line.get("agency_id")));
        });
        log("Done");
    }

    /**
     * Class used to discriminate the VehicleJourney
     */
    private class JourneyPatternDiscriminator {

        public final VehicleJourney vehicleJourney;
        public final Route route;

        public JourneyPatternDiscriminator(VehicleJourney vehicleJourney,Route route) {
            this.vehicleJourney = vehicleJourney;
            this.route=route;
        }

        /**
         * @param o
         * @return true if the route is the same and the VehicleJourneys have the same structure => Same JourneyPattern
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JourneyPatternDiscriminator that = (JourneyPatternDiscriminator) o;
            if (that.route!=route) return false;
            if (vehicleJourney.getVehicleJourneyStops().size()!=that.vehicleJourney.getVehicleJourneyStops().size()) return false;
            var iter1=vehicleJourney.getVehicleJourneyStops().iterator();
            var iter2=that.vehicleJourney.getVehicleJourneyStops().iterator();
            while (iter1.hasNext()) {
                var p1=iter1.next();
                var p2=iter2.next();
                if (p1.getStop()!=p2.getStop()) return false;
                if (!vehicleJourneyStopAuthorizations.get(p1).equals(vehicleJourneyStopAuthorizations.get(p2))) return false;
            }
            return true;
        };

        Integer hash=null;

        @Override
        public int hashCode() {
            if (hash==null) {
                int computed = route.hashCode();
                for (var vehicleJourneyStop : vehicleJourney.getVehicleJourneyStops()) {
                    computed = computed * 31 + Objects.hash(
                            vehicleJourneyStop.getStop(),
                            vehicleJourneyStopAuthorizations.get(vehicleJourneyStop)
                    );
                }
                hash = computed;
            }
            return hash.intValue();
        }
    }

    private void makeJourneyPatterns() {
        log("Making Patterns");

        HashMap<JourneyPatternDiscriminator, List<VehicleJourney>> journeys = new HashMap<>();


        Comparator<VehicleJourneyStop> vehicleJourneyStopComparator=new Comparator<VehicleJourneyStop>() {
            @Override
            public int compare(VehicleJourneyStop o1, VehicleJourneyStop o2) {
                return o1.getPosition()-o2.getPosition();
            }
        };
        getIdentifiedDSEntityMap(VehicleJourney.class).forEach((id,vehicleJourney)-> {
            //Order stops to allow hash and egual. For use by JourneyPatternDiscriminator
            vehicleJourney.getVehicleJourneyStops().sort(vehicleJourneyStopComparator);
            journeys.computeIfAbsent(new JourneyPatternDiscriminator(vehicleJourney,vehicleJourneyRoutes.get(vehicleJourney)),k->new ArrayList<>()).add(vehicleJourney);
        });

        IdentityHashMap<Route,Integer> nbJourneyPatternsFound=new IdentityHashMap<>();

        journeys.forEach((discriminator,vehicleJourneys)-> {

            var route=discriminator.route;


            //create the journey pattern
            int nb=nbJourneyPatternsFound.computeIfAbsent(route,r->0);
            nbJourneyPatternsFound.put(route,nb+1);
            String journeyPatternId=route.getExternalId()+':'+nb;
            var journeyPattern=getIdentifiedDSEntity(JourneyPattern.class,journeyPatternId);
            journeyPattern.setRoute(route);


            //create the JourneyPatternStops
            AtomicInteger i= new AtomicInteger(0);
            journeyPattern.setJourneyPatternStops(
                    discriminator.vehicleJourney.getVehicleJourneyStops().stream()
                            .map(vehicleJourneyStop -> {
                                String journeyPatternStopId=journeyPatternId+':'+(i.get());
                                var journeyPatternStop=getIdentifiedDSEntity(JourneyPatternStop.class,journeyPatternStopId);
                                var authorizations=vehicleJourneyStopAuthorizations.get(vehicleJourneyStop);
                                journeyPatternStop.setStop(vehicleJourneyStop.getStop());
                                journeyPatternStop.setAlightAllowed(authorizations.alightAllowed);
                                journeyPatternStop.setBoardAllowed(authorizations.boardAllowed);
                                journeyPatternStop.setPosition((short) i.getAndIncrement());
                                return journeyPatternStop;
                            }).toList());


            //set the JourneyPatternStops for all the vehicleJourneys, recompute their positions in order to start at 0 and eliminate holes
            vehicleJourneys.forEach(vehicleJourney -> {
                vehicleJourney.setJourneyPattern(journeyPattern);
                var iterJourneyPatternStops=journeyPattern.getJourneyPatternStops().iterator();
                var iterVehicleJourneyStops=vehicleJourney.getVehicleJourneyStops().iterator();
                while (iterJourneyPatternStops.hasNext()) {
                    var journeyPatternStop=iterJourneyPatternStops.next();
                    var vehicleJourneyStop=iterVehicleJourneyStops.next();
                    vehicleJourneyStop.setJourneyPatternStop(journeyPatternStop);
                    vehicleJourneyStop.setPosition(journeyPatternStop.getPosition());
                }
            });


        });
        log("Done");
    }

}
