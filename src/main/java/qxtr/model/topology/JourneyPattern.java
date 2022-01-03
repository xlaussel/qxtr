package qxtr.model.topology;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;
import qxtr.model.schedules.VehicleJourney;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
public class JourneyPattern extends IdentifiedDSEntity {

    public JourneyPattern(DataSetImport dataSetImport, String externalId, Route route) {
        super(dataSetImport, externalId);
        setRoute(route);
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Route route;

    @ManyToMany
    @OrderColumn(name = "position", nullable = false)
    private List<StopPoint> stopPoints;

    @OneToMany(mappedBy = "journeyPattern",cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<VehicleJourney> vehicleJourneys;

    public void setRoute(Route route) {
        if (this.route==route) return;
        if (this.route!=null) {
            this.route.getJourneyPatterns().remove(this);
        }
        route.getJourneyPatterns().add(this);
        this.route=route;
    }
}
