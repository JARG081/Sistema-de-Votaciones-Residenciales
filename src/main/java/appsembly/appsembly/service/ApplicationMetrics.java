package appsembly.appsembly.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import appsembly.appsembly.dto.SystemMetricsDTO;

@Component
public class ApplicationMetrics {
    private final AtomicLong voteCount = new AtomicLong();
    private final AtomicLong badRequestCount = new AtomicLong();
    private final AtomicLong conflictCount = new AtomicLong();
    private final AtomicLong unexpectedErrorCount = new AtomicLong();

    public void recordVote() {
        voteCount.incrementAndGet();
    }

    public void recordBadRequest() {
        badRequestCount.incrementAndGet();
    }

    public void recordConflict() {
        conflictCount.incrementAndGet();
    }

    public void recordUnexpectedError() {
        unexpectedErrorCount.incrementAndGet();
    }

    public SystemMetricsDTO snapshot() {
        return new SystemMetricsDTO(voteCount.get(), badRequestCount.get(), conflictCount.get(), unexpectedErrorCount.get());
    }
}