package pl.dawid0604.pcforum.gateway.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application runtime class.
 */
@SpringBootApplication
final class PCForumGatewayServiceApplication {

    /**
     * Creating class instance is prohibited.
     */
    private PCForumGatewayServiceApplication() { }

    /**
     * Simple main method.
     * @param args arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(PCForumGatewayServiceApplication.class, args);
    }
}
