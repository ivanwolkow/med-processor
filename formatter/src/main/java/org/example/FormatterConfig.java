package org.example;

import med.service.MedEntryParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FormatterConfig {

    @Bean
    public MedEntryParser medEntryParser() {
        return new MedEntryParser();
    }
}
