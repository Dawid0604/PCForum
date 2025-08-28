package pl.dawid0604.pcforum.config.service;

import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static lombok.AccessLevel.PACKAGE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@NoArgsConstructor(access = PACKAGE)
class PCForumConfigServiceApplicationTests {

	@Autowired
	@SuppressWarnings("unused")
	private ApplicationContext context;

	@Test
	void contextLoads() {
		assertThat(context).isNotNull();
	}

}
