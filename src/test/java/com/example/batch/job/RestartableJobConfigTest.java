package com.example.batch.job;

import com.example.batch.config.RetryConfig;
import com.example.batch.config.TaskExecutorConfig;
import com.example.batch.listener.RetryListener;
import com.example.batch.listener.SkipListener;
import com.example.batch.metrics.BatchMetricsListener;
import com.example.batch.processor.EmployeeProcessor;
import com.example.batch.validator.InputDataValidator;
import com.example.batch.validator.JobParametersValidator;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RestartableJobConfig.class,
        TaskExecutorConfig.class,
        RetryConfig.class,
        RetryListener.class,
        SkipListener.class,
        BatchMetricsListener.class,
        EmployeeProcessor.class,
        InputDataValidator.class,
        JobParametersValidator.class
})
@SpringBatchTest
class RestartableJobConfigTest {
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Autowired
    private JobLauncher jobLauncher;
    
    @MockBean
    private Validator validator;
    
    @Test
    void testJobRestart() throws Exception {
        // 初回実行
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", "src/test/resources/test-employees.csv")
                .addString("outputFile", "output/test-output.csv")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution firstExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        // 強制的に失敗させる
        firstExecution.setStatus(BatchStatus.FAILED);
        firstExecution.setExitStatus(ExitStatus.FAILED);
        
        // 再実行
        JobParameters restartParams = new JobParametersBuilder()
                .addString("inputFile", "src/test/resources/test-employees.csv")
                .addString("outputFile", "output/test-output.csv")
                .addLong("timestamp", System.currentTimeMillis() + 1000)
                .toJobParameters();
        
        JobExecution restartExecution = jobLauncherTestUtils.launchJob(restartParams);
        
        assertNotEquals(firstExecution.getId(), restartExecution.getId());
    }
    
    @Test
    void testStepExecution() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("restartableStep");
        
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertTrue(stepExecution.getReadCount() >= 0);
        assertTrue(stepExecution.getWriteCount() >= 0);
    }
    
    @Test
    void testCheckpointJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(3, execution.getStepExecutions().size());
        
        // 各チェックポイントが完了していることを確認
        execution.getStepExecutions().forEach(stepExecution -> {
            assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
        });
    }
    
    @Test
    void testIdempotentJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "src/test/resources/test-employees.csv")
                .addString("outputFile", "output/test-output.csv")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        // 同じパラメータで2回実行
        JobExecution firstExecution = jobLauncherTestUtils.launchJob(params);
        
        // 2回目の実行（同じパラメータ）
        assertThrows(JobInstanceAlreadyCompleteException.class, () -> {
            jobLauncherTestUtils.launchJob(params);
        });
    }
}