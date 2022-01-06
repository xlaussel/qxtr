package qxtr.model.topology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;
import qxtr.model.dataset.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;

import javax.persistence.*;

/**
 * Physical stop in the network identified by name and included into a StopGroup
 */
@Getter
@ToString
@Entity
@NoArgsConstructor
public class Stop extends IdentifiedDSEntity {

    public Stop(DataSetImport dataSetImport,String externalId) {
        super(dataSetImport,externalId);
        dataSetImport.getStops().add(this);
    }

    @Basic(optional = false)
    @Column(nullable = false)
    private String name;

    public void setName(String name) {
        this.name = name.length()<=255?name:name.substring(0,255);
    }

    @ManyToOne(optional = false,cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private StopGroup stopGroup;

    @Setter
    @Column(columnDefinition = "geometry(Point,4326)")
    @Basic
    private Point location;

    public void setStopGroup(StopGroup stopGroup) {
        if (this.stopGroup==stopGroup) return;
        if (this.stopGroup!=null) {
            this.stopGroup.getStops().remove(this);
        }
        stopGroup.getStops().add(this);
        this.stopGroup=stopGroup;
    }
}
