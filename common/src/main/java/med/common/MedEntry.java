package med.common;

import java.util.List;

public class MedEntry {

    private int id;
    private String publisher;
    private String title;
    private String authorsAndCollaborators;
    private List<String> affiliations;
    private String text;
    private String all;

    public MedEntry(int id, String publisher, String title, String authorsAndCollaborators, List<String> affiliations, String text, String all) {
        this.id = id;
        this.publisher = publisher;
        this.title = title;
        this.authorsAndCollaborators = authorsAndCollaborators;
        this.affiliations = affiliations;
        this.text = text;
        this.all = all;
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

    public String getAuthorsAndCollaborators() {
        return authorsAndCollaborators;
    }

    public List<String> getAffiliations() {
        return affiliations;
    }

    public String getText() {
        return text;
    }

    public String getAll() {
        return all;
    }
}
