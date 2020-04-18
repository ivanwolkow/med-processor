package med.controller;

import med.common.MedEntry;
import med.entity.BaseResponse;
import med.entity.MedEntryRequest;
import med.entity.MedEntryParserResponse;
import med.service.MedEntryParser;
import one.util.streamex.EntryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Random;

@RestController
public class BasicController {

    private static final Logger logger = LoggerFactory.getLogger(BasicController.class);

    private MedEntryParser medEntryParser;
    private Random random;

    public BasicController(MedEntryParser medEntryParser) {
        this.medEntryParser = medEntryParser;
        random = new Random(1);
    }

    @GetMapping(value = "/")
    public BaseResponse hello() {
        return new BaseResponse(random.nextInt(100), "Hi there");
    }

    @PostMapping(value = "/parse")
    public Collection<MedEntryParserResponse> parse(@RequestParam MultipartFile file) throws IOException {
        if (file == null) {
            logger.warn("No file uploaded");
            return Collections.emptyList();
        }

        var source = new String(file.getBytes());
        LinkedHashMap<String, MedEntry> result = medEntryParser.parse(source);
        return EntryStream.of(result)
                .mapValues(BasicController::map)
                .values()
                .toList();
    }

    private static MedEntryParserResponse map(MedEntry entry) {
        return new MedEntryParserResponse(
                entry.getId(),
                entry.getPublisher(),
                entry.getTitle(),
                entry.getAuthorsAndCollaborators(),
                entry.getAffiliations(),
                entry.getText());
    }
}

