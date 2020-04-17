package med;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BlinderWebApp {

    private static final Logger logger = LoggerFactory.getLogger(BlinderWebApp.class);

    public void run() {
        logger.info("Spring boot application is available!");
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BlinderWebApp.class, args);
        BlinderWebApp bean = context.getBean(BlinderWebApp.class);
        bean.run();
    }
}
