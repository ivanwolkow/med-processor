package med.entity;

import com.fasterxml.jackson.annotation.JsonGetter;

public class MedEntryRequest {
    private String source;

    public MedEntryRequest() {
    }

    public MedEntryRequest(String source) {
        this.source = source;
    }

    @JsonGetter
    public String getSource() {
        return source;
    }
}
