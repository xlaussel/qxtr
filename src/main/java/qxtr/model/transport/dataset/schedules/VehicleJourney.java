package qxtr.model.transport.dataset.schedules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.transport.dataset.DataSetImport;
import qxtr.model.transport.dataset.common.IdentifiedDSEntity;
import qxtr.model.transport.dataset.topology.JourneyPattern;

import javax.persistence.*;
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

    @Setter
    @Basic(optional = false)
    @Column(nullable = false)
    @Convert(converter = VehicleJourneySchedules.converter.class)
    private VehicleJourneySchedules schedules;

    @ManyToMany(mappedBy = "vehicleJourneys")
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
        return schedules.compareTo(o.schedules);
    }
}
