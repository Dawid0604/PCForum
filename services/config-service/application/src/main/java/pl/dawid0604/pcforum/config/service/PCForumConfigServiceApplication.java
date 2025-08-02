package pl.dawid0604.pcforum.config.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Application runtime.
 */
@EnableConfigServer
@SpringBootApplication
final class PCForumConfigServiceApplication {

	/**
	 * Creating class instance is prohibited.
	 */
	private PCForumConfigServiceApplication() { }

	/**
	 * Simple main method.
	 * @param args arguments
	 */
	public static void main(final String[] args) {
		SpringApplication.run(PCForumConfigServiceApplication.class, args);
	}

}
