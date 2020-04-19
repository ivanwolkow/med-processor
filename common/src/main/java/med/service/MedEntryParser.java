package med.service;

import med.common.MedEntry;
import med.common.MedEntryReduced;
import one.util.streamex.StreamEx;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class MedEntryParser {

    private static final Logger logger = LoggerFactory.getLogger(MedEntryParser.class);

    private static final String fieldSeparator = "\n\n";
    private static final String entrySeparator = "\n\n\n";
    private static final String splitToEntriesPattern = "[\\n[\\r\\n]]{3}(?=\\d+\\. )";

    private static final Pattern entryPattern = Pattern.compile(".*?(\\d+?)\\. (.+?)\\R\\R(.+?)\\R\\R(.+?)Author information:.*?\\R(.+?)\\R\\R(.+)", Pattern.DOTALL);
    private static final Pattern reducedEntryPattern = Pattern.compile(".*?(\\d+?)\\. (.+?)\\R\\R(.+?)\\R\\R(.+)", Pattern.DOTALL);

    public MedEntryParser() {
    }

    public LinkedHashMap<String, MedEntry> parse(String src) {
        if (StringUtils.isBlank(src)) return new LinkedHashMap<>();

        return StreamEx.of(src.split(splitToEntriesPattern))
                .parallel()
                .map(this::map)
                .remove(Objects::isNull)
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getId(), entry), HashMap::putAll);
    }

    public LinkedHashMap<String, MedEntryReduced> parseReduced(String src) {
        if (StringUtils.isBlank(src)) return new LinkedHashMap<>();

        return StreamEx.of(src.split(splitToEntriesPattern))
                .parallel()
                .map(this::mapReduced)
                .remove(Objects::isNull)
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getId(), entry), HashMap::putAll);
    }

    public String print(Collection<MedEntry> entries) {
        return entries.stream()
                .map(MedEntry::getAll)
                .collect(joining(entrySeparator));
    }

    public String printReduced(Collection<MedEntryReduced> entries) {
        return entries.stream()
                .map(MedEntryParser::convertToStringReduced)
                .collect(joining(entrySeparator));
    }

    public MedEntryReduced reduce(MedEntry entry) {
        return new MedEntryReduced(entry.getId(), entry.getPublisher(), entry.getTitle(), entry.getText());
    }

    public List<MedEntryReduced> reduceAll(Collection<MedEntry> entries) {
        return StreamEx.of(entries)
                .map(this::reduce)
                .toList();
    }

    private static String convertToStringReduced(MedEntryReduced medEntryReduced) {
        return medEntryReduced.getId() + ". " + medEntryReduced.getPublisher() + fieldSeparator +
                medEntryReduced.getTitle() + fieldSeparator +
                medEntryReduced.getText();
    }

    private MedEntry map(String src1) {
        String src = StringUtils.trimToEmpty(src1).replaceAll("\r\n", "\n");

        Matcher m = entryPattern.matcher(src);

        if (!m.find()) {
            logger.warn("Bad entry: {}", src.substring(0, Math.min(src.length(), 20)));
            return null;
        }

        var id = m.group(1);
        var publisher = m.group(2);
        var title = m.group(3);
        var authorsAndCollaborators = m.group(4);
        var affiliations = m.group(5);
        var text = m.group(6);

        List<String> affiliationList = stream(affiliations.split("\\(\\d+\\)"))
                .filter(not(String::isBlank))
                .collect(toList());

        return new MedEntry(id, publisher, title, authorsAndCollaborators, affiliationList, text, src);
    }

    private MedEntryReduced mapReduced(String src1) {
        String src = StringUtils.trimToEmpty(src1).replaceAll("\r\n", "\n");

        Matcher m = reducedEntryPattern.matcher(src);

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

}
