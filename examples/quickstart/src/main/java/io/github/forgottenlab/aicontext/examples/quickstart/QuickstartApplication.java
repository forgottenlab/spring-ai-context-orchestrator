package io.github.forgottenlab.aicontext.examples.quickstart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class QuickstartApplication {

    public static void main(String[] args) {
        SpringApplication application =
                new SpringApplication(QuickstartApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);

        try (ConfigurableApplicationContext ignored =
                     application.run(args)) {
            // The demo runs through QuickstartDemoRunner during startup.
        }
    }
}
