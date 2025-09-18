package com.reliaquest.api.util;

import io.github.resilience4j.retry.event.RetryOnRetryEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetryEventLogger {

    private final io.github.resilience4j.retry.RetryRegistry retryRegistry;

    @PostConstruct
    public void setupRetryEventLogging() {
        log.info("Setting up retry event logging...");
        retryRegistry.getAllRetries().forEach(retry -> {
            log.info("Registering events for retry: {}", retry.getName());
            retry.getEventPublisher().onRetry(this::handleRetryEvent);
            retry.getEventPublisher()
                    .onSuccess(event -> log.info(
                            "Retry '{}' succeeded after {} attempts",
                            event.getName(),
                            event.getNumberOfRetryAttempts()));
        });
        log.info("Retry event logging setup complete");
    }

    private void handleRetryEvent(RetryOnRetryEvent event) {
        log.warn(
                "Retry attempt #{} for '{}' - waiting {}s before next attempt. Reason: {}",
                event.getNumberOfRetryAttempts(),
                event.getName(),
                event.getWaitInterval().getSeconds(),
                event.getLastThrowable() != null ? event.getLastThrowable().getMessage() : "Unknown error");
    }
}
