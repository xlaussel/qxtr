package qxtr.repositories.transport.dataset.schedules;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import qxtr.model.transport.dataset.schedules.VehicleJourney;

import java.util.List;


@Repository
public interface VehicleJourneyRepository extends CrudRepository<VehicleJourney,Long> {

    @Query("SELECT vj FROM VehicleJourney vj")
    List<VehicleJourney> listAll();

}
