package med;

import med.common.MedEntryReduced;
import med.service.MedEntryParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;


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
            logger.error("Usage: java -jar blinder.jar --in=input.txt --out=output.csv");
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

        if (FilenameUtils.isExtension(outputFileName, "csv")) {
            logger.info("Printing CSV!");
            List<MedEntryReduced> reducedEntries = medEntryParser.reduceAll(allEntries.values());
            CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(new File(outputFileName)), CSVFormat.DEFAULT.withDelimiter(';'));

            for (MedEntryReduced m: reducedEntries) {
                csvPrinter.printRecord(m.getId(), m.getPublisher(), m.getTitle(), m.getText());
            }

            csvPrinter.flush();
            csvPrinter.close();
            return;
        }

        if (FilenameUtils.isExtension(outputFileName, "txt")) {
            var result = medEntryParser.print(allEntries.values());
            Files.writeString(Paths.get(outputFileName), result, UTF_8, WRITE, TRUNCATE_EXISTING, CREATE);
            return;
        }

        logger.error("Unknown output file format!");
    }

    public static void main(String[] args) throws IOException {
        SpringApplication springApplication = new SpringApplicationBuilder()
                .web(WebApplicationType.NONE)
                .sources(BlinderApp.class)
                .build();

        springApplication.run(args);
    }

}
