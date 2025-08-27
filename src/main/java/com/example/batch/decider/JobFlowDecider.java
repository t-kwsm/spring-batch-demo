package com.example.batch.decider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobFlowDecider implements JobExecutionDecider {
    
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        // ステップの実行結果から処理を分岐
        if (stepExecution != null) {
            long readCount = stepExecution.getReadCount();
            long skipCount = stepExecution.getSkipCount();
            
            // スキップ率が高い場合はエラー処理へ
            if (skipCount > 0 && (double) skipCount / readCount > 0.1) {
                log.warn("スキップ率が10%を超えています。エラー処理フローへ移行します。");
                return new FlowExecutionStatus("HIGH_SKIP_RATE");
            }
            
            // 読み込み件数が0の場合
            if (readCount == 0) {
                log.info("処理対象データがありません。");
                return new FlowExecutionStatus("NO_DATA");
            }
            
            // 処理件数による分岐
            if (readCount > 10000) {
                log.info("大量データ処理フローへ移行します。件数: {}", readCount);
                return new FlowExecutionStatus("LARGE_DATA");
            }
        }
        
        // 通常処理を継続
        return FlowExecutionStatus.COMPLETED;
    }
}