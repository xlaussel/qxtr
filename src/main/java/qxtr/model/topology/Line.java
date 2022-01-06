package qxtr.model.topology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import qxtr.model.dataset.DataSetImport;
import qxtr.model.common.IdentifiedDSEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@ToString(onlyExplicitlyIncluded = true)
@Entity
@NoArgsConstructor
public class Line extends IdentifiedDSEntity {

    public Line(DataSetImport dataSetImport, String externalId) {
        super(dataSetImport, externalId);
        dataSetImport.getLines().add(this);
    }

    @Basic(optional = false)
    @Column(nullable = false)
    private String shortName;

    @Basic(optional = false)
    @Column(nullable = false)
    private String name;

    public void setShortName(String shortName) {
        this.shortName = shortName.length()<=255?shortName:shortName.substring(0,255);
    }

    public void setName(String name) {
        this.name = name.length()<=255?name:name.substring(0,255);
    }

    @OneToMany(mappedBy = "line",cascade = CascadeType.ALL)
    private Set<Route> routes=new HashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Agency agency;

    public void setAgency(Agency agency) {
        if (this.agency==agency) return;
        if (this.agency!=null) {
            this.agency.getLines().remove(this);
        }
        agency.getLines().add(this);
        this.agency=agency;
    }
}
