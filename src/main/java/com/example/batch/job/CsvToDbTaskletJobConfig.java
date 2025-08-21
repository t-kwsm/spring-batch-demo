package com.example.batch.job;

import com.example.batch.listener.JobCompletionListener;
import com.example.batch.tasklet.*;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * CSV→DB（タスクレット）ジョブ設定クラス
 * タスクレットモデルを使用してCSVファイルからデータベースへデータを登録
 */
@Configuration
@RequiredArgsConstructor
public class CsvToDbTaskletJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobCompletionListener listener;
    
    @Autowired
    private EmployeeCsvToDbTasklet employeeCsvToDbTasklet;
    
    @Autowired
    private ProductCsvToDbTasklet productCsvToDbTasklet;
    
    @Autowired
    private SalesCsvToDbTasklet salesCsvToDbTasklet;
    
    /**
     * 従業員CSV→DBジョブ（タスクレット）
     */
    @Bean
    public Job employeeCsvToDbTaskletJob() {
        return new JobBuilder("employeeCsvToDbTaskletJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(employeeCsvToDbTaskletStep())
                .build();
    }
    
    /**
     * 従業員CSV→DBステップ（タスクレット）
     */
    @Bean
    public Step employeeCsvToDbTaskletStep() {
        return new StepBuilder("employeeCsvToDbTaskletStep", jobRepository)
                .tasklet(employeeCsvToDbTasklet, transactionManager)
                .build();
    }
    
    /**
     * 商品CSV→DBジョブ（タスクレット）
     */
    @Bean
    public Job productCsvToDbTaskletJob() {
        return new JobBuilder("productCsvToDbTaskletJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(productCsvToDbTaskletStep())
                .build();
    }
    
    /**
     * 商品CSV→DBステップ（タスクレット）
     */
    @Bean
    public Step productCsvToDbTaskletStep() {
        return new StepBuilder("productCsvToDbTaskletStep", jobRepository)
                .tasklet(productCsvToDbTasklet, transactionManager)
                .build();
    }
    
    /**
     * 売上CSV→DBジョブ（タスクレット）
     */
    @Bean
    public Job salesCsvToDbTaskletJob() {
        return new JobBuilder("salesCsvToDbTaskletJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(salesCsvToDbTaskletStep())
                .build();
    }
    
    /**
     * 売上CSV→DBステップ（タスクレット）
     */
    @Bean
    public Step salesCsvToDbTaskletStep() {
        return new StepBuilder("salesCsvToDbTaskletStep", jobRepository)
                .tasklet(salesCsvToDbTasklet, transactionManager)
                .build();
    }
}