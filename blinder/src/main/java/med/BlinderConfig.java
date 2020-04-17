package med;

import med.service.MedEntryParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlinderConfig {

    @Bean
    public MedEntryParser medEntryParser() {
        return new MedEntryParser();
    }
}
