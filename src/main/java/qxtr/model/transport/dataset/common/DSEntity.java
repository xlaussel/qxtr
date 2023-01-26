package qxtr.model.transport.dataset.common;

import lombok.*;
import org.hibernate.annotations.Filter;
import qxtr.model.transport.dataset.DataSetImport;

import javax.persistence.*;

@Getter
@Setter
@ToString
@MappedSuperclass
@NoArgsConstructor
@Filter(name="dataSetImport")
@Filter(name="dataSetImports")
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
