package qxtr.model.schedules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;
import qxtr.model.topology.JourneyPattern;
import qxtr.model.topology.StopGroup;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class VehicleJourney extends IdentifiedDSEntity {

    public VehicleJourney(DataSetImport dataSetImport,String externalId) {
        super(dataSetImport,externalId);
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private JourneyPattern journeyPattern;

    @OneToMany(mappedBy = "vehicleJourney")
    @OrderBy("position")
    private List<VehicleJourneyAtStop> vehicleJourneyAtStops=new ArrayList<>();

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
}
