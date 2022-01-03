package qxtr.model.topology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Route
 */
@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class Route extends IdentifiedDSEntity {

    public Route(DataSetImport dataSetImport, String externalId) {
        super(dataSetImport, externalId);
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Line line;

    @Basic(optional = false)
    @Column(length = 50,nullable = false)
    private String shortName;

    @Basic(optional = false)
    @Column(length = 50,nullable = false)
    private String name;

    @Basic
    private Short order;

    @OneToMany(mappedBy = "route",cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<JourneyPattern> journeyPatterns=new HashSet<>();

    @OneToMany(mappedBy = "route")
    @OrderColumn(name = "position", nullable = false)
    private List<StopPoint> stopPoints=new ArrayList<>();

    public void setLine(Line line) {
        if (this.line==line) return;
        if (this.line!=null) {
            this.line.getRoutes().remove(this);
        }
        line.getRoutes().add(this);
        this.line=line;
    }
}
