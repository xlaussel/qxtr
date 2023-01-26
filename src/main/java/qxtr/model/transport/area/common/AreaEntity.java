package qxtr.model.transport.area.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import qxtr.model.transport.area.AreaConfiguration;

import javax.persistence.*;

@Getter
@Filter(name="areaConfiguration")
@MappedSuperclass
@NoArgsConstructor
public class AreaEntity {

    public AreaEntity(AreaConfiguration areaConfiguration) {
        this.areaConfiguration=areaConfiguration;
    }

    @Id
    @GeneratedValue(generator= "ds-entity-sequence")
    @Basic(optional = false)
    @Column(nullable = false)
    protected long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false,name = "area_configuration_id")
    protected AreaConfiguration areaConfiguration;

}
