package pl.dawid0604.pcforum.gateway.service.configuration;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import static lombok.AccessLevel.PACKAGE;

@EnableAsync
@Configuration
@EnableScheduling
@SuppressWarnings("unused")
@NoArgsConstructor(access = PACKAGE)
class SchedulerConfig { }
