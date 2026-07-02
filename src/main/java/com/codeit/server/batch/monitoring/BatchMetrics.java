package com.codeit.server.batch.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class BatchMetrics {

    private final MeterRegistry meterRegistry;

    public BatchMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordJobDuration(String jobName, String status, long durationMs) {
        Timer.builder("batch.job.duration")
            .description("배치 잡 실행 소요 시간") // 💡 한글 설명으로 변경
            .tag("job.name", jobName)
            .tag("status", status)
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void incrementJobCount(String jobName, String status) {
        Counter.builder("batch.job.count")
            .description("배치 잡 총 실행 횟수") // 💡 한글 설명으로 변경
            .tag("job.name", jobName)
            .tag("status", status)
            .register(meterRegistry)
            .increment();
    }
}