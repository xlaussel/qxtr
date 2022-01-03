package qxtr.model.schedules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.DataSetImport;
import qxtr.model.common.DSEntity;
import qxtr.model.topology.Stop;
import qxtr.model.topology.StopPoint;

import javax.persistence.*;
import java.time.LocalTime;

@Getter
@ToString
@Entity
@NoArgsConstructor
public class VehicleJourneyAtStop extends DSEntity implements Comparable<VehicleJourneyAtStop> {

    public VehicleJourneyAtStop(DataSetImport dataSetImport, short position, LocalTime aimedArrival, LocalTime aimedDeparture, boolean alightAllowed, boolean boardAllowed) {
        super(dataSetImport);
        this.position = position;
        this.aimedArrival = aimedArrival;
        this.aimedDeparture = aimedDeparture;
        this.alightAllowed = alightAllowed;
        this.boardAllowed = boardAllowed;
    }

    public VehicleJourneyAtStop(DataSetImport dataSetImport, VehicleJourney vehicleJourney,short position, LocalTime aimedArrival, LocalTime aimedDeparture, boolean alightAllowed, boolean boardAllowed) {
        super(dataSetImport);
        this.position = position;
        this.aimedArrival = aimedArrival;
        this.aimedDeparture = aimedDeparture;
        this.alightAllowed = alightAllowed;
        this.boardAllowed = boardAllowed;
        setVehicleJourney(vehicleJourney);
    }

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Stop stop;

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private StopPoint stopPoint;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private VehicleJourney vehicleJourney;

    @Basic(optional = false)
    @Column(nullable = false)
    // Position in pattern, starting at 0 and without "holes"
    private short position;

    @Basic(optional = false)
    @Column(nullable = false)
    private LocalTime aimedArrival;

    @Basic(optional = false)
    @Column(nullable = false)
    private LocalTime aimedDeparture;

    @Basic(optional = false)
    @Column(nullable = false)
    private boolean alightAllowed=true;

    @Basic(optional = false)
    @Column(nullable = false)
    private boolean boardAllowed=true;

    public void setVehicleJourney(VehicleJourney vehicleJourney) {
        if (this.vehicleJourney==vehicleJourney) return;
        if (this.vehicleJourney!=null) {
            this.vehicleJourney.getVehicleJourneyAtStops().remove(this);
        }
        vehicleJourney.getVehicleJourneyAtStops().add(this);
        this.vehicleJourney=vehicleJourney;
    }

    @Override
    public int compareTo(VehicleJourneyAtStop o) {
        return position-o.position;
    }
}
