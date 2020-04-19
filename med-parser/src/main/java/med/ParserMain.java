package med;

import med.processor.MedProcessorSingle;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ParserMain implements CommandLineRunner {

    private final MedProcessorSingle processor;

    public ParserMain(MedProcessorSingle processor) {
        this.processor = processor;
    }

    public void run(String[] args) {
        processor.run(args);
    }

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplicationBuilder()
                .web(WebApplicationType.NONE)
                .sources(ParserMain.class)
                .build();

        springApplication.run(args);
    }

}
