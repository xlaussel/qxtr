package qxtr.model.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

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
    @ToString.Exclude
    @OrderColumn
    private List<DataSetImport> dataSetImports=new LinkedList<>();

}
