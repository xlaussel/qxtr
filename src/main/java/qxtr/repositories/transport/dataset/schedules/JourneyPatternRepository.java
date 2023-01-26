package qxtr.repositories.transport.dataset.schedules;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import qxtr.model.transport.dataset.topology.JourneyPattern;

import java.util.List;


@Repository
public interface JourneyPatternRepository extends CrudRepository<JourneyPattern,Long> {

    @Query("SELECT jp FROM JourneyPattern jp LEFT JOIN FETCH jp.journeyPatternStops")
    List<JourneyPattern> listAllWithStops();

}
