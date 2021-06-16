package med;

import med.service.MedEntryParser;
import med.service.ReducedMedEntryParser;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final MedEntryParser medEntryParser;
    private final ReducedMedEntryParser reducedMedEntryParser;

    public BlinderApp() {
        this.medEntryParser = new MedEntryParser();
        this.reducedMedEntryParser = new ReducedMedEntryParser();
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

        var reduced = reducedMedEntryParser.reduceAll(allEntries.values());
        var result = reducedMedEntryParser.joinReduced(reduced);
        Files.writeString(Paths.get(outputFileName), result, UTF_8, WRITE, TRUNCATE_EXISTING, CREATE);
    }

    public static void main(String[] args) throws IOException {
        new BlinderApp().run(args);
    }

}
