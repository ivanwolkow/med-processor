package med;

import med.service.MedEntryParser;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;

/**
 * Cuts off the authors, collaborators and affiliations, thus producing the "reduced" (or "blinded") publication list
 */
public class BlinderApp {

    private static final Logger logger = LoggerFactory.getLogger(BlinderApp.class);

    private MedEntryParser medEntryParser;

    public BlinderApp() {
        this.medEntryParser = new MedEntryParser();
    }

    public void run(String[] args) throws IOException {

        if (args.length < 1) {
            logger.error("Usage: java -jar blinder.jar input.txt");
            return;
        }

        var inputFileName = args[0];
        String input = Files.readString(Paths.get(inputFileName));

        logger.info("Parsing input...");
        var allEntries = medEntryParser.parse(input);
        logger.info("Found {} entries", allEntries.size());

        var outputFileName = FilenameUtils.removeExtension(inputFileName) + "_blinded.txt";
        File outputDir = new File(outputFileName).getParentFile();

        if (outputDir != null) {
            outputDir.mkdirs();
            if (!outputDir.exists()) {
                logger.error("Failed to create output dir {}", outputDir);
                return;
            }
        }

        var reduced = medEntryParser.reduceAll(allEntries.values());
        var result = medEntryParser.printReduced(reduced);
        Files.writeString(Paths.get(outputFileName), result, UTF_8, WRITE, TRUNCATE_EXISTING, CREATE);
    }

    public static void main(String[] args) throws IOException {
        new BlinderApp().run(args);
    }

}
