package med.entity;

import java.util.List;

public class MedEntryParserResponse {

    private int id;
    private String publisher;
    private String title;
    private String authorsAndCollabs;
    private List<String> affiliations;
    private String text;

    public MedEntryParserResponse() {
    }

    public MedEntryParserResponse(int id, String publisher, String title, String authorsAndCollabs, List<String> affiliations, String text) {
        this.id = id;
        this.publisher = publisher;
        this.title = title;
        this.authorsAndCollabs = authorsAndCollabs;
        this.affiliations = affiliations;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorsAndCollabs() {
        return authorsAndCollabs;
    }

    public List<String> getAffiliations() {
        return affiliations;
    }

    public String getText() {
        return text;
    }
}
