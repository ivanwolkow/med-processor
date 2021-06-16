package med.predicate;

import lombok.RequiredArgsConstructor;
import med.common.MedEntry;

import java.util.List;

@RequiredArgsConstructor
public class AllAffiliationsContainsPredicate implements BasePredicate {
    private final List<String> keywords;

    @Override
    public boolean test(MedEntry medEntry) {
        return medEntry.getAffiliations().stream()
                .allMatch(author -> keywords.stream()
                        .anyMatch(author::contains));
    }

}
