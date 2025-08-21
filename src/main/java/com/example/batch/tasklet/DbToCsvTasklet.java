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
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * DB→CSVタスクレット基底クラス
 * タスクレットモデルを使用してDBから一括読み込みしてCSVファイルに出力
 */
@Slf4j
public abstract class DbToCsvTasklet<E, T> implements Tasklet {
    
    /**
     * DBからデータを読み込んでCSVファイルに出力
     * 
     * @param contribution ステップの寄与情報
     * @param chunkContext チャンクコンテキスト
     * @return 処理結果
     * @throws Exception 処理エラー時の例外
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String filePath = (String) chunkContext.getStepContext()
                .getJobParameters().get("output.file.path");
        
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("output.file.path parameter is required");
        }
        
        log.info("Starting CSV export to: {}", filePath);
        
        // DBからデータを取得
        List<E> entities = loadEntities();
        log.info("Loaded {} records from database", entities.size());
        
        // エンティティをCSV DTOに変換
        List<T> csvRecords = new ArrayList<>();
        for (E entity : entities) {
            T csvRecord = convertToCsvDto(entity);
            if (csvRecord != null) {
                csvRecords.add(csvRecord);
            }
        }
        
        // CSVファイルに書き込み
        try (Writer writer = new FileWriter(filePath)) {
            // ヘッダーを書き込み
            writer.write(getCsvHeader() + "\n");
            
            // データを書き込み
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(',')
                    .withOrderedResults(true)
                    .build();
            
            beanToCsv.write(csvRecords);
            
            log.info("CSV export completed. Exported {} records", csvRecords.size());
        }
        
        return RepeatStatus.FINISHED;
    }
    
    /**
     * DBからエンティティを取得
     * 
     * @return エンティティリスト
     */
    protected abstract List<E> loadEntities();
    
    /**
     * エンティティをCSV DTOに変換
     * 
     * @param entity エンティティ
     * @return CSV DTO
     */
    protected abstract T convertToCsvDto(E entity);
    
    /**
     * CSVヘッダーを取得
     * 
     * @return CSVヘッダー
     */
    protected abstract String getCsvHeader();
}

