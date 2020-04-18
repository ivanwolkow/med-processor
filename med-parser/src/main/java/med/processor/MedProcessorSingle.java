package med.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import med.processor.config.AppConfig;
import med.processor.config.CountryConfig;
import one.util.streamex.EntryStream;
import med.common.MedEntry;
import med.common.PredicateName;
import med.predicate.AllAffiliationsContainsPredicate;
import med.predicate.FirstAffiliationContainsPredicate;
import med.service.MedEntryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MedProcessorSingle implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(MedProcessorSingle.class);

    private String configFileName;
    private boolean dryRun;
    private Set<String> countries;
    private Random random;

    private MedEntryParser medEntryParser;

    public MedProcessorSingle(@Value("${countries:}") Set<String> countries,
                              @Value("${dryRun:false}") boolean dryRun,
                              @Value("${config:}") String configFileName,
                              @Value("${randomSeed:372893751}") Long seed,
                              MedEntryParser medEntryParser) {
        this.countries = countries;
        this.dryRun = dryRun;
        this.configFileName = configFileName;
        this.random = new Random(seed);
        this.medEntryParser = medEntryParser;
    }

    @Override
    public void run() {
        AppConfig appConfig = loadCountryConfig();

        Map<String, CountryConfig> configMap = countries.isEmpty()
                ? appConfig.getCountries()
                : EntryStream.of(appConfig.getCountries()).filterKeys(countries::contains).toImmutableMap();

        logger.info("Country list: {}", configMap.keySet());

        try {
            String content = Files.readString(Paths.get(appConfig.getInputFileName()));

            logger.info("Reading med entries...");
            var allEntries = medEntryParser.parse(content);
            logger.info("-----------------------------------------");

            EntryStream.of(configMap)
                    .forKeyValue((countryName, countryConfig) -> processCountry(allEntries, appConfig, countryName, countryConfig));
        } catch (Exception e) {
            logger.error("error", e);
        }
    }

    private void processCountry(Map<Integer, MedEntry> allEntries, AppConfig appConfig, String countryName, CountryConfig countryConfig) {
        logger.info("Processing country: {}", countryName);

        Map<Integer, MedEntry> matched = synchronizedMap(new TreeMap<>());
        Map<Integer, MedEntry> unmatched = synchronizedMap(new TreeMap<>());

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

        logger.info("\tMatched: {}\tUnmatched: {}", matched.size(), unmatched.size());

        if (!dryRun) {
            try {

                File outputDir = new File(appConfig.getOutputDir());
                outputDir.mkdirs();
                if (!outputDir.exists()) {
                    throw new RuntimeException("Failed to create output directory");
                }

                Files.write(Paths.get(appConfig.getOutputDir() + String.format("/%s_out_matched_%d.txt", countryName, matched.size())),
                        matched.values().stream().map(MedEntry::getAll).collect(toList()), CREATE, WRITE, TRUNCATE_EXISTING);

                Files.write(Paths.get(appConfig.getOutputDir() + String.format("/%s_out_matched_shuffled_%d.txt", countryName, shuffled.size())),
                        shuffled.stream().map(MedEntry::getAll).collect(toList()), CREATE, WRITE, TRUNCATE_EXISTING);

                var chunks = Math.min(appConfig.getPartitionNumber(), partition.size());
                if (chunks < appConfig.getPartitionNumber())
                    logger.warn("Not enough matched entries for splitting into {} partitions by {} entries", appConfig.getPartitionNumber(), appConfig.getPartitionSize());

                for (int i = 0; i < chunks; i++) {
                    Files.write(Paths.get(appConfig.getOutputDir() + String.format("/%s_out_partition_%d_%d.txt", countryName, i, partition.get(i).size())), partition.get(i), CREATE, WRITE, TRUNCATE_EXISTING);
                }

                logger.info("-----------------------------------------\n");

            } catch (Exception e) {
                logger.error("error", e);
            }
        }
    }

    private Predicate<MedEntry> createPredicate(PredicateName predicateName, List<String> keywords) {
        if (predicateName == PredicateName.FIRST_AFFILIATION) {
            logger.warn("Using FIRST_AFFILIATION predicate!");
            return new FirstAffiliationContainsPredicate(keywords);
        } else {
            return new AllAffiliationsContainsPredicate(keywords);
        }
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
