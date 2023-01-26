package qxtr.model.transport.dataset.topology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import qxtr.model.transport.dataset.DataSetImport;
import qxtr.model.transport.dataset.common.IdentifiedDSEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Route
 */
@Getter
@ToString
@Entity
@NoArgsConstructor
public class Route extends IdentifiedDSEntity {

    public Route(DataSetImport dataSetImport, String externalId) {
        super(dataSetImport, externalId);
    }

    @ManyToOne(optional = false,cascade = CascadeType.PERSIST)
    @JoinColumn(nullable = false)
    private Line line;

    @OneToMany(mappedBy = "route",cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<JourneyPattern> journeyPatterns=new HashSet<>();

    /*@OneToMany(mappedBy = "route")
    @OrderColumn(name = "position", nullable = false)
    private List<JourneyPatternStop> journeyPatternStops=new ArrayList<>();*/

    public void setLine(Line line) {
        if (this.line==line) return;
        if (this.line!=null) {
            this.line.getRoutes().remove(this);
        }
        line.getRoutes().add(this);
        this.line=line;
    }
}
