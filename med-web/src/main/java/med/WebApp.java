package med;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WebApp {

    private static final Logger logger = LoggerFactory.getLogger(WebApp.class);

    public void run() {
        logger.info("Spring boot application is available!");
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WebApp.class, args);
        WebApp bean = context.getBean(WebApp.class);
        bean.run();
    }
}
