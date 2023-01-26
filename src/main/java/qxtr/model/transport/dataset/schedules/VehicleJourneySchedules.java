package qxtr.model.transport.dataset.schedules;

import lombok.Getter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class VehicleJourneySchedules implements Comparable<VehicleJourneySchedules> {

    private VehicleJourneySchedules() {}

    public VehicleJourneySchedules(int[] arrivals,int[] departures) {
        this.arrivals=arrivals;
        this.departures=departures;
    }

    public VehicleJourneySchedules(int[] schedules) {
        this.arrivals=schedules;
        this.departures=schedules;
    }

    public boolean waitAtStops() {
        return arrivals!=departures;
    }

    @Getter
    private int[] arrivals;

    @Getter
    private int[] departures;

    public int getAimedDeparture(int index) {
        return departures[index];
    }

    public int getAimedArrival(int index) {
        return arrivals[index];
    }

    @Override
    public int compareTo(VehicleJourneySchedules o) {
        return Arrays.compare(departures,o.departures);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleJourneySchedules that = (VehicleJourneySchedules) o;
        if (waitAtStops()!= that.waitAtStops()) return false;
        if (!waitAtStops()) {
            return Arrays.equals(arrivals, that.arrivals);
        } else {
            return Arrays.equals(arrivals, that.arrivals) && Arrays.equals(departures, that.departures);
        }
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(waitAtStops());
        result = 31 * result + Arrays.hashCode(arrivals);
        if (waitAtStops()) {
            result = 31 * result + Arrays.hashCode(departures);
        }
        return result;
    }

    static int crosses(int[] a,int[] b) {
        for (int i=0;i<a.length;++i) {
            a[i]-b[i];
        }
    }

    /**
     * Suppose the sizes are equivalent
     * @param o
     * @return
     */
    public boolean crosses(VehicleJourneySchedules o) {
        if (!waitAtStops() && !o.waitAtStops()) {

        }
    }

    @Converter
    public static class converter implements AttributeConverter<VehicleJourneySchedules,byte[]> {

        @Override
        public byte[] convertToDatabaseColumn(VehicleJourneySchedules schedules) {
            boolean waitAtStops=schedules.waitAtStops();
            var buffer= ByteBuffer.allocate(1+((waitAtStops?8:4)*schedules.arrivals.length));
            buffer.put(waitAtStops?(byte)1:(byte)0);
            for (int arrival: schedules.arrivals) {
                buffer.putInt(arrival);
            }
            if (waitAtStops) {
                for (int departure: schedules.departures) {
                    buffer.putInt(departure);
                }
            }
            return buffer.array();
        }

        @Override
        public VehicleJourneySchedules convertToEntityAttribute(byte[] raw) {
            VehicleJourneySchedules result=new VehicleJourneySchedules();
            var buffer=ByteBuffer.wrap(raw);
            boolean waitAtStops=buffer.get()!=0;
            int nb=buffer.remaining()/4;
            if (waitAtStops) {
                nb/=2;
                result.arrivals=new int[nb];
                result.departures=new int[nb];
                for (int i=0;i<nb;i++) {
                    result.arrivals[i]=buffer.getInt();
                }
                for (int i=0;i<nb;i++) {
                    result.departures[i]=buffer.getInt();
                }
            } else {
                result.arrivals=new int[nb];
                result.departures= result.arrivals;
                for (int i=0;i<nb;i++) {
                    result.arrivals[i]=buffer.getInt();
                }
            }
            return result;
        }
    }

}
