package model;

public class Organization implements ApiEntity{
    private String id;
    private String name;
    private String twitterHashTag;


    public Organization(String id, String name, String twitterHashTag) {
        this.id = id;
        this.name = name;
        this.twitterHashTag = twitterHashTag;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return name;
    }

    public String toString() {
        return String.format("#%s - %s", id, name);
    }

}
