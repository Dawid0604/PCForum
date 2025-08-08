package pl.dawid0604.pcforum.reaction.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application runtime class.
 */
@SpringBootApplication
final class PCForumReactionServiceApplication {

    /**
     * Creating class instance is prohibited.
     */
    private PCForumReactionServiceApplication() { }

    /**
     * Simple main method.
     * @param args arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(PCForumReactionServiceApplication.class, args);
    }
}
