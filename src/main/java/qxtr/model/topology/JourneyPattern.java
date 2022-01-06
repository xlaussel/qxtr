package qxtr.model.topology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import qxtr.model.dataset.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;
import qxtr.model.schedules.VehicleJourney;

import javax.persistence.*;
import java.util.*;

@Getter
@ToString
@Entity
@NoArgsConstructor
public class JourneyPattern extends IdentifiedDSEntity {

    public JourneyPattern(DataSetImport dataSetImport, String externalId) {
        super(dataSetImport, externalId);
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Route route;

    @OneToMany(mappedBy = "journeyPattern",cascade = CascadeType.ALL)
    @OrderBy("position")
    private List<JourneyPatternStop> journeyPatternStops=new LinkedList<>();

    public void setJourneyPatternStops(List<JourneyPatternStop> journeyPatternStops) {
        this.journeyPatternStops=journeyPatternStops;
        journeyPatternStops.forEach(journeyPatternStop -> journeyPatternStop.__setJourneyPattern(this));
    }

    @OneToMany(mappedBy = "journeyPattern",cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<VehicleJourney> vehicleJourneys=new HashSet<>();

    public void setRoute(Route route) {
        if (this.route==route) return;
        if (this.route!=null) {
            this.route.getJourneyPatterns().remove(this);
        }
        route.getJourneyPatterns().add(this);
        this.route=route;
    }
}
