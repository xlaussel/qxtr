package qxtr.model.topology;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.common.IdentifiedDSEntity;

import javax.persistence.*;


/**
 *
 */
@Getter
@Setter
@ToString
@Entity
public class StopPoint extends IdentifiedDSEntity {

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Stop stop;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Route route;

    @Basic(optional = false)
    @Column(nullable = false)
    // Position in route, starting at 0 and without "holes" in a route
    private short position;

    @Basic(optional = false)
    @Column(nullable = false)
    private boolean boardAllowed=true;

    @Basic(optional = false)
    @Column(nullable = false)
    private boolean alightAllowed=true;
}
