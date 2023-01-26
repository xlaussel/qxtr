package qxtr.model.transport.dataset.topology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.transport.dataset.DataSetImport;
import qxtr.model.transport.dataset.common.IdentifiedDSEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@ToString
@Entity
@NoArgsConstructor
public class Agency extends IdentifiedDSEntity {

    public Agency(DataSetImport dataSetImport, String externalId) {
        super(dataSetImport,externalId);
        dataSetImport.getAgencies().add(this);
    }

    @Setter
    @Basic(optional = false)
    @Column(length = 50,nullable = false)
    private String name;

    @OneToMany(mappedBy = "agency",cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Line> lines=new HashSet<>();

}
