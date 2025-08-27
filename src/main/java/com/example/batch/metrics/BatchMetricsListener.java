package com.example.batch.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.metrics.BatchMetrics;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BatchMetricsListener implements JobExecutionListener, StepExecutionListener {
    
    private final MeterRegistry meterRegistry;
    private final Counter jobStartedCounter;
    private final Counter jobCompletedCounter;
    private final Counter jobFailedCounter;
    private final Counter stepCompletedCounter;
    private final Counter stepFailedCounter;
    private final Counter itemReadCounter;
    private final Counter itemProcessedCounter;
    private final Counter itemWrittenCounter;
    private final Counter itemSkippedCounter;
    private Timer.Sample jobTimerSample;
    private Timer.Sample stepTimerSample;
    
    public BatchMetricsListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // カウンターの初期化
        this.jobStartedCounter = Counter.builder("batch.job.started")
                .description("Number of jobs started")
                .register(meterRegistry);
                
        this.jobCompletedCounter = Counter.builder("batch.job.completed")
                .description("Number of jobs completed successfully")
                .register(meterRegistry);
                
        this.jobFailedCounter = Counter.builder("batch.job.failed")
                .description("Number of jobs failed")
                .register(meterRegistry);
                
        this.stepCompletedCounter = Counter.builder("batch.step.completed")
                .description("Number of steps completed")
                .register(meterRegistry);
                
        this.stepFailedCounter = Counter.builder("batch.step.failed")
                .description("Number of steps failed")
                .register(meterRegistry);
                
        this.itemReadCounter = Counter.builder("batch.item.read")
                .description("Number of items read")
                .register(meterRegistry);
                
        this.itemProcessedCounter = Counter.builder("batch.item.processed")
                .description("Number of items processed")
                .register(meterRegistry);
                
        this.itemWrittenCounter = Counter.builder("batch.item.written")
                .description("Number of items written")
                .register(meterRegistry);
                
        this.itemSkippedCounter = Counter.builder("batch.item.skipped")
                .description("Number of items skipped")
                .register(meterRegistry);
    }
    
    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobStartedCounter.increment();
        jobTimerSample = Timer.start(meterRegistry);
        
        // ジョブパラメータをタグとして記録
        meterRegistry.gauge("batch.job.active", 
                jobExecution, 
                e -> 1.0);
        
        log.info("Job {} started with parameters: {}", 
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }
    
    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        
        // ジョブ実行時間を記録
        if (jobTimerSample != null) {
            jobTimerSample.stop(Timer.builder("batch.job.duration")
                    .description("Job execution duration")
                    .tag("job.name", jobName)
                    .tag("status", jobExecution.getExitStatus().getExitCode())
                    .register(meterRegistry));
        }
        
        // ステータスに基づいてカウンターを更新
        if (jobExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            jobCompletedCounter.increment();
        } else if (jobExecution.getExitStatus().equals(ExitStatus.FAILED)) {
            jobFailedCounter.increment();
        }
        
        // ジョブ実行結果のメトリクスを記録
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            meterRegistry.gauge("batch.job.read.count",
                    stepExecution.getReadCount());
            meterRegistry.gauge("batch.job.write.count",
                    stepExecution.getWriteCount());
            meterRegistry.gauge("batch.job.skip.count",
                    stepExecution.getSkipCount());
        });
        
        log.info("Job {} finished with status: {}", 
                jobName,
                jobExecution.getExitStatus());
    }
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepTimerSample = Timer.start(meterRegistry);
        
        log.info("Step {} started", stepExecution.getStepName());
    }
    
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        
        // ステップ実行時間を記録
        if (stepTimerSample != null) {
            stepTimerSample.stop(Timer.builder("batch.step.duration")
                    .description("Step execution duration")
                    .tag("job.name", jobName)
                    .tag("step.name", stepName)
                    .tag("status", stepExecution.getExitStatus().getExitCode())
                    .register(meterRegistry));
        }
        
        // ステップメトリクスを更新
        if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            stepCompletedCounter.increment();
        } else if (stepExecution.getExitStatus().equals(ExitStatus.FAILED)) {
            stepFailedCounter.increment();
        }
        
        // アイテム処理メトリクスを記録
        itemReadCounter.increment(stepExecution.getReadCount());
        itemProcessedCounter.increment(stepExecution.getReadCount() - stepExecution.getSkipCount());
        itemWrittenCounter.increment(stepExecution.getWriteCount());
        itemSkippedCounter.increment(stepExecution.getSkipCount());
        
        // スループットを計算して記録
        long duration = Duration.between(
                stepExecution.getStartTime(),
                stepExecution.getEndTime() != null ? stepExecution.getEndTime() : stepExecution.getStartTime()
        ).toMillis();
        
        if (duration > 0) {
            double throughput = (double) stepExecution.getReadCount() / (duration / 1000.0);
            meterRegistry.gauge("batch.step.throughput",
                    throughput);
        }
        
        log.info("Step {} finished - Read: {}, Written: {}, Skipped: {}", 
                stepName,
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());
        
        return stepExecution.getExitStatus();
    }
}