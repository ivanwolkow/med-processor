package med.predicate;

import lombok.RequiredArgsConstructor;
import med.common.MedEntry;
import one.util.streamex.StreamEx;

import java.util.List;

@RequiredArgsConstructor
public class FirstAffiliationContainsPredicate implements BasePredicate {

    private final List<String> keywords;

    @Override
    public boolean test(MedEntry medEntry) {
        return StreamEx.of(medEntry.getAffiliations())
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
