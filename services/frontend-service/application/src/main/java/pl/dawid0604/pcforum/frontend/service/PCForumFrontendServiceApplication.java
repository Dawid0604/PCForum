package pl.dawid0604.pcforum.frontend.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application runtime class.
 */
@SpringBootApplication
final class PCForumFrontendServiceApplication {

    /**
     * Creating class instance is prohibited.
     */
    private PCForumFrontendServiceApplication() { }

    /**
     * Simple main method.
     * @param args arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(PCForumFrontendServiceApplication.class, args);
    }
}
