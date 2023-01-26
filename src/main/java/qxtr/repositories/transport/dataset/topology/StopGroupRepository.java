package qxtr.repositories.transport.dataset.topology;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import qxtr.model.transport.dataset.topology.StopGroup;

import java.util.List;

@Repository
public interface StopGroupRepository extends CrudRepository<StopGroup, Long> {

    @Query("SELECT sg FROM StopGroup sg LEFT JOIN FETCH sg.stops")
    public List<StopGroup> findAllWithStops();
}
