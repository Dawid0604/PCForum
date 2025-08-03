package pl.dawid0604.pcforum.thread.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application runtime class.
 */
@SpringBootApplication
final class PCForumThreadServiceApplication {

    /**
     * Creating class instance is prohibited.
     */
    private PCForumThreadServiceApplication() { }

    /**
     * Simple main method.
     * @param args arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(PCForumThreadServiceApplication.class, args);
    }
}
