package qxtr.loader.gtfs;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import qxtr.loader.LoaderInput;
import qxtr.loader.LoaderInterface;
import qxtr.model.DataSet;
import qxtr.model.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;
import qxtr.model.schedules.*;
import qxtr.model.topology.*;
import qxtr.utils.CsvReader;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GtfsLoader implements LoaderInterface {

    @Override
    public DataSetImport load(DataSet dataSet, LoaderInput input) {
        return null;
    }

    private DataSetImport dataSetImport;

    private final IdentityHashMap<Class<? extends IdentifiedDSEntity>,HashMap<String,? extends IdentifiedDSEntity>> identifiedDSEntities=new IdentityHashMap<>();

    private final IdentityHashMap<VehicleJourney, Route> vehicleJourneyRoutes = new IdentityHashMap<>();

    private <T extends IdentifiedDSEntity> T getIdentifiedDSEntity(Class<T> clazz,String identifier) {
        return  getIdentifiedDSEntityMap(clazz)
                .computeIfAbsent(identifier,id-> { try {
                    return clazz.getConstructor(DataSetImport.class, String.class).newInstance(dataSetImport, id);
                  } catch (Exception e) { return null;}
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

    private void loadAgencies(InputStream agencyStream) throws IOException {
        var reader=new CsvReader(agencyStream);
        int AGENCY_ID=reader.columnIndex("agency_id");
        int AGENCY_NAME=reader.columnIndex("agency_name");
        CsvReader.reader(agencyStream).forEach(line -> getIdentifiedDSEntity(Agency.class,line.get(AGENCY_ID)).setName(line.get(AGENCY_NAME)));
    }

    private void loadStops(InputStream stopsStream) throws IOException {
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
                Stop stop = getIdentifiedDSEntity(Stop.class,stopId);
                stop.setName(line.get("stop_name"));
                String stopGroupId = line.get("parent_station", null);
                if (stopGroupId != null) {
                    stop.setStopGroup(getIdentifiedDSEntity(StopGroup.class,stopGroupId));
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
    }

    private void loadTimes(InputStream stopTimesStream) throws IOException {
        CsvReader.reader(stopTimesStream).forEach(line -> {
            var vehicleJourney = getIdentifiedDSEntity(VehicleJourney.class,line.get("trip_id"));
            LocalTime arrivaltime = LocalTime.parse(line.get("arrival_time"));
            LocalTime departuretime = LocalTime.parse(line.get("departure_time"));
            short sequence = Short.parseShort(line.get("stop_sequence"));
            boolean boardAllowed = !line.get("pickup_type", "0").equals("1");
            boolean alightAllowed = !line.get("drop_off_type", "0").equals("1");
            VehicleJourneyAtStop vehicleJourneyAtStop = new VehicleJourneyAtStop(dataSetImport, vehicleJourney, sequence, arrivaltime, departuretime, alightAllowed, boardAllowed);
            vehicleJourneyAtStop.setStop(getIdentifiedDSEntity(Stop.class,line.get("stop_id")));
        });
    }

    private void loadCalendar(InputStream calendarStream) throws IOException {
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
    }

    private void loadCalendarDates(InputStream calendarDatesStream) throws IOException {
        CsvReader.reader(calendarDatesStream).forEach(line -> {
            var timeTable = getIdentifiedDSEntity(TimeTable.class,line.get("service_id"));
            var date = LocalDate.parse(line.get("date"), DateTimeFormatter.BASIC_ISO_DATE);
            boolean in_out = line.get("exception_type", "1").equals("1");
            timeTable.getDays().add(new TimeTableDay(date, in_out));
        });
    }

    private void loadTrips(InputStream tripStream) throws IOException {
        CsvReader.reader(tripStream).forEach(line -> {
            var vehicleJourney = getIdentifiedDSEntity(VehicleJourney.class,line.get("trip_id"));
            vehicleJourney.getTimeTables().add(getIdentifiedDSEntity(TimeTable.class,line.get("service_id")));
            vehicleJourneyRoutes.put(vehicleJourney, getIdentifiedDSEntity(Route.class,line.get("route_id")));
        });
    }

    private void loadRoutes(InputStream routesStream) throws IOException {
        CsvReader.reader(routesStream).forEach(line -> {
            var route = getIdentifiedDSEntity(Route.class,line.get("route_id"));
            route.setShortName(line.get("route_short_name", null));
            route.setName(line.get("route_long_name", null));
            var sortString = line.get("route_sort_order", null);
            if (sortString != null) {
                route.setOrder(Short.parseShort(sortString));
            }
        });
    }

    private static class JourneyPatternDiscriminator {

        public VehicleJourney vehicleJourney;
        public Route route;

        public JourneyPatternDiscriminator(VehicleJourney vehicleJourney,Route route) {
            this.vehicleJourney = vehicleJourney;
            this.route=route;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JourneyPatternDiscriminator that = (JourneyPatternDiscriminator) o;
            if (that.route!=route) return false;
            if (vehicleJourney.getVehicleJourneyAtStops().size()!=that.vehicleJourney.getVehicleJourneyAtStops().size()) return false;
            var iter1=vehicleJourney.getVehicleJourneyAtStops().iterator();
            var iter2=that.vehicleJourney.getVehicleJourneyAtStops().iterator();
            while (iter1.hasNext()) {
                var p1=iter1.next();
                var p2=iter2.next();
                if (p1.getStop()!=p2.getStop()) return false;
                if (p1.isAlightAllowed()!=p2.isAlightAllowed()) return false;
                if (p1.isBoardAllowed()!=p2.isBoardAllowed()) return false;
            }
            return true;
        };

        @Override
        public int hashCode() {
            int result = Objects.hashCode(route);
            for (var vehicleJourneyAtStop: vehicleJourney.getVehicleJourneyAtStops()) {
                result = result * 31 + Objects.hash(
                        vehicleJourneyAtStop.getStop(),
                        vehicleJourneyAtStop.isAlightAllowed(),
                        vehicleJourneyAtStop.isBoardAllowed());
            }
            return result;
        }
    }

    private void makeJourneyPatterns() {
        HashMap<JourneyPatternDiscriminator, List<VehicleJourney>> journeys = new HashMap<>();
        getIdentifiedDSEntityMap(VehicleJourney.class).forEach((id,vehicleJourney)-> {
            //Order stops to allow hash and egual. For use by JourneyPatternDiscriminator
            vehicleJourney.getVehicleJourneyAtStops().sort(null);
            journeys.computeIfAbsent(new JourneyPatternDiscriminator(vehicleJourney,vehicleJourneyRoutes.get(vehicleJourney)),k->new ArrayList<>()).add(vehicleJourney);
        });
        IdentityHashMap<Route,Integer> nbRoutesFound=new IdentityHashMap<>();
        journeys.forEach((discriminator,vehicleJourneys)-> {
            var route=discriminator.route;
            int nb=nbRoutesFound.computeIfAbsent(route,r->1);
            nbRoutesFound.put(route,nb+1);
            JourneyPattern journeyPattern=new JourneyPattern(dataSetImport,route.getExternalId()+'-'+nb,route);
            // create the stop points
            for (int i=0;i<discriminator.vehicleJourney.getVehicleJourneyAtStops().size();++i) {

            }
            journeyPattern.setStopPoints(
                    discriminator.vehicleJourney.getVehicleJourneyAtStops().stream()
                            .map(VehicleJourneyAtStop::getStopPoint).toList());
        });
    }
}
