package pl.dawid0604.pcforum.thread.service.configuration;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static lombok.AccessLevel.PACKAGE;

@Configuration
@EnableScheduling
@NoArgsConstructor(access = PACKAGE)
class SchedulerConfig { }
