package med.common;

import java.util.List;

public class MedEntry {

    public int id;
    public String publisher;
    public String title;
    public String authorsAndCollaborators;
    public List<String> affiliations;
    public String text;
    public String all;

    public MedEntry(int id, String publisher, String title, String authorsAndCollaborators, List<String> affiliations, String text, String all) {
        this.id = id;
        this.publisher = publisher;
        this.title = title;
        this.authorsAndCollaborators = authorsAndCollaborators;
        this.affiliations = affiliations;
        this.text = text;
        this.all = all;
    }

    public String getBasicInfo() {
        return String.format("%d. %s\r\n\r\n%s\r\n\r\n%s\r\n\r\n", id, publisher, title, text);
    }

    public String getAll() {
        return all;
    }

    public int getId() {
        return id;
    }
}
