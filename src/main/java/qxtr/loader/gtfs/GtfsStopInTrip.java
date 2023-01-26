package qxtr.loader.gtfs;

import lombok.Getter;

import java.time.LocalTime;
import java.util.Objects;

@Getter
public class GtfsStopInTrip {
    private String stopId;
    private int sequence;
    private LocalTime arrivalTime;
    private LocalTime departureTime;
    private boolean alightAllowed;
    private boolean boardAllowed;

    public GtfsStopInTrip(String stopId, int sequence, LocalTime arrivalTime, LocalTime departureTime, boolean alightAllowed, boolean boardAllowed) {
        this.stopId = stopId;
        this.sequence = sequence;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.alightAllowed = alightAllowed;
        this.boardAllowed = boardAllowed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GtfsStopInTrip that = (GtfsStopInTrip) o;
        return sequence == that.sequence && arrivalTime == that.arrivalTime && departureTime == that.departureTime && alightAllowed == that.alightAllowed && boardAllowed == that.boardAllowed && stopId.equals(that.stopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopId, sequence, arrivalTime, departureTime, alightAllowed, boardAllowed);
    }

}
