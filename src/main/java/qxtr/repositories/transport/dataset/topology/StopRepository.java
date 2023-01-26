package qxtr.repositories.transport.dataset.topology;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import qxtr.model.transport.dataset.topology.Stop;

@Repository
public abstract class StopRepository implements CrudRepository<Stop,Long> {
}
