package qxtr.model.topology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.locationtech.jts.geom.Point;
import qxtr.model.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class StopGroup extends IdentifiedDSEntity {

    public StopGroup(DataSetImport dataSetImport,String externalId) {
        super(dataSetImport,externalId);
        dataSetImport.getStopGroups().add(this);
    }

    @Basic(optional = false)
    @Column(length = 50,nullable = false)
    private String name;

    @OneToMany(mappedBy = "stopGroup",cascade =  CascadeType.ALL)
    @ToString.Exclude
    private Set<Stop> stops;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;


}
