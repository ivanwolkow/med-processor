package med.processor.multi.config;

import med.common.PredicateName;

import java.util.List;

public class CountryConfig {
    private String input;
    private String output;
    private Integer selectionSize;
    private List<String> keywords;
    private PredicateName predicate;

    public CountryConfig() {
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Integer getSelectionSize() {
        return selectionSize;
    }

    public void setSelectionSize(Integer selectionSize) {
        this.selectionSize = selectionSize;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public PredicateName getPredicate() {
        return predicate;
    }

    public void setPredicate(PredicateName predicate) {
        this.predicate = predicate;
    }
}
