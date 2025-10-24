package pl.dawid0604.pcforum.thread.service.configuration;

import lombok.NoArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import pl.dawid0604.pcforum.thread.service.commons.Constants;

import static lombok.AccessLevel.PACKAGE;

@Configuration
@NoArgsConstructor(access = PACKAGE)
class KafkaTopicConfig {

    @Bean
    public NewTopic threadCreatedTopic(

            @Value("${kafka.threadCreatedTopic.partitions:3}")
            final int partitions,

            @Value("${kafka.threadCreatedTopic.replicas:1}")
            final int replicas,

            @Value("${kafka.threadCreatedTopic.retentionMs:604800000}")
            final long retentionMs) {

        return TopicBuilder.name(Constants.KAFKA_THREAD_CREATED_TOPIC)
                           .partitions(partitions)
                           .replicas(replicas)
                           .config("retention.ms", String.valueOf(retentionMs))
                           .config("cleanup.policy", "delete")
                           .build();
    }

    @Bean
    public NewTopic threadStatusChangedTopic(

            @Value("${kafka.threadCreatedTopic.partitions:3}")
            final int partitions,

            @Value("${kafka.threadCreatedTopic.replicas:1}")
            final int replicas,

            @Value("${kafka.threadCreatedTopic.retentionMs:2592000000}")
            final long retentionMs) {

        return TopicBuilder.name(Constants.KAFKA_THREAD_STATUS_CHANGED_TOPIC)
                           .partitions(partitions)
                           .replicas(replicas)
                           .config("retention.ms", String.valueOf(retentionMs))
                           .config("cleanup.policy", "delete")
                           .build();
    }
}
