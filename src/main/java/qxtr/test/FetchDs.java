package qxtr.test;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import qxtr.model.transport.dataset.DataSetImport;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.Map;

@Service
public class FetchDs {

    @PersistenceContext(type= PersistenceContextType.EXTENDED)
    EntityManager entityManager;

    public void fetch(int id) {
        var session=entityManager.unwrap(Session.class);
        //session.
        CriteriaBuilder builder=entityManager.getCriteriaBuilder();
        CriteriaQuery<DataSetImport> cq=builder.createQuery(DataSetImport.class);
        Root<DataSetImport> root=cq.from(DataSetImport.class);
        root.fetch("stops", JoinType.LEFT);


        System.out.println("ok qq");
        DataSetImport impor = entityManager
                .createNamedQuery("DataSetImport.loadAll",DataSetImport.class)
                .setParameter("id",3)
                .getSingleResult();
        System.out.println("ok ss");
    }
}
