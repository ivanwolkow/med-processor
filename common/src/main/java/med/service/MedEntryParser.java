package med.service;

import med.common.MedEntry;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

public class MedEntryParser {

    private static final Logger logger = LoggerFactory.getLogger(MedEntryParser.class);

    private static final String splitToEntriesPattern = "\\r\\n(?=\\r\\n\\r\\n\\d+\\.)";
    private static final Pattern entryPattern = Pattern.compile(".*?(\\d+?)\\. (.+?)\\r\\n\\r\\n(.+?)\\r\\n\\r\\n(.+?)Author information:.*?\\r\\n(.+?)\\r\\n\\r\\n(.+)", Pattern.DOTALL);

    public MedEntryParser() {
    }

    public Map<Integer, MedEntry> parse(String src) {
        return StreamEx.of(src.split(splitToEntriesPattern))
                .parallel()
                .map(this::map)
                .remove(Objects::isNull)
                .toMap(MedEntry::getId, identity());
    }

    private MedEntry map(String src) {
        Matcher m = entryPattern.matcher(src);

        if (!m.find()) {
            logger.warn("Bad entry: {}", src.substring(0, 20).replaceAll("\r\n", " "));
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
