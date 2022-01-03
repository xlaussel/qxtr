package qxtr.model.topology;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.common.IdentifiedDSEntity;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Entity
public class Line extends IdentifiedDSEntity {

    @OneToMany(mappedBy = "line",cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Route> routes;

    @ManyToMany
    @ToString.Exclude
    private Set<Stop> stops;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @ToString.Exclude
    private Agency agency;
}
