package med.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MedEntryReduced {
    private final String id;
    private final String publisher;
    private final String title;
    private final String text;

}
