package pl.dawid0604.pcforum.category.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application runtime class.
 */
@SpringBootApplication
final class PCForumCategoryServiceApplication {

    /**
     * Creating class instance is prohibited.
     */
    private PCForumCategoryServiceApplication() { }

    /**
     * Simple main method.
     * @param args arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(PCForumCategoryServiceApplication.class, args);
    }
}
