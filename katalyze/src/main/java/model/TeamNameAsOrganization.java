package model;

import java.util.function.Function;

public class TeamNameAsOrganization implements Function<Team, String> {

    @Override
    public String apply(Team source) {
        Organization org = source.getOrganization();
        return (org == null) ? source.getName() : org.getDisplayName();
    }
}
