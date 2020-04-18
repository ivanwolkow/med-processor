package med.service;

import med.common.MedEntry;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
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

    public MedEntryParser() {
    }

    public LinkedHashMap<Integer, MedEntry> parse(String src) {
        if (StringUtils.isBlank(src)) return new LinkedHashMap<>();

        return StreamEx.of(src.split(splitToEntriesPattern))
                .parallel()
                .map(this::map)
                .remove(Objects::isNull)
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getId(), entry), HashMap::putAll);
    }

    public String save(Collection<MedEntry> entries) {
        return entries.stream()
                .map(MedEntry::getAll)
                .collect(joining(entrySeparator));
    }

    public String saveReduced(Collection<MedEntry> entries) {
        return entries.stream()
                .map(MedEntryParser::convertToStringReduced)
                .collect(joining(fieldSeparator));
    }

    private static String convertToStringReduced(MedEntry medEntry) {
        return medEntry.getId() + ". " + medEntry.getPublisher() + fieldSeparator +
                medEntry.getTitle() + fieldSeparator +
                medEntry.getText() + fieldSeparator;
    }

    private MedEntry map(String src1) {
        String src = StringUtils.trimToEmpty(src1).replaceAll("\r\n", "\n");

        Matcher m = entryPattern.matcher(src);

        if (!m.find()) {
            logger.warn("Bad entry: {}", src.substring(0, Math.min(src.length(), 20)));
            return null;
        }

        var id = Integer.parseInt(m.group(1));
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
}
