package med.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class MedEntry {
    private final String id;
    private final String publisher;
    private final String title;
    private final String authorsAndCollaborators;
    private final List<String> affiliations;
    private final String text;
    private final String all;

}
