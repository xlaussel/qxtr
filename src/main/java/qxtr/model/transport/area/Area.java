package qxtr.model.transport.area;

import lombok.Getter;
import qxtr.model.transport.dataset.DataSet;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
public class Area {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Basic(optional = false)
    @Column(nullable = false)
    private int id;

    @OneToMany(mappedBy = "area",cascade = CascadeType.ALL)
    private Set<DataSet> dataSets=new HashSet<>();

    @OneToMany(mappedBy = "area",cascade=CascadeType.ALL)
    private Set<AreaConfiguration> areaConfigurations=new HashSet<>();

}
