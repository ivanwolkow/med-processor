package med.predicate;

import med.common.MedEntry;

import java.util.List;

public class AllAffiliationsContainsPredicate implements BasePredicate {
    private List<String> keywords;

    public AllAffiliationsContainsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(MedEntry medEntry) {
        return medEntry.getAffiliations().stream()
                .allMatch(author -> keywords.stream()
                        .anyMatch(author::contains));
    }

}
