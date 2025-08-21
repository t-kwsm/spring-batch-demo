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
 * DB→CSV（タスクレット）ジョブ設定クラス
 * タスクレットモデルを使用してデータベースからCSVファイルへデータを出力
 */
@Configuration
@RequiredArgsConstructor
public class DbToCsvTaskletJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobCompletionListener listener;
    
    @Autowired
    private EmployeeDbToCsvTasklet employeeDbToCsvTasklet;
    
    @Autowired
    private ProductDbToCsvTasklet productDbToCsvTasklet;
    
    @Autowired
    private SalesDbToCsvTasklet salesDbToCsvTasklet;
    
    /**
     * 従業員DB→CSVジョブ（タスクレット）
     */
    @Bean
    public Job employeeDbToCsvTaskletJob() {
        return new JobBuilder("employeeDbToCsvTaskletJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(employeeDbToCsvTaskletStep())
                .build();
    }
    
    /**
     * 従業員DB→CSVステップ（タスクレット）
     */
    @Bean
    public Step employeeDbToCsvTaskletStep() {
        return new StepBuilder("employeeDbToCsvTaskletStep", jobRepository)
                .tasklet(employeeDbToCsvTasklet, transactionManager)
                .build();
    }
    
    /**
     * 商品DB→CSVジョブ（タスクレット）
     */
    @Bean
    public Job productDbToCsvTaskletJob() {
        return new JobBuilder("productDbToCsvTaskletJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(productDbToCsvTaskletStep())
                .build();
    }
    
    /**
     * 商品DB→CSVステップ（タスクレット）
     */
    @Bean
    public Step productDbToCsvTaskletStep() {
        return new StepBuilder("productDbToCsvTaskletStep", jobRepository)
                .tasklet(productDbToCsvTasklet, transactionManager)
                .build();
    }
    
    /**
     * 売上DB→CSVジョブ（タスクレット）
     */
    @Bean
    public Job salesDbToCsvTaskletJob() {
        return new JobBuilder("salesDbToCsvTaskletJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(salesDbToCsvTaskletStep())
                .build();
    }
    
    /**
     * 売上DB→CSVステップ（タスクレット）
     */
    @Bean
    public Step salesDbToCsvTaskletStep() {
        return new StepBuilder("salesDbToCsvTaskletStep", jobRepository)
                .tasklet(salesDbToCsvTasklet, transactionManager)
                .build();
    }
}