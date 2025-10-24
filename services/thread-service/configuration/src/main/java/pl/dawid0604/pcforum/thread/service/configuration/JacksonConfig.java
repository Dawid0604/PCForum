package pl.dawid0604.pcforum.thread.service.configuration;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static lombok.AccessLevel.PACKAGE;

@Configuration
@NoArgsConstructor(access = PACKAGE)
class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(final Jackson2ObjectMapperBuilder builder) {
        return builder.featuresToDisable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                      .build();
    }
}
