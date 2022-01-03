package qxtr.model.schedules;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import qxtr.model.DataSetImport;
import qxtr.model.common.DSEntity;
import qxtr.model.common.IdentifiedDSEntity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
public class TimeTable extends IdentifiedDSEntity  {

    public TimeTable(DataSetImport dataSetImport, String externalId) {
        super(dataSetImport,externalId);
    }

    @ManyToMany(mappedBy = "timeTables")
    private Set<VehicleJourney> vehicleJourneys=new HashSet<>();

    @Basic(optional = false)
    @Column(nullable = false)
    @Setter
    private byte validDays=0;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    private List<Period> periods = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @OrderColumn(name = "day", nullable = false)
    private List<TimeTableDay> days = new ArrayList<>();

    @Basic(optional = false)
    @Column(nullable = false)
    private LocalDate first;

    @Basic(optional = false)
    @Column(nullable = false)
    private LocalDate last;

    public TreeSet<LocalDate> getDates() {
        TreeSet<LocalDate> result=new TreeSet<>();
        for (var period:periods) {
            result.addAll(period.toDates(validDays));
        }
        for (var day:days) {
            if (day.isIn_out()) {
                result.add(day.getDay());
            } else {
                result.remove(day.getDay());
            }
        }
        return result;
    }

    @PrePersist
    @PreUpdate
    private void updateFirstLast() {
        var dates=getDates();
        first = dates.first();
        last = dates.last();
    }

}
