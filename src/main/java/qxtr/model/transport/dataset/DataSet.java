package qxtr.model.transport.dataset;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.transport.area.Area;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class DataSet {

    public DataSet(Area area) {
        this.area=area;
        area.getDataSets().add(this);
    }

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Basic(optional = false)
    @Column(nullable = false)
    private int id;

    @Basic(optional = false)
    @Column(length = 50,nullable = false)
    private String name;

    @OneToMany(mappedBy="dataSet",cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<DataSetImport> dataSetImports=new HashSet<>();

    @Setter
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Area area;

}
