package com.example.batch.job;

import com.example.batch.decider.JobFlowDecider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConditionalFlowJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobFlowDecider jobFlowDecider;
    
    @Bean
    public Job conditionalFlowJob() {
        return new JobBuilder("conditionalFlowJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(validationStep())
                .next(jobFlowDecider)
                .on("HIGH_SKIP_RATE").to(errorHandlingStep())
                .from(jobFlowDecider).on("NO_DATA").to(noDataStep())
                .from(jobFlowDecider).on("LARGE_DATA").to(largeDataProcessingStep())
                .from(jobFlowDecider).on("COMPLETED").to(normalProcessingStep())
                .end()
                .build();
    }
    
    @Bean
    public Step validationStep() {
        return new StepBuilder("validationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("データ検証ステップを実行中...");
                    
                    // 入力パラメータの検証
                    String inputFile = chunkContext.getStepContext()
                            .getJobParameters().getString("inputFile");
                    
                    if (inputFile == null || inputFile.isEmpty()) {
                        contribution.setExitStatus(ExitStatus.FAILED);
                        throw new IllegalArgumentException("入力ファイルが指定されていません");
                    }
                    
                    log.info("データ検証が完了しました");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Step normalProcessingStep() {
        return new StepBuilder("normalProcessingStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("通常処理ステップを実行中...");
                    // 通常のデータ処理ロジック
                    Thread.sleep(1000);
                    log.info("通常処理が完了しました");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Step largeDataProcessingStep() {
        return new StepBuilder("largeDataProcessingStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("大量データ処理ステップを実行中...");
                    // パーティション処理や並列処理の設定
                    Thread.sleep(2000);
                    log.info("大量データ処理が完了しました");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Step errorHandlingStep() {
        return new StepBuilder("errorHandlingStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.error("エラー処理ステップを実行中...");
                    // エラーレコードの記録、通知処理など
                    log.error("エラー処理が完了しました。管理者に通知しました");
                    contribution.setExitStatus(ExitStatus.FAILED);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Step noDataStep() {
        return new StepBuilder("noDataStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("データなし処理ステップを実行中...");
                    log.info("処理対象データがないため、ジョブを終了します");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}