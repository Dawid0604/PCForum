package pl.dawid0604.pcforum.image.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application runtime class.
 */
@SpringBootApplication
final class PCForumImageServiceApplication {

    /**
     * Creating class instance is prohibited.
     */
    private PCForumImageServiceApplication() { }

    /**
     * Simple main method.
     * @param args arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(PCForumImageServiceApplication.class, args);
    }
}
