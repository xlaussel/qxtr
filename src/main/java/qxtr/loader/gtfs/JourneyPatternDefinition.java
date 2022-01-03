package qxtr.loader.gtfs;

import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode
public class JourneyPatternDefinition {
    public List<String> stops=new LinkedList<>();
    public Set<Integer> alightRestrictions=new HashSet<>();
    public Set<Integer> boardRestrictions=new HashSet<>();
}
