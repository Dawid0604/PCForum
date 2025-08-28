package pl.dawid0604.pcforum.discovery.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Application runtime.
 */
@EnableEurekaServer
@SpringBootApplication
final class PCForumDiscoveryServiceApplication {

	/**
	 * Creating class instance is prohibited.
	 */
	private PCForumDiscoveryServiceApplication() { }

	/**
	 * Simple main method.
	 * @param args arguments
	 */
	public static void main(final String[] args) {
		SpringApplication.run(PCForumDiscoveryServiceApplication.class, args);
	}

}
