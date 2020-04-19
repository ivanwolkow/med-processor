package med.service;

import com.google.common.io.Resources;
import med.common.MedEntry;
import med.common.MedEntryReduced;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class MedEntryParserTest {

    private MedEntryParser parser;

    @BeforeEach
    void setUp() {
        parser = new MedEntryParser();
    }

    @Test
    void parseMultipleEntries() {
        String s = loadResource("med_entries_3.txt");
        Map<String, MedEntry> result = parser.parse(s);
        Assertions.assertEquals(3, result.size());
    }

    @Test
    void parseSingleEntry() {
        String s = loadResource("med_entries_1.txt");
        Map<String, MedEntry> result = parser.parse(s);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void parseNoEntries() {
        String s = loadResource("med_entries_0.txt");
        Map<String, MedEntry> result = parser.parse(s);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void parseSaveParseTest() {
        String s = loadResource("med_entries_3.txt");

        Map<String, MedEntry> parsed = parser.parse(s);
        String saved = parser.print(parsed.values());
        LinkedHashMap<String, MedEntry> result = parser.parse(saved);

        Assertions.assertEquals(3, result.size());
    }

    @Test
    void saveReducedTest() {
        String full = loadResource("med_entries_1.txt");

        Collection<MedEntry> entries = parser.parse(full).values();
        List<MedEntryReduced> reduced = parser.reduceAll(entries);

        String result = parser.printReduced(reduced);

        String refString = loadResource("med_entries_1_reduced.txt")
                .replace("\r\n", "\n");

        Assertions.assertEquals(refString, result);
    }

    @Test
    void parseReducedEntries() {
        String full = loadResource("med_entries_reduced_3.txt");

        Collection<MedEntryReduced> entriesReduced = parser.parseReduced(full).values();
        Assertions.assertEquals(3, entriesReduced.size());
    }

    private String loadResource(String name) {
        try {
            URL resource = Resources.getResource(name);
            return Resources.toString(resource, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}