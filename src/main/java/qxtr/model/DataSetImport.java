package qxtr.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.topology.Agency;
import qxtr.model.topology.Line;
import qxtr.model.topology.Stop;
import qxtr.model.topology.StopGroup;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
public class DataSetImport {

    public DataSetImport() {
    }

    protected DataSetImport(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(nullable = false)
    private int id;

    @Basic(optional = false)
    @Column(nullable = false)
    private int version;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false,targetEntity = DataSet.class)
    private DataSet dataSet;

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<StopGroup> stopGroups;

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Stop> stops;

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Line> lines;

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Agency> agencies;


}
