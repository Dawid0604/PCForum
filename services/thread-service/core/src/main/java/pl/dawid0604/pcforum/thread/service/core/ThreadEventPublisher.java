package pl.dawid0604.pcforum.thread.service.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import pl.dawid0604.pcforum.thread.service.commons.Constants;

import java.util.concurrent.CompletableFuture;

import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Component
@RequiredArgsConstructor(access = PACKAGE)
class ThreadEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishThreadStatusChangedEvent(final ThreadStatusChangedEvent event) {
        publishEvent(event, event.getThreadId(), Constants.KAFKA_THREAD_STATUS_CHANGED_TOPIC);
    }

    public void publishThreadCreatedEvent(final ThreadCreatedEvent event) {
        publishEvent(event, event.getCategoryId(), Constants.KAFKA_THREAD_CREATED_TOPIC);
    }

    private void publishEvent(final ThreadEvent event, final String key, final String topic) {
        final CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if(ex == null) {
                log.info(
                        "Successfully published event: eventId={}, topic={}, partition={}, offset={}",
                        event.getEventId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );

            } else {
                log.error(
                        "Failed to publish event: eventId={}, threadId={}, error={}",
                        event.getEventId(),
                        event.getThreadId(),
                        ex.getMessage(),
                        ex
                );
            }
        });
    }
}
