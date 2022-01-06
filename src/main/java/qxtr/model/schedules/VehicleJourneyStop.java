package qxtr.model.schedules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.dataset.DataSetImport;
import qxtr.model.common.DSEntity;
import qxtr.model.common.StopTime;
import qxtr.model.topology.Stop;
import qxtr.model.topology.JourneyPatternStop;

import javax.persistence.*;

@Getter
@ToString
@Entity
@NoArgsConstructor
public class VehicleJourneyStop extends DSEntity {

    public VehicleJourneyStop(DataSetImport dataSetImport, short position, int aimedArrival, int aimedDeparture) {
        super(dataSetImport);
        this.position = position;
        this.aimedArrival = aimedArrival;
        this.aimedDeparture = aimedDeparture;
    }

    public VehicleJourneyStop(DataSetImport dataSetImport, VehicleJourney vehicleJourney, short position, int aimedArrival, int aimedDeparture) {
        super(dataSetImport);
        this.position = position;
        this.aimedArrival = aimedArrival;
        this.aimedDeparture = aimedDeparture;
        setVehicleJourney(vehicleJourney);
    }

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Stop stop;

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private JourneyPatternStop journeyPatternStop;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private VehicleJourney vehicleJourney;

    @Setter
    @Basic(optional = false)
    @Column(nullable = false)
    // Position in vehicle journey, starting at 0 and without "holes"
    private short position;

    @Basic(optional = false)
    @Column(nullable = false)
    //@Convert(converter = StopTime.converter.class)
    private int aimedArrival;

    @Basic(optional = false)
    @Column(nullable = false)
    //@Convert(converter = StopTime.converter.class)
    private int aimedDeparture;

    public void setVehicleJourney(VehicleJourney vehicleJourney) {
        if (this.vehicleJourney==vehicleJourney) return;
        if (this.vehicleJourney!=null) {
            this.vehicleJourney.getVehicleJourneyStops().remove(this);
        }
        vehicleJourney.getVehicleJourneyStops().add(this);
        this.vehicleJourney=vehicleJourney;
    }

}
