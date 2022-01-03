package qxtr.model.topology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class Agency extends IdentifiedDSEntity {

    public Agency(DataSetImport dataSetImport, String externalId) {
        super(dataSetImport,externalId);
        dataSetImport.getAgencies().add(this);
    }

    @Basic(optional = false)
    @Column(length = 50,nullable = false)
    private String name;

    @OneToMany(mappedBy = "agency",cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Line> lines;

}
