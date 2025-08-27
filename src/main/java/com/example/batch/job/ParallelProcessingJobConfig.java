package com.example.batch.job;

import com.example.batch.dto.CsvEmployee;
import com.example.batch.entity.Employee;
import com.example.batch.listener.SkipListener;
import com.example.batch.partitioner.RangePartitioner;
import com.example.batch.processor.EmployeeProcessor;
import com.example.batch.validator.InputDataValidator;
import com.example.batch.validator.JobParametersValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.H2PagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ParallelProcessingJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final RangePartitioner rangePartitioner;
    private final EmployeeProcessor employeeProcessor;
    private final InputDataValidator<CsvEmployee> inputDataValidator;
    private final JobParametersValidator jobParametersValidator;
    private final SkipListener<CsvEmployee, Employee> skipListener;
    
    @Qualifier("batchTaskExecutor")
    private final TaskExecutor batchTaskExecutor;
    
    @Qualifier("partitionTaskExecutor")
    private final TaskExecutor partitionTaskExecutor;
    
    @Bean
    public Job parallelProcessingJob() {
        return new JobBuilder("parallelProcessingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .validator(jobParametersValidator)
                .start(partitionStep())
                .build();
    }
    
    @Bean
    public Step partitionStep() {
        return new StepBuilder("partitionMasterStep", jobRepository)
                .partitioner("partitionWorkerStep", rangePartitioner)
                .partitionHandler(partitionHandler())
                .build();
    }
    
    @Bean
    public PartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(partitionTaskExecutor);
        handler.setStep(partitionWorkerStep());
        handler.setGridSize(5); // パーティション数
        return handler;
    }
    
    @Bean
    public Step partitionWorkerStep() {
        return new StepBuilder("partitionWorkerStep", jobRepository)
                .<Employee, Employee>chunk(100, transactionManager)
                .reader(partitionItemReader(null, null))
                .processor(item -> {
                    log.debug("Processing employee: {}", item.getEmployeeCode());
                    // ビジネスロジックの処理
                    Thread.sleep(10); // 処理のシミュレーション
                    return item;
                })
                .writer(partitionItemWriter())
                .faultTolerant()
                .skip(ValidationException.class)
                .skipLimit(100)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .taskExecutor(batchTaskExecutor)
                .throttleLimit(4)
                .build();
    }
    
    @Bean
    @StepScope
    public JdbcPagingItemReader<Employee> partitionItemReader(
            @Value("#{stepExecutionContext['minValue']}") Integer minValue,
            @Value("#{stepExecutionContext['maxValue']}") Integer maxValue) {
        
        log.info("リーダー作成: 範囲 [{} - {}]", minValue, maxValue);
        
        JdbcPagingItemReader<Employee> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(100);
        reader.setRowMapper(new BeanPropertyRowMapper<>(Employee.class));
        
        H2PagingQueryProvider queryProvider = new H2PagingQueryProvider();
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM employees");
        queryProvider.setWhereClause("WHERE id BETWEEN :minValue AND :maxValue");
        
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        
        reader.setQueryProvider(queryProvider);
        
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("minValue", minValue);
        parameterValues.put("maxValue", maxValue);
        reader.setParameterValues(parameterValues);
        
        return reader;
    }
    
    @Bean
    public JdbcBatchItemWriter<Employee> partitionItemWriter() {
        JdbcBatchItemWriter<Employee> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("""
            UPDATE employees 
            SET status = :status, 
                updated_at = CURRENT_TIMESTAMP 
            WHERE id = :id
            """);
        writer.setDataSource(dataSource);
        return writer;
    }
    
    @Bean
    public Job multiThreadedJob() {
        return new JobBuilder("multiThreadedJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .validator(jobParametersValidator)
                .start(multiThreadedStep())
                .build();
    }
    
    @Bean
    public Step multiThreadedStep() {
        return new StepBuilder("multiThreadedStep", jobRepository)
                .<CsvEmployee, Employee>chunk(50, transactionManager)
                .reader(csvItemReader(null))
                .processor(employeeProcessor)
                .writer(items -> {
                    items.forEach(item -> 
                        log.info("Writing employee: {}", item.getEmployeeCode())
                    );
                })
                .faultTolerant()
                .skip(ValidationException.class)
                .skipLimit(100)
                .listener(skipListener)
                .taskExecutor(batchTaskExecutor)
                .throttleLimit(4) // 同時実行スレッド数の制限
                .build();
    }
    
    @Bean
    @StepScope
    public FlatFileItemReader<CsvEmployee> csvItemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        
        return new FlatFileItemReaderBuilder<CsvEmployee>()
                .name("csvItemReader")
                .resource(new FileSystemResource(inputFile != null ? inputFile : "input/employees.csv"))
                .delimited()
                .names("employeeCode", "firstName", "lastName", "email", 
                       "department", "position", "salary", "hireDate", "status")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(CsvEmployee.class);
                }})
                .build();
    }
    
    @Bean
    public Job asyncJob() {
        return new JobBuilder("asyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(asyncStep1())
                .split(batchTaskExecutor)
                .add(asyncFlow())
                .end()
                .build();
    }
    
    private org.springframework.batch.core.job.builder.FlowBuilder<org.springframework.batch.core.job.flow.Flow> asyncFlow() {
        return new org.springframework.batch.core.job.builder.FlowBuilder<>("asyncFlow")
                .start(asyncStep2())
                .next(asyncStep3());
    }
    
    @Bean
    public Step asyncStep1() {
        return new StepBuilder("asyncStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("非同期ステップ1を実行中...");
                    Thread.sleep(2000);
                    log.info("非同期ステップ1完了");
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Step asyncStep2() {
        return new StepBuilder("asyncStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("非同期ステップ2を実行中...");
                    Thread.sleep(3000);
                    log.info("非同期ステップ2完了");
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Step asyncStep3() {
        return new StepBuilder("asyncStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("非同期ステップ3を実行中...");
                    Thread.sleep(1000);
                    log.info("非同期ステップ3完了");
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}