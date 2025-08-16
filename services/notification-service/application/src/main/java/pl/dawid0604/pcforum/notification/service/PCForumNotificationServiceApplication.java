package pl.dawid0604.pcforum.notification.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application runtime class.
 */
@SpringBootApplication
final class PCForumNotificationServiceApplication {

    /**
     * Creating class instance is prohibited.
     */
    private PCForumNotificationServiceApplication() { }

    /**
     * Simple main method.
     * @param args arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(PCForumNotificationServiceApplication.class, args);
    }
}
