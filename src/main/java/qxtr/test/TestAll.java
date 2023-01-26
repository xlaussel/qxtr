package qxtr.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
public class TestAll implements CommandLineRunner {

    @Autowired
    LoadGtfs loader;

    @Autowired
    FetchDs fetcher;

    public void run(String... args) throws Exception {
        int id=loader.createAndload(args[0],Integer.parseInt(args[1]));
        //fetcher.fetch(3);
    }
}
