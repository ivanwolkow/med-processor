package med;

import med.service.MedEntryParser;
import one.util.streamex.EntryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;

/**
 * Cuts off the authors, collaborators and affiliations, thus producing the "reduced" (or "blinded") publication list
 */
@SpringBootApplication
public class BlinderApp implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BlinderApp.class);

    private MedEntryParser medEntryParser;

    public BlinderApp(MedEntryParser medEntryParser) {
        this.medEntryParser = medEntryParser;
    }

    public void run(ApplicationArguments args) throws IOException {

        Set<String> optionNames = args.getOptionNames();
        if (!optionNames.containsAll(Set.of("in", "out"))) {
            logger.error("Usage: java -jar blinder.jar --in=input.txt --out=output.txt");
            return;
        }

        var inputFileName = args.getOptionValues("in").get(0);
        var outputFileName = args.getOptionValues("out").get(0);

        String input = Files.readString(Paths.get(inputFileName));

        logger.info("Parsing input...");
        var allEntries = medEntryParser.parse(input);

        logger.info("Found {} entries", allEntries.size());

        File outputFile = new File(outputFileName);
        File outputDir = outputFile.getParentFile();

        if (outputDir != null) {
            outputDir.mkdirs();
            if (!outputDir.exists()) {
                logger.error("Failed to create output dir {}", outputDir);
                return;
            }
        }

        var reduced = EntryStream.of(allEntries)
                .mapValues(medEntryParser::reduce)
                .values()
                .toList();

        var result = medEntryParser.printReduced(reduced);
        Files.writeString(Paths.get(outputFileName), result, UTF_8, WRITE, TRUNCATE_EXISTING, CREATE);
    }

    public static void main(String[] args) throws IOException {
        SpringApplication springApplication = new SpringApplicationBuilder()
                .web(WebApplicationType.NONE)
                .sources(BlinderApp.class)
                .build();

        springApplication.run(args);
    }

}
