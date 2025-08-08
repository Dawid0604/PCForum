package pl.dawid0604.pcforum.message.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application runtime class.
 */
@SpringBootApplication
final class PCForumMessageServiceApplication {

    /**
     * Creating class instance is prohibited.
     */
    private PCForumMessageServiceApplication() { }

    /**
     * Simple main method.
     * @param args arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(PCForumMessageServiceApplication.class, args);
    }
}
