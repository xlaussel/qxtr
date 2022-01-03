package qxtr.model.schedules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
public class TimeTableDay {

    public TimeTableDay(LocalDate day,boolean in_out) {
        this.day=day;
        this.in_out=in_out;
    }

    @Basic(optional = false)
    @Column(nullable = false)
    private LocalDate day;

    /**
     * true if included, false if excluded
     */
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean in_out;
}
