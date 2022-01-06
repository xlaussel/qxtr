package qxtr.model.schedules;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Getter
@Embeddable
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class Period {

    public Period(LocalDate start,LocalDate end) {
        this.start=start;
        this.end=end;
    }

    @Basic(optional = false)
    @Column(nullable = false)
    private LocalDate start;

    @Basic(optional = false)
    @Column(nullable = false,name="endd")
    private LocalDate end;

    public List<LocalDate> toDates() {
        List<LocalDate> dates = new ArrayList<>();
        for (var current=start;!current.isAfter(end);current=current.plusDays(1)) {
            dates.add(current);
        }
        return dates;
    }

    public List<LocalDate> toDates(byte validDays) {
        List<LocalDate> dates = new ArrayList<>();
        for (var current=start;!current.isAfter(end);current=current.plusDays(1)) {
            if ((validDays & ((byte)1) << (current.getDayOfWeek().getValue()-1))!=0) {
                dates.add(current);
            }
        }
        return dates;
    }

}
