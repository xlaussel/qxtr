package qxtr.model.transport.area;

import lombok.Getter;
import lombok.NoArgsConstructor;
import qxtr.model.transport.area.common.AreaEntity;
import qxtr.model.transport.dataset.topology.Stop;
import javax.persistence.*;

@Getter
@Entity
@Table(indexes = @Index(unique=true,columnList="area_configuration_id , start_stop_id, end_stop_id"))
@NoArgsConstructor
public class Transfer extends AreaEntity {

    public Transfer(AreaConfiguration areaConfiguration, Stop start, Stop end, short distance) {
        super(areaConfiguration);
        areaConfiguration.getTransfers().add(this);
        this.start = start;
        this.end = end;
        this.distance = distance;
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false,name="start_stop_id")
    private Stop start;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false,name="end_stop_id")
    private Stop end;

    /**
     * Distance in meters
     */
    @Basic(optional = false)
    @Column(nullable = false)
    private short distance;

}
