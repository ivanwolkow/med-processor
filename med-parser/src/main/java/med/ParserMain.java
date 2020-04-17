package med;

import med.processor.Processor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ParserMain {

    private static final Logger logger = LoggerFactory.getLogger(ParserMain.class);

    private List<Processor> processors;

    public ParserMain(List<Processor> processors) {
        this.processors = processors;
    }

    public void run() {
        String processorName = ObjectUtils.firstNonNull(
                System.getProperty("processor"),
                System.getenv("PROCESSOR"),
                StringUtils.EMPTY);

        logger.info("Processors={}", processorName);

        for (Processor p : processors) {
            if (p.getClass().getSimpleName().equals(processorName)) {
                logger.info("Running processor {}", processorName);

                long l = System.currentTimeMillis();
                try {
                    p.run();
                } catch (Exception e) {
                    throw new RuntimeException("Processor failed", e);
                } finally {
                    long took = TimeUnit.of(ChronoUnit.MILLIS).toSeconds(System.currentTimeMillis() - l);
                    logger.info("Processor finished in {} seconds", took);
                }
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplicationBuilder()
                .web(WebApplicationType.NONE)
                .sources(ParserMain.class)
                .build();

        ConfigurableApplicationContext applicationContext = springApplication.run(args);

        applicationContext.getBean(ParserMain.class).run();
    }

}
