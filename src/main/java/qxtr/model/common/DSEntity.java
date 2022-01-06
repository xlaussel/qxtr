package qxtr.model.common;

import lombok.*;
import qxtr.model.dataset.DataSetImport;

import javax.persistence.*;

@Getter
@Setter
@ToString()
@MappedSuperclass
@NoArgsConstructor
public class DSEntity {

    @Id
    @GeneratedValue(generator= "ds-entity-sequence")
    @Basic(optional = false)
    @Column(nullable = false)
    protected long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @ToString.Include
    protected DataSetImport dataSetImport;

    public DSEntity(DataSetImport dataSetImport) {
        this.dataSetImport=dataSetImport;
    }

}
