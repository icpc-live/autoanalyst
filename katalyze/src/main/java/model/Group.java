package model;

public class Group implements ApiEntity {
    private final String id;
    private final String groupName;
    private final boolean hidden;

    public Group(String id, String groupName, boolean hidden) {
        this.id = id;
        this.groupName = groupName;
        this.hidden = hidden;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return groupName;
    }

    public boolean isHidden() { return hidden; }
}
