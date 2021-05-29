package med.constants;

import java.util.regex.Pattern;

public class DocumentStyleConstants {

    public static final String SPLIT_TO_ENTRIES_PATTERN = "[\\n[\\r\\n]]{3}(?=\\d+\\. )";

    public static final String FIELD_SEPARATOR = "\n\n";
    public static final String ENTRY_SEPARATOR = "\n\n\n";

    public static final Pattern ENTRY_PATTERN = Pattern.compile(".*?(\\d+?)\\. (.+?)\\R\\R(.+?)\\R\\R(.+?)Author information:.*?\\R(.+?)\\R\\R(.+)", Pattern.DOTALL);
    public static final Pattern REDUCED_ENTRY_PATTERN = Pattern.compile(".*?(\\d+?)\\. (.+?)\\R\\R(.+?)\\R\\R(.+)", Pattern.DOTALL);


}
