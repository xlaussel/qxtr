package qxtr.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@ToString
@Entity
public class DataSet {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Basic(optional = false)
    @Column(nullable = false)
    private int id;

    @Basic(optional = false)
    @Column(length = 50,nullable = false)
    private String name;

    @OneToMany(mappedBy="dataSet",cascade = CascadeType.ALL)
    @MapKey(name="version")
    @ToString.Exclude
    private Map<Integer,DataSetImport> dataSetImports;

    public Map<Integer,DataSetImport> getDataSetImports() {
        return Collections.unmodifiableMap(dataSetImports);
    }

    public DataSetImport addNewDataSetImport() {
        DataSetImport dataSetImport=new DataSetImport(this);
        int version=dataSetImports.values().size()+1;
        dataSetImport.setVersion(version);
        dataSetImports.put(version,dataSetImport);
        return dataSetImport;
    }
}
