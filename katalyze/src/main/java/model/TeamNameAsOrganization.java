package model;

import java.util.function.Function;

public class TeamNameAsOrganization implements Function<Team, String> {
    public final static TeamNameAsOrganization instance = new TeamNameAsOrganization();

    @Override
    public String apply(Team source) {
        Organization org = source.getOrganization();
        return Organization.isNull(org) ? source.getName() : org.getDisplayName();
    }
}
