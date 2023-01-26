package qxtr.repositories.transport.dataset.topology;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import qxtr.model.transport.area.Transfer;

import java.util.List;

@Repository
public interface TransferRepository extends CrudRepository<Transfer,Long> {

    @Query("SELECT t FROM Transfer t")
    List<Transfer> listAll();

}