package med.processor.single.config;

import med.common.PredicateName;

import java.util.List;

public class CountryConfig {
    private List<String> keywords;
    private PredicateName predicate;

    public CountryConfig() {
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
