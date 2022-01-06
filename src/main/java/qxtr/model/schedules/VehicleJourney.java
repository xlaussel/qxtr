package qxtr.model.schedules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import qxtr.model.dataset.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;
import qxtr.model.topology.JourneyPattern;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
@ToString
@Entity
@NoArgsConstructor
public class VehicleJourney extends IdentifiedDSEntity implements Comparable<VehicleJourney> {

    public VehicleJourney(DataSetImport dataSetImport,String externalId) {
        super(dataSetImport,externalId);
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private JourneyPattern journeyPattern;

    @OneToMany(mappedBy = "vehicleJourney",cascade = CascadeType.ALL)
    @OrderBy("position")
    private List<VehicleJourneyStop> vehicleJourneyStops=new ArrayList<>();

    @ManyToMany(mappedBy = "vehicleJourneys",cascade = CascadeType.ALL)
    private List<TimeTable> timeTables=new LinkedList<>();

    public void setJourneyPattern(JourneyPattern journeyPattern) {
        if (this.journeyPattern==journeyPattern) return;
        if (this.journeyPattern!=null) {
            this.journeyPattern.getVehicleJourneys().remove(this);
        }
        journeyPattern.getVehicleJourneys().add(this);
        this.journeyPattern=journeyPattern;
    }

    @Override
    public int compareTo(VehicleJourney o) {
        return vehicleJourneyStops.get(0).getAimedDeparture()-o.vehicleJourneyStops.get(0).getAimedDeparture();
    }
}
