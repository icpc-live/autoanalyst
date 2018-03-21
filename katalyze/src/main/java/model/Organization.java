package model;

public class Organization implements ApiEntity{
    private String id;
    private String name;
    private String fullName;
    private String country;
    private String twitterHashTag;


    public Organization(String id, String shortName, String fullName, String country, String twitterHashTag) {
        this.id = id;
        this.name = shortName;
        this.fullName = fullName;
        this.country = country;
        this.twitterHashTag = twitterHashTag;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return name;
    }

    public String getFullName() { return fullName; }

    public String getCountry() { return country; }

    public String toString() {
        return String.format("#%s - %s", id, name);
    }

}
