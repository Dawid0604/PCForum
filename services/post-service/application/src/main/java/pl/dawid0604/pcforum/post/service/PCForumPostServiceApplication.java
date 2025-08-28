package pl.dawid0604.pcforum.post.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application runtime class.
 */
@SpringBootApplication
final class PCForumPostServiceApplication {

    /**
     * Creating class instance is prohibited.
     */
    private PCForumPostServiceApplication() { }

    /**
     * Simple main method.
     * @param args arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(PCForumPostServiceApplication.class, args);
    }
}
