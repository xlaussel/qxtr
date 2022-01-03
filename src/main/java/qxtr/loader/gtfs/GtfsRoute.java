package qxtr.loader.gtfs;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GtfsRoute {
    public String id;
    public String agencyId;
    public String shortName;
    public String longName;
    public Integer order;
}
