package med.service;


import med.common.MedEntry;
import med.common.MedEntryReduced;
import one.util.streamex.StreamEx;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;

import static java.util.stream.Collectors.joining;
import static med.constants.DocumentStyleConstants.*;

public class ReducedMedEntryParser {

    private final static Logger logger = LoggerFactory.getLogger(ReducedMedEntryParser.class);

    public ReducedMedEntryParser() {
    }

    public LinkedHashMap<String, MedEntryReduced> parseReduced(String src) {
        if (StringUtils.isBlank(src)) return new LinkedHashMap<>();

        return StreamEx.of(src.split(SPLIT_TO_ENTRIES_PATTERN))
                .parallel()
                .map(this::mapReduced)
                .remove(Objects::isNull)
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getId(), entry), HashMap::putAll);
    }

    public String joinReduced(Collection<MedEntryReduced> entries) {
        return entries.stream()
                .map(ReducedMedEntryParser::convertToStringReduced)
                .collect(joining(ENTRY_SEPARATOR));
    }

    public MedEntryReduced reduce(MedEntry entry) {
        return new MedEntryReduced(entry.getId(), entry.getPublisher(), entry.getTitle(), entry.getText());
    }

    public List<MedEntryReduced> reduceAll(Collection<MedEntry> entries) {
        return StreamEx.of(entries)
                .map(this::reduce)
                .toList();
    }

    private MedEntryReduced mapReduced(String src1) {
        String src = StringUtils.trimToEmpty(src1)
                .replaceAll("\r\n", "\n");

        Matcher m = REDUCED_ENTRY_PATTERN.matcher(src);

        if (!m.find()) {
            logger.warn("Bad entry: {}", src.substring(0, Math.min(src.length(), 20)));
            return null;
        }

        var id = m.group(1);
        var publisher = m.group(2);
        var title = m.group(3);
        var text = m.group(4);

        return new MedEntryReduced(id, publisher, title, text);
    }

    private static String convertToStringReduced(MedEntryReduced medEntryReduced) {
        return medEntryReduced.getId() + ". " + medEntryReduced.getPublisher() + FIELD_SEPARATOR +
                medEntryReduced.getTitle() + FIELD_SEPARATOR +
                medEntryReduced.getText();
    }
}
