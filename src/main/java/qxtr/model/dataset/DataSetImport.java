package qxtr.model.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.topology.Agency;
import qxtr.model.topology.Line;
import qxtr.model.topology.Stop;
import qxtr.model.topology.StopGroup;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
public class DataSetImport {

    public enum State {NEW,LOADING,LOADED,CLOSED}

    public DataSetImport() {
    }

    public DataSetImport(DataSet dataSet) {
        this.dataSet = dataSet;
        dataSet.getDataSetImports().add(this);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(nullable = false)
    private int id;

    @Basic(optional = false)
    @Column(nullable = false)
    private int version;

    @Basic(optional = false)
    @Column(nullable = false)
    private State state=State.NEW;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false,targetEntity = DataSet.class)
    private DataSet dataSet;

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<StopGroup> stopGroups=new HashSet<>();

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Stop> stops=new HashSet<>();

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Line> lines=new HashSet<>();

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Agency> agencies=new HashSet<>();

}
