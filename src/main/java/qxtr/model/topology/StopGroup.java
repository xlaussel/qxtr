package qxtr.model.topology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.locationtech.jts.geom.Point;
import qxtr.model.dataset.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@ToString
@Entity
@NoArgsConstructor
public class StopGroup extends IdentifiedDSEntity {

    public StopGroup(DataSetImport dataSetImport,String externalId) {
        super(dataSetImport,externalId);
        dataSetImport.getStopGroups().add(this);
    }

    @Setter
    @Basic(optional = false)
    @Column(nullable = false)
    private String name;

    public void setName(String name) {
        this.name = name.length()<=255?name:name.substring(0,255);
    }

    @OneToMany(mappedBy = "stopGroup",cascade =  CascadeType.ALL)
    @ToString.Exclude
    private Set<Stop> stops=new HashSet<>();

    @Setter
    @Column(columnDefinition = "geometry(Point,4326)")
    @Basic
    private Point location;

}
