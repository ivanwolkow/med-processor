package med.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import med.common.MedEntry;
import med.common.PredicateName;
import med.predicate.AllAffiliationsContainsPredicate;
import med.predicate.FirstAffiliationContainsPredicate;
import med.processor.config.AppConfig;
import med.processor.config.CountryConfig;
import med.service.MedEntryParser;
import one.util.streamex.EntryStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

import static java.nio.file.StandardOpenOption.*;
import static java.util.Collections.shuffle;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class MedProcessorSingle implements Processor {

    private final String configFileName;
    private final boolean dryRun;
    private final Set<String> countries;
    private final Random random;

    private final MedEntryParser medEntryParser;

    public MedProcessorSingle(@Value("${countries:}") Set<String> countries,
                              @Value("${dryRun:false}") boolean dryRun,
                              @Value("${config:}") String configFileName,
                              @Value("${randomSeed:372893751}") Long seed) {
        this.countries = countries;
        this.dryRun = dryRun;
        this.configFileName = configFileName;
        this.random = new Random(seed);
        this.medEntryParser = new MedEntryParser();
    }

    @Override
    @SneakyThrows
    public void run(String[] args) {
        AppConfig appConfig = loadCountryConfig();

        Map<String, CountryConfig> configMap = countries.isEmpty()
                ? appConfig.getCountries()
                : EntryStream.of(appConfig.getCountries()).filterKeys(countries::contains).toImmutableMap();

        log.info("Country list: {}", configMap.keySet());

        String content = Files.readString(Paths.get(appConfig.getInputFileName()));

        log.info("Reading med entries...");
        var allEntries = medEntryParser.parse(content);
        log.info("-----------------------------------------");

        EntryStream.of(configMap)
                .forKeyValue((countryName, countryConfig) -> processCountry(allEntries, appConfig, countryName, countryConfig));
    }

    @SneakyThrows
    private void processCountry(Map<String, MedEntry> allEntries, AppConfig appConfig, String countryName, CountryConfig countryConfig) {
        log.info("Processing country: {}", countryName);

        Map<String, MedEntry> matched = synchronizedMap(new TreeMap<>());
        Map<String, MedEntry> unmatched = synchronizedMap(new TreeMap<>());

        Predicate<MedEntry> predicate = createPredicate(countryConfig.getPredicate(), countryConfig.getKeywords());

        EntryStream.of(allEntries)
                .parallel()
                .forKeyValue((id, medEntry) -> {
                    if (predicate.test(medEntry)) {
                        matched.put(id, medEntry);
                    } else {
                        unmatched.put(id, medEntry);
                    }
                });

        List<MedEntry> shuffled = new ArrayList<>(matched.values());
        shuffle(shuffled, random);

        var partitionEntries = shuffled.stream()
                .map(MedEntry::getAll)
                .collect(toList());

        List<List<String>> partition = Lists.partition(partitionEntries, appConfig.getPartitionSize());

        log.info("\tMatched: {}\tUnmatched: {}", matched.size(), unmatched.size());

        if (!dryRun) {
            File outputDir = new File(appConfig.getOutputDir());
            outputDir.mkdirs();
            if (!outputDir.exists()) throw new RuntimeException("Failed to create output directory");

            var resultMatched = medEntryParser.join(matched.values());
            var resultShuffled = medEntryParser.join(shuffled);

            Files.writeString(Paths.get(appConfig.getOutputDir() + String.format("/%s_out_matched_%d.txt", countryName, matched.size())),
                    resultMatched, CREATE, WRITE, TRUNCATE_EXISTING);

            Files.writeString(Paths.get(appConfig.getOutputDir() + String.format("/%s_out_matched_shuffled_%d.txt", countryName, shuffled.size())),
                    resultShuffled, CREATE, WRITE, TRUNCATE_EXISTING);

            var chunks = Math.min(appConfig.getPartitionNumber(), partition.size());
            if (chunks < appConfig.getPartitionNumber())
                log.warn("Not enough matched entries for splitting into {} partitions by {} entries",
                        appConfig.getPartitionNumber(), appConfig.getPartitionSize());

            for (int i = 0; i < chunks; i++) {
                Files.write(Paths.get(appConfig.getOutputDir() + String.format("/%s_out_partition_%d_%d.txt",
                        countryName, i, partition.get(i).size())), partition.get(i), CREATE, WRITE, TRUNCATE_EXISTING);
            }

            log.info("-----------------------------------------\n");
        }
    }

    private Predicate<MedEntry> createPredicate(PredicateName predicateName, List<String> keywords) {
        if (predicateName == PredicateName.FIRST_AFFILIATION) {
            log.warn("Using FIRST_AFFILIATION predicate!");
            return new FirstAffiliationContainsPredicate(keywords);
        } else {
            return new AllAffiliationsContainsPredicate(keywords);
        }
    }

    @SneakyThrows
    private AppConfig loadCountryConfig() {
        log.info("Loading config from {}", configFileName);

        try (InputStream is = new FileInputStream(configFileName)) {
            var objectMapper = new ObjectMapper(new YAMLFactory());
            return objectMapper.readerFor(AppConfig.class).readValue(requireNonNull(is));
        }
    }
}
