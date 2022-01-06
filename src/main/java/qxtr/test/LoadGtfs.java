package qxtr.test;

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qxtr.loader.gtfs.GtfsLoader;
import qxtr.model.dataset.DataSet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@Order(2)
public class LoadGtfs implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    protected void load(String file,DataSet dataSet) throws IOException {
        GtfsLoader loader=new GtfsLoader(dataSet);
        InputStream inputStream=new FileInputStream(file);
        loader.load(inputStream);
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        var session=entityManager.unwrap(Session.class);
        session.setJdbcBatchSize(100000);
        session.setCacheMode(CacheMode.IGNORE);
        DataSet dataSet=new DataSet();
        dataSet.setName("Test");
        entityManager.persist(dataSet);
        load(args[0],dataSet);
    }
}
