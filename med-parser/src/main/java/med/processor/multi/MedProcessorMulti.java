package med.processor.multi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import med.common.MedEntry;
import med.common.PredicateName;
import med.predicate.AllAffiliationsContainsPredicate;
import med.predicate.FirstAffiliationContainsPredicate;
import med.processor.Processor;
import med.processor.multi.config.AppConfig;
import med.processor.multi.config.CountryConfig;
import one.util.streamex.EntryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;
import static java.util.Arrays.stream;
import static java.util.Collections.shuffle;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

@Service
public class MedProcessorMulti implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(MedProcessorMulti.class);
    private static final Pattern pattern = Pattern.compile(".*?(\\d+?)\\. (.+?)\\r\\n\\r\\n(.+?)\\r\\n\\r\\n(.+?)Author information: \\r\\n(.+?)\\r\\n\\r\\n(.+)", Pattern.DOTALL);

    private String configFileName;
    private boolean dryRun;
    private Set<String> countries;
    private Random random;

    public MedProcessorMulti(@Value("${countries:}") Set<String> countries,
                             @Value("${dryRun:false}") boolean dryRun,
                             @Value("${config:}") String configFileName,
                             @Value("${randomSeed:372893751}") Long seed) {
        this.countries = countries;
        this.dryRun = dryRun;
        this.configFileName = configFileName;
        this.random = new Random(seed);
    }

    @Override
    public void run() {
        AppConfig appConfig = loadCountryConfig();

        Map<String, CountryConfig> configMap = countries.isEmpty()
                ? appConfig.getCountries()
                : EntryStream.of(appConfig.getCountries()).filterKeys(countries::contains).toImmutableMap();

        logger.info("Country list: {}", configMap.keySet());

        EntryStream.of(configMap)
                .forKeyValue((countryName, config) -> processCountry(countryName, appConfig.getBaseDir(), config));
    }

    private void processCountry(String countryName, String baseDir, CountryConfig config) {
        String content;

        try {
            content = Files.readString(Paths.get(baseDir + config.getInput()));
        } catch (Exception e) {
            logger.error("Unable to read input file {}", config.getInput(), e);
            return;
        }

        logger.info("Processing country: {}", countryName);

        Map<Integer, MedEntry> matched = synchronizedMap(new TreeMap<>());
        Map<Integer, MedEntry> unmatched = synchronizedMap(new TreeMap<>());

        Predicate<MedEntry> predicate;
        if (config.getPredicate() == PredicateName.FIRST_AFFILIATION) {
            logger.warn("Using FIRST_AFFILIATION predicate!");
            predicate = new FirstAffiliationContainsPredicate(config.getKeywords());
        } else {
            predicate = new AllAffiliationsContainsPredicate(config.getKeywords());
        }

        stream(content.split("(?=\\r\\n\\r\\n\\r\\n\\d)"))
                .parallel()
                .map(this::map)
                .filter(Objects::nonNull)
                .forEach(medEntry -> {
                    if (predicate.test(medEntry)) {
                        matched.put(medEntry.id, medEntry);
                    } else {
                        unmatched.put(medEntry.id, medEntry);
                    }
                });

        var matchedEntries = matched.values().stream()
                .map(MedEntry::getAll)
                .collect(Collectors.toList());

        List<MedEntry> shuffled = new ArrayList<>(matched.values());
        shuffle(shuffled, random);

        var selectionEntries = shuffled.stream()
                .map(MedEntry::getAll)
                .collect(Collectors.toList());

        List<List<String>> selectionPartitioned = Lists.partition(selectionEntries, config.getSelectionSize());

        var unmatchedEntries = unmatched.values().stream()
                .map(MedEntry::getAll)
                .collect(Collectors.toList());

        logger.info("\n{}:\n\tMatched: {}\n\tUnmatched: {}",
                countryName, matched.size(), unmatched.size());

        if (!dryRun) {
            try {
                Files.write(Paths.get(baseDir + String.format("%s_matched_%d.txt", config.getOutput(), matchedEntries.size())), matchedEntries, CREATE, WRITE, TRUNCATE_EXISTING);
                //Files.write(Paths.get(baseDir + String.format("%s_unmatched_%d.txt", config.getOutput(), unmatchedEntries.size())), unmatchedEntries, CREATE, WRITE, TRUNCATE_EXISTING);

                for (int i = 0; i < 4; i++) {
                    Files.write(Paths.get(baseDir + String.format("%s_selection_%d_%d.txt", config.getOutput(), selectionPartitioned.get(i).size(), i)), selectionPartitioned.get(i), CREATE, WRITE, TRUNCATE_EXISTING);
                }

                logger.info("Successfully written to files");
            } catch (Exception e) {
                logger.error("Unable to write output for country {}", countryName, e);
            }
        }
    }

    private MedEntry map(String src) {
        Matcher m = pattern.matcher(src);

        if (!m.find()) {
            logger.warn("Bad entry: {}", src.substring(0, 100).replaceAll("\r\n", " "));
            return null;
        }

        var id = Integer.parseInt(m.group(1));
        var publisher = m.group(2);
        var title = m.group(3);
        var authorsAndCollaborators = m.group(4);
        var affilations = m.group(5);
        var text = m.group(6);

        List<String> affiliationList = stream(affilations.split("\\(\\d+\\)"))
                .filter(not(String::isBlank))
                .collect(Collectors.toList());

        return new MedEntry(id, publisher, title, authorsAndCollaborators, affiliationList, text, src);
    }

    private AppConfig loadCountryConfig() {
        logger.info("Loading config from {}", configFileName);

        try (InputStream is = new FileInputStream(configFileName)) {
            var objectMapper = new ObjectMapper(new YAMLFactory());
            return objectMapper.readerFor(AppConfig.class).readValue(requireNonNull(is));

        } catch (Exception e) {
            throw new RuntimeException("Unable to read countries config", e);
        }
    }

}
