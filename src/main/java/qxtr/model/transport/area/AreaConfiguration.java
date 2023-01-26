package qxtr.model.transport.area;

import lombok.Getter;
import lombok.NoArgsConstructor;
import qxtr.model.transport.dataset.DataSetImport;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor
public class AreaConfiguration {

    public AreaConfiguration(Area area) {
        this.area = area;
    }

    @Id
    @GeneratedValue
    @Basic(optional = false)
    @Column(nullable = false)
    private long id;

    @OneToMany(mappedBy = "areaConfiguration",cascade = CascadeType.ALL)
    private Set<Transfer> transfers=new HashSet<>();

    @ManyToMany
    private Set<DataSetImport> dataSetImports=new HashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Area area;

}
