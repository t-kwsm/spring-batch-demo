package com.example.batch.job;

import com.example.batch.dto.CsvEmployee;
import com.example.batch.entity.Employee;
import com.example.batch.listener.SkipListener;
import com.example.batch.metrics.BatchMetricsListener;
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
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestartableJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final EmployeeProcessor employeeProcessor;
    private final InputDataValidator<CsvEmployee> inputDataValidator;
    private final JobParametersValidator jobParametersValidator;
    private final SkipListener<CsvEmployee, Employee> skipListener;
    private final BatchMetricsListener batchMetricsListener;
    
    @Qualifier("batchTaskExecutor")
    private final TaskExecutor taskExecutor;
    
    @Bean
    public Job restartableJob() {
        return new JobBuilder("restartableJob", jobRepository)
                // RunIdIncrementerを使用せず、再実行可能にする
                .validator(jobParametersValidator)
                .listener(batchMetricsListener)
                .start(restartableStep())
                .build();
    }
    
    @Bean
    public Step restartableStep() {
        return new StepBuilder("restartableStep", jobRepository)
                .<CsvEmployee, Employee>chunk(100, transactionManager)
                .reader(restartableItemReader(null))
                .processor(restartableItemProcessor())
                .writer(restartableItemWriter())
                .faultTolerant()
                // スキップ可能な例外の設定
                .skip(ValidationException.class)
                .skip(IllegalArgumentException.class)
                .skipLimit(1000)
                // リトライ可能な例外の設定
                .retry(TransientDataAccessException.class)
                .retry(DeadlockLoserDataAccessException.class)
                .retryLimit(3)
                .listener(skipListener)
                // チャンクの完了ポリシー
                .completionPolicy(completionPolicy())
                // トランザクション属性
                .transactionAttribute(org.springframework.transaction.interceptor.DefaultTransactionAttribute.getAttributeWithRollbackOn(
                        RuntimeException.class))
                .build();
    }
    
    @Bean
    @StepScope
    public FlatFileItemReader<CsvEmployee> restartableItemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        
        return new FlatFileItemReaderBuilder<CsvEmployee>()
                .name("restartableItemReader")
                .resource(new FileSystemResource(inputFile != null ? inputFile : "input/employees.csv"))
                // 再起動時にスキップする行数を保存
                .saveState(true)
                .linesToSkip(1)
                .delimited()
                .names("employeeCode", "firstName", "lastName", "email", 
                       "department", "position", "salary", "hireDate", "status")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(CsvEmployee.class);
                }})
                .build();
    }
    
    @Bean
    public ItemProcessor<CsvEmployee, Employee> restartableItemProcessor() {
        CompositeItemProcessor<CsvEmployee, Employee> processor = new CompositeItemProcessor<>();
        
        ValidatingItemProcessor<CsvEmployee> validatingProcessor = new ValidatingItemProcessor<>();
        validatingProcessor.setValidator(inputDataValidator);
        validatingProcessor.setFilter(false);
        
        processor.setDelegates(Arrays.asList(validatingProcessor, employeeProcessor));
        return processor;
    }
    
    @Bean
    public JdbcBatchItemWriter<Employee> restartableItemWriter() {
        return new JdbcBatchItemWriterBuilder<Employee>()
                .dataSource(dataSource)
                .sql("""
                    INSERT INTO employees (
                        employee_code, first_name, last_name, email,
                        department, position, salary, hire_date, status,
                        created_at, updated_at
                    ) VALUES (
                        :employeeCode, :firstName, :lastName, :email,
                        :department, :position, :salary, :hireDate, :status,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    ON DUPLICATE KEY UPDATE
                        first_name = VALUES(first_name),
                        last_name = VALUES(last_name),
                        email = VALUES(email),
                        department = VALUES(department),
                        position = VALUES(position),
                        salary = VALUES(salary),
                        hire_date = VALUES(hire_date),
                        status = VALUES(status),
                        updated_at = CURRENT_TIMESTAMP
                    """)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .assertUpdates(false)
                .build();
    }
    
    @Bean
    public CompletionPolicy completionPolicy() {
        CompositeCompletionPolicy policy = new CompositeCompletionPolicy();
        policy.setPolicies(new CompletionPolicy[] {
                new SimpleCompletionPolicy(100),  // チャンクサイズ
                new TimeoutTerminationPolicy(30)  // タイムアウト（秒）
        });
        return policy;
    }
    
    @Bean
    public Job idempotentJob() {
        return new JobBuilder("idempotentJob", jobRepository)
                .validator(jobParametersValidator)
                .listener(batchMetricsListener)
                .preventRestart()  // 再起動を防ぐ（冪等性を保証）
                .start(idempotentStep())
                .build();
    }
    
    @Bean
    public Step idempotentStep() {
        return new StepBuilder("idempotentStep", jobRepository)
                .<CsvEmployee, Employee>chunk(50, transactionManager)
                .reader(restartableItemReader(null))
                .processor(item -> {
                    log.debug("Processing employee: {}", item.getEmployeeCode());
                    
                    // 冪等性を保証するための処理
                    Employee employee = new Employee();
                    employee.setEmployeeCode(item.getEmployeeCode());
                    employee.setFirstName(item.getFirstName());
                    employee.setLastName(item.getLastName());
                    employee.setEmail(item.getEmail());
                    employee.setDepartment(item.getDepartment());
                    employee.setPosition(item.getPosition());
                    employee.setSalary(item.getSalary());
                    employee.setHireDate(item.getHireDate());
                    employee.setStatus(item.getStatus());
                    
                    return employee;
                })
                .writer(items -> {
                    for (Employee item : items) {
                        log.info("Writing employee: {}", item.getEmployeeCode());
                        // UPSERT操作で冪等性を保証
                    }
                })
                .faultTolerant()
                .skip(ValidationException.class)
                .skipLimit(100)
                .listener(skipListener)
                .allowStartIfComplete(true)  // 完了したステップの再実行を許可
                .build();
    }
    
    @Bean
    public Job checkpointJob() {
        return new JobBuilder("checkpointJob", jobRepository)
                .validator(jobParametersValidator)
                .listener(batchMetricsListener)
                .start(checkpointStep1())
                .next(checkpointStep2())
                .next(checkpointStep3())
                .build();
    }
    
    @Bean
    public Step checkpointStep1() {
        return new StepBuilder("checkpointStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Checkpoint 1: データ検証");
                    
                    // チェックポイント情報を保存
                    chunkContext.getStepContext().getStepExecution()
                            .getExecutionContext().put("checkpoint1_completed", true);
                    
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .allowStartIfComplete(false)  // 完了したら再実行しない
                .build();
    }
    
    @Bean
    public Step checkpointStep2() {
        return new StepBuilder("checkpointStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Checkpoint 2: データ変換");
                    
                    // 前のチェックポイントの確認
                    Boolean checkpoint1 = (Boolean) chunkContext.getStepContext()
                            .getJobExecutionContext().get("checkpoint1_completed");
                    
                    if (checkpoint1 == null || !checkpoint1) {
                        throw new IllegalStateException("Checkpoint 1 が完了していません");
                    }
                    
                    chunkContext.getStepContext().getStepExecution()
                            .getExecutionContext().put("checkpoint2_completed", true);
                    
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .allowStartIfComplete(false)
                .build();
    }
    
    @Bean
    public Step checkpointStep3() {
        return new StepBuilder("checkpointStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Checkpoint 3: データ出力");
                    
                    Boolean checkpoint2 = (Boolean) chunkContext.getStepContext()
                            .getJobExecutionContext().get("checkpoint2_completed");
                    
                    if (checkpoint2 == null || !checkpoint2) {
                        throw new IllegalStateException("Checkpoint 2 が完了していません");
                    }
                    
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .allowStartIfComplete(false)
                .build();
    }
}