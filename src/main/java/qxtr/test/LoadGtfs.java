package qxtr.test;

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qxtr.loader.gtfs.GtfsLoader;
import qxtr.model.transport.area.Area;
import qxtr.model.transport.area.AreaConfiguration;
import qxtr.model.transport.dataset.DataSet;
import qxtr.model.transport.dataset.DataSetImport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class LoadGtfs {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    protected DataSetImport load(String file, DataSet dataSet) throws IOException {
        GtfsLoader loader=new GtfsLoader(dataSet);
        InputStream inputStream=new FileInputStream(file);
        return loader.load(inputStream);

    }

    @Transactional
    public int createAndload(String fileName,int bsize) throws IOException {
        var session=entityManager.unwrap(Session.class);
        session.setJdbcBatchSize(bsize);
        session.setCacheMode(CacheMode.IGNORE);
        Area area=new Area();
        AreaConfiguration conf=new AreaConfiguration(area);
        DataSet dataSet=new DataSet(area);
        dataSet.setName("Test");
        DataSetImport dataSetImport=load(fileName,dataSet);
        dataSetImport.setAreaConfiguration(conf);
        entityManager.persist(area);
        return dataSetImport.getId();
    }


}
