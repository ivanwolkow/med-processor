package med.common;

public class MedEntryReduced {
    private String id;
    private String publisher;
    private String title;
    private String text;

    public MedEntryReduced() {
    }

    public MedEntryReduced(String id, String publisher, String title, String text) {
        this.id = id;
        this.publisher = publisher;
        this.title = title;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }
}
