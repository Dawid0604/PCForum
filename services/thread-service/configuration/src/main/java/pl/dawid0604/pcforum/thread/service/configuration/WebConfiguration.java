package pl.dawid0604.pcforum.thread.service.configuration;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static lombok.AccessLevel.PACKAGE;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@SuppressWarnings("unused")
@NoArgsConstructor(access = PACKAGE)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
class WebConfiguration { }
