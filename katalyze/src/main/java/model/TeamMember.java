package model;

public class TeamMember implements ApiEntity {
    private final String id;
    public final String teamId;
    public final String name;
    public final String role;

    public TeamMember(String teamMemberId, String teamId, String name, String role) {
        this.id = teamMemberId;
        this.teamId = teamId;
        this.name = name;
        this.role = role;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
