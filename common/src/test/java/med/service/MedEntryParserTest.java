package med.service;

import com.google.common.io.Resources;
import med.common.MedEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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
        Map<Integer, MedEntry> result = parser.parse(s);
        Assertions.assertEquals(3, result.size());
    }

    @Test
    void parseSingleEntry() {
        String s = loadResource("med_entries_1.txt");
        Map<Integer, MedEntry> result = parser.parse(s);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void parseNoEntries() {
        String s = loadResource("med_entries_0.txt");
        Map<Integer, MedEntry> result = parser.parse(s);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void parseSaveParseTest() {
        String s = loadResource("med_entries_3.txt");

        Map<Integer, MedEntry> parsed = parser.parse(s);
        String saved = parser.save(parsed.values());
        LinkedHashMap<Integer, MedEntry> result = parser.parse(saved);

        Assertions.assertEquals(3, result.size());
    }

    @Test
    void saveReducedTest() {
        String full = loadResource("med_entries_1.txt");
        Map<Integer, MedEntry> parsed = parser.parse(full);
        String result = parser.saveReduced(parsed.values());

        String refString = loadResource("med_entries_1_reduced.txt")
                .replace("\r\n", "\n");

        Assertions.assertEquals(refString, result);
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