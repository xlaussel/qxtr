package qxtr.model.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.dataset.DataSetImport;

import javax.persistence.*;

@Getter
@Setter
@ToString(callSuper = true)
@MappedSuperclass
@NoArgsConstructor
@Table(indexes = @Index(unique=true,columnList="dataSetImport , externalId"))
public class IdentifiedDSEntity extends DSEntity {

    @Getter
    @Setter
    @Basic(optional = false)
    @Column(nullable = false)
    @ToString.Include
    protected String externalId=null;

    /*@PrePersist
    private void setExternalId() {
        if (externalId==null) {
            externalId= id+"-"+ dataSetImport.getId();
        }
    }*/

    protected IdentifiedDSEntity(DataSetImport dataSetImport,String externalId) {
        super(dataSetImport);
        this.externalId=externalId;
    }
}
