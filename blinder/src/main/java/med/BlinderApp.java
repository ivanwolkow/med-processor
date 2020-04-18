package med;

import med.common.MedEntry;
import med.service.MedEntryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;


@SpringBootApplication
public class BlinderApp {

    private static final Logger logger = LoggerFactory.getLogger(BlinderApp.class);

    private MedEntryParser medEntryParser;

    public BlinderApp(MedEntryParser medEntryParser) {
        this.medEntryParser = medEntryParser;
    }

    public void run(String... args) throws IOException {

        if (args.length < 2) {
            logger.error("Usage: java -jar blinder.jar input.txt output.txt");
            return;
        }

        var inputFileName = args[0];
        var outputFileName = args[1];

        String input = Files.readString(Paths.get(inputFileName));

        logger.info("Parsing input...");
        var allEntries = medEntryParser.parse(input);

        logger.info("Found {} entries", allEntries.size());
        var result = medEntryParser.saveReduced(allEntries.values());

        File dir = (new File(outputFileName)).getParentFile();

        if (dir != null) {
            dir.mkdirs();
            if (!dir.exists()) {
                logger.error("Failed to create output dir {}", dir);
                return;
            }
        }

        Files.writeString(Paths.get(outputFileName), result, UTF_8, WRITE, TRUNCATE_EXISTING, CREATE);
    }

    public static void main(String[] args) throws IOException {
        SpringApplication springApplication = new SpringApplicationBuilder()
                .web(WebApplicationType.NONE)
                .sources(BlinderApp.class)
                .build();

        ConfigurableApplicationContext applicationContext = springApplication.run(args);
        applicationContext.getBean(BlinderApp.class).run(args);

    }

}
