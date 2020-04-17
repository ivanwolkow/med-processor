package med.predicate;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FirstAffiliationContainsPredicateTest {

    @Test
    void reduceAffiliation() {
        FirstAffiliationContainsPredicate predicate = new FirstAffiliationContainsPredicate(List.of("abc"));
        String result = predicate.reduceAffiliation("aaa;bbb;ccc");
        assertEquals("aaa", result);
    }
}