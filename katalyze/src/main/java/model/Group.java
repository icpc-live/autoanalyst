package model;

public class Group implements ApiEntity {
    private final String id;
    private final String groupName;

    public Group(String id, String groupName) {
        this.id = id;
        this.groupName = groupName;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.groupName;
    }
}
