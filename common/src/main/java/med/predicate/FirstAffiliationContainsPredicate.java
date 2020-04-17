package med.predicate;

import med.common.MedEntry;
import one.util.streamex.StreamEx;

import java.util.List;

public class FirstAffiliationContainsPredicate implements BasePredicate {
    private List<String> keywords;

    public FirstAffiliationContainsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(MedEntry medEntry) {
        return StreamEx.of(medEntry.affiliations)
                .findFirst()
                .map(this::reduceAffiliation)
                .filter(author -> keywords.stream().anyMatch(author::contains))
                .isPresent();
    }

    public String reduceAffiliation(String src) {
        int i = src.indexOf(";");
        if (i >= 0) return src.substring(0, i);
        return src;
    }

}