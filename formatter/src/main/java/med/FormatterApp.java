package med;

import med.common.MedEntryReduced;
import med.service.MedEntryParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

/**
 * Converts input txt file with reduced(blinded) entries to CSV format
 */
public class FormatterApp {

    private static final Logger logger = LoggerFactory.getLogger(FormatterApp.class);

    private final MedEntryParser medEntryParser;

    public FormatterApp() {
        this.medEntryParser = new MedEntryParser();
    }

    public void run(String[] args) throws Exception {
        if (args.length < 1) {
            logger.error("Usage: java -jar formatter.jar input_blinded.txt");
        }

        var inputFleName = args[0];
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

    public static void main(String[] args) throws Exception {
        new FormatterApp().run(args);
    }
}
