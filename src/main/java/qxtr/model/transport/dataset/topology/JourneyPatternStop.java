package qxtr.model.transport.dataset.topology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import qxtr.model.transport.dataset.DataSetImport;
import qxtr.model.transport.dataset.common.IdentifiedDSEntity;

import javax.persistence.*;

/**
 *
 */
@Getter
@ToString
@Entity
@NoArgsConstructor
public class JourneyPatternStop extends IdentifiedDSEntity {

    public JourneyPatternStop(DataSetImport dataSetImport, String externalId) {
        super(dataSetImport, externalId);
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private JourneyPattern journeyPattern;

    @Setter
    @ManyToOne(optional = false,cascade = CascadeType.PERSIST)
    @JoinColumn(nullable = false)
    private Stop stop;

    @Setter
    @Basic(optional = false)
    @Column(nullable = false)
    // Position in journey pattern, starting at 0 and without "holes"
    private short position;

    @Setter
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean boardAllowed=true;

    @Setter
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean alightAllowed=true;

    public void setJourneyPattern(JourneyPattern journeyPattern) {
        if (this.journeyPattern==journeyPattern) return;
        if (this.journeyPattern!=null) {
            this.journeyPattern.getJourneyPatternStops().remove(this);
        }
        journeyPattern.getJourneyPatternStops().add(this);
        this.journeyPattern=journeyPattern;
    }

    void __setJourneyPattern(JourneyPattern journeyPattern) {
        this.journeyPattern=journeyPattern;
    }
}
