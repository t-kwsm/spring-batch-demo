package com.example.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * ジョブ完了リスナー
 * ジョブの開始と終了時にログを出力
 */
@Slf4j
@Component
public class JobCompletionListener implements JobExecutionListener {
    
    /**
     * ジョブ開始前の処理
     * 
     * @param jobExecution ジョブ実行情報
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("====================================");
        log.info("Job Started: {}", jobExecution.getJobInstance().getJobName());
        log.info("Job Parameters: {}", jobExecution.getJobParameters());
        log.info("====================================");
    }
    
    /**
     * ジョブ終了後の処理
     * 
     * @param jobExecution ジョブ実行情報
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("====================================");
        log.info("Job Finished: {}", jobExecution.getJobInstance().getJobName());
        log.info("Job Status: {}", jobExecution.getStatus());
        log.info("Exit Status: {}", jobExecution.getExitStatus());
        log.info("Start Time: {}", jobExecution.getStartTime());
        log.info("End Time: {}", jobExecution.getEndTime());
        
        if (jobExecution.getAllFailureExceptions().size() > 0) {
            log.error("Job Errors:");
            jobExecution.getAllFailureExceptions().forEach(throwable -> 
                log.error("Error: ", throwable));
        }
        
        log.info("====================================");
    }
}