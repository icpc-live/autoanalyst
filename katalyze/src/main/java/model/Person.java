package model;

public class Person implements ApiEntity {
    private final String id;
    public final String[] teamIds;
    public final String name;
    public final String role;

    public Person(String personId, String[] teamIds, String name, String role) {
        this.id = personId;
        this.teamIds = teamIds;
        this.name = name;
        this.role = role;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
