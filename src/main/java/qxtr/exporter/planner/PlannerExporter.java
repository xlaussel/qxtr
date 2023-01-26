package qxtr.exporter.planner;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import qxtr.exporter.ByteOutputStreamWriter;
import qxtr.model.transport.area.AreaConfiguration;
import qxtr.model.transport.area.Transfer;
import qxtr.model.transport.dataset.schedules.VehicleJourney;
import qxtr.model.transport.dataset.topology.JourneyPattern;
import qxtr.model.transport.dataset.topology.JourneyPatternStop;
import qxtr.model.transport.dataset.topology.Stop;
import qxtr.model.transport.dataset.topology.StopGroup;
import qxtr.repositories.transport.dataset.schedules.JourneyPatternRepository;
import qxtr.repositories.transport.dataset.schedules.VehicleJourneyRepository;
import qxtr.repositories.transport.dataset.topology.StopGroupRepository;
import qxtr.repositories.transport.dataset.topology.TransferRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.HashMap;

public class PlannerExporter {

    @PersistenceContext(type=PersistenceContextType.EXTENDED)
    EntityManager entityManager;

    @Autowired
    StopGroupRepository stopGroupRepository;

    @Autowired
    TransferRepository transferRepository;
    
    @Autowired
    JourneyPatternRepository journeyPatternRepository;            

    VehicleJourneyRepository vehicleJourneyRepository;

    public void export(AreaConfiguration areaConfiguration, OutputStream outputStream) throws IOException {

        ByteOutputStreamWriter outputStreamWriter=new ByteOutputStreamWriter(outputStream);

        Session session=entityManager.unwrap(Session.class);

        session
                .enableFilter("dataSetImports")
                .setParameterList("dataSetImports",areaConfiguration.getDataSetImports())
                .validate();

        session
                .enableFilter("areaConfiguration")
                .setParameter("areaConfiguration",areaConfiguration)
                .validate();

        writeStops(outputStreamWriter);
        writeTransfers(outputStreamWriter);
        writeJourneyPatterns(outputStreamWriter);
        writeVehicleJourneys(outputStreamWriter);
    }

    private void writeStops(ByteOutputStreamWriter outputStreamWriter) throws IOException {
        var stopGroups=stopGroupRepository.findAllWithStops();
        outputStreamWriter.writeInt(stopGroups.size());
        for (StopGroup stopGroup:stopGroups) {
            outputStreamWriter.writeLong(stopGroup.getId());
            outputStreamWriter.writeInt(stopGroup.getStops().size());
            for (Stop stop:stopGroup.getStops()) {
                outputStreamWriter.writeLong(stop.getId());
            }
        }
    }

    private void writeTransfers(ByteOutputStreamWriter outputStreamWriter) throws IOException {
        var transfers=transferRepository.listAll();
        outputStreamWriter.writeInt(transfers.size());
        for (Transfer transfer:transfers) {
            outputStreamWriter
                    .writeLong(transfer.getStart().getId())
                    .writeLong(transfer.getEnd().getId())
                    .writeInt(transfer.getDistance());
        }
    }

    private void writeVehicleJourneys(ByteOutputStreamWriter outputStreamWriter) throws IOException {
        var vehicleJourneys=vehicleJourneyRepository.listAll();
        outputStreamWriter.writeInt(vehicleJourneys.size());
        for (VehicleJourney vehicleJourney:vehicleJourneys) {
            outputStreamWriter.writeLong(vehicleJourney.getId());
            outputStreamWriter.writeBoolean(vehicleJourney.getSchedules().waitAtStops());
            outputStreamWriter.writeInt(vehicleJourney.getSchedules().getArrivals().length);
            outputStreamWriter.writeInts(vehicleJourney.getSchedules().getArrivals());
            if (vehicleJourney.getSchedules().waitAtStops()) {
                outputStreamWriter.writeInt(vehicleJourney.getSchedules().getDepartures().length);
                outputStreamWriter.writeInts(vehicleJourney.getSchedules().getDepartures());
            }
        }
    }

    private void writeJourneyPatterns(ByteOutputStreamWriter outputStreamWriter) throws IOException {
        var journeyPatterns=journeyPatternRepository.listAllWithStops();
        outputStreamWriter.writeInt(journeyPatterns.size());
        for (JourneyPattern journeyPattern:journeyPatterns) {
            writeJourneyPattern(outputStreamWriter,journeyPattern);
        };
    }

    private void writeJourneyPattern(ByteOutputStreamWriter outputStreamWriter,JourneyPattern journeyPattern) throws IOException {
        outputStreamWriter.writeLong(journeyPattern.getId());
        outputStreamWriter.writeInt(journeyPattern.getJourneyPatternStops().size());
        for (JourneyPatternStop journeyPatternStop:journeyPattern.getJourneyPatternStops()) {
            outputStreamWriter.writeLong(journeyPatternStop.getStop().getId());
            outputStreamWriter.writeByte((byte) ((journeyPatternStop.isAlightAllowed()?1:0) + (journeyPatternStop.isBoardAllowed()?2:0)));
        }
        HashMap<LocalDate,VehicleJourney> vehicleJourneys=new HashMap<>();
        journeyPattern.getVehicleJourneys().forEach(vehicleJourney -> {
            vehicleJourney.getTimeTables().forEach(timeTable -> {
                timeTable.getDates().forEach(date -> {
                    vehicleJourneys.put(date,vehicleJourney);
                });
            });
        });

    }

}
