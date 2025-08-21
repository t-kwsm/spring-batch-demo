package com.example.batch.tasklet;

import com.example.batch.dto.CsvEmployee;
import com.example.batch.dto.CsvProduct;
import com.example.batch.dto.CsvSales;
import com.example.batch.entity.Employee;
import com.example.batch.entity.Product;
import com.example.batch.entity.Sales;
import com.example.batch.mapper.EmployeeMapper;
import com.example.batch.mapper.ProductMapper;
import com.example.batch.mapper.SalesMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;

/**
 * CSV→DBタスクレット基底クラス
 * タスクレットモデルを使用してCSVファイルを一括読み込みしてDBに登録
 */
@Slf4j
public abstract class CsvToDbTasklet<T, E> implements Tasklet {
    
    /**
     * CSVファイルを読み込んでエンティティに変換し、DBに登録
     * 
     * @param contribution ステップの寄与情報
     * @param chunkContext チャンクコンテキスト
     * @return 処理結果
     * @throws Exception 処理エラー時の例外
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String filePath = (String) chunkContext.getStepContext()
                .getJobParameters().get("input.file.path");
        
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("input.file.path parameter is required");
        }
        
        log.info("Starting CSV import from: {}", filePath);
        
        try (Reader reader = new FileReader(filePath)) {
            // CSVファイルを読み込み
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType(getCsvType())
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            
            List<T> csvRecords = csvToBean.parse();
            log.info("Read {} records from CSV", csvRecords.size());
            
            int processedCount = 0;
            int errorCount = 0;
            
            // 各レコードを処理
            for (T csvRecord : csvRecords) {
                try {
                    E entity = convertToEntity(csvRecord);
                    if (entity != null) {
                        saveEntity(entity);
                        processedCount++;
                    }
                } catch (Exception e) {
                    log.error("Error processing record: {}", csvRecord, e);
                    errorCount++;
                }
            }
            
            log.info("CSV import completed. Processed: {}, Errors: {}", processedCount, errorCount);
        }
        
        return RepeatStatus.FINISHED;
    }
    
    /**
     * CSV DTOクラスを取得
     * 
     * @return CSV DTOクラス
     */
    protected abstract Class<T> getCsvType();
    
    /**
     * CSV DTOをエンティティに変換
     * 
     * @param csvRecord CSV DTO
     * @return エンティティ
     */
    protected abstract E convertToEntity(T csvRecord);
    
    /**
     * エンティティをDBに保存
     * 
     * @param entity エンティティ
     */
    protected abstract void saveEntity(E entity);
}

