package qxtr.model.transport.dataset;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import qxtr.model.transport.area.AreaConfiguration;
import qxtr.model.transport.dataset.schedules.TimeTable;
import qxtr.model.transport.dataset.topology.Agency;
import qxtr.model.transport.dataset.topology.Line;
import qxtr.model.transport.dataset.topology.Stop;
import qxtr.model.transport.dataset.topology.StopGroup;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@org.hibernate.annotations.NamedQuery(
        name="DataSetImport.loadAll",
        query = "FROM DataSetImport dsi LEFT JOIN FETCH dsi.stops WHERE dsi.id= :id"
)

@Getter
@ToString(onlyExplicitlyIncluded = true)
@Entity
public class DataSetImport {

    public enum State {NEW,LOADING,LOADED,CLOSED}

    public DataSetImport() {
    }

    public DataSetImport(@NotNull DataSet dataSet,@NotNull AreaConfiguration areaConfiguration) {
        setDataSet(dataSet);
        setAreaConfiguration(areaConfiguration);
    }

    public DataSetImport(@NotNull DataSet dataSet) {
        setDataSet(dataSet);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(nullable = false)
    @ToString.Include
    private int id;

    @Basic(optional = false)
    @Column(nullable = false)
    @ToString.Include
    @Setter
    private int version;

    @Basic(optional = false)
    @Column(nullable = false)
    @ToString.Include
    @Setter
    private State state=State.NEW;

    @ManyToOne(optional = false,targetEntity = DataSet.class)
    @ToString.Include
    private DataSet dataSet;

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<StopGroup> stopGroups=new HashSet<>();

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Stop> stops=new HashSet<>();

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Line> lines=new HashSet<>();

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Agency> agencies=new HashSet<>();

    @OneToMany(mappedBy = "dataSetImport", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<TimeTable> timeTables=new HashSet<>();

    @Getter
    @ManyToOne(optional = false,cascade = CascadeType.PERSIST)
    @JoinColumn(nullable = false)
    private AreaConfiguration areaConfiguration;

    public void setAreaConfiguration(@NotNull AreaConfiguration areaConfiguration) {
        this.areaConfiguration=areaConfiguration;
        areaConfiguration.getDataSetImports().add(this);
    }

    public void setDataSet(@NotNull DataSet dataSet) {
        this.dataSet=dataSet;
        dataSet.getDataSetImports().add(this);
    }
}
