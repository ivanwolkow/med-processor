package org.example;

import med.common.MedEntryReduced;
import med.service.MedEntryParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Converts input txt file with reduced(blinded) entries to CSV format
 */
@SpringBootApplication
public class FormatterApp implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(FormatterApp.class);

    private MedEntryParser medEntryParser;

    public FormatterApp(MedEntryParser medEntryParser) {
        this.medEntryParser = medEntryParser;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Set<String> optionNames = args.getOptionNames();
        if (!optionNames.contains("in")) {
            logger.error("Usage: java -jar formatter.jar --in=input_blinded.txt");
        }

        var inputFleName = args.getOptionValues("in").get(0);
        var outputFileName = FilenameUtils.removeExtension(inputFleName) + ".csv";

        String source = Files.readString(Paths.get(inputFleName));
        LinkedHashMap<String, MedEntryReduced> parsed = medEntryParser.parseReduced(source);

        CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(new File(outputFileName)),
                CSVFormat.DEFAULT.withDelimiter(';'));

        for (MedEntryReduced m : parsed.values()) {
            csvPrinter.printRecord(m.getId(), m.getTitle(), m.getText());
        }

        csvPrinter.flush();
        csvPrinter.close();
    }

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplicationBuilder()
                .web(WebApplicationType.NONE)
                .sources(FormatterApp.class)
                .build();

        springApplication.run(args);
    }
}
