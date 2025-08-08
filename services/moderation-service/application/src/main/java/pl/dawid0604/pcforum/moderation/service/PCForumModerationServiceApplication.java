package pl.dawid0604.pcforum.moderation.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application runtime class.
 */
@SpringBootApplication
final class PCForumModerationServiceApplication {

    /**
     * Creating class instance is prohibited.
     */
    private PCForumModerationServiceApplication() { }

    /**
     * Simple main method.
     * @param args arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(PCForumModerationServiceApplication.class, args);
    }
}
