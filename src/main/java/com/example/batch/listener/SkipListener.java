package com.example.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class SkipListener<T, S> implements SkipListener<T, S> {
    
    private static final String ERROR_LOG_PATH = "output/error_records.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void onSkipInRead(Throwable throwable) {
        log.error("スキップされたレコード（読み込み時）: {}", throwable.getMessage());
        
        if (throwable instanceof FlatFileParseException) {
            FlatFileParseException ffpe = (FlatFileParseException) throwable;
            logErrorToFile(String.format(
                "READ_ERROR [%s] Line: %d, Input: %s, Error: %s",
                LocalDateTime.now().format(DATE_FORMATTER),
                ffpe.getLineNumber(),
                ffpe.getInput(),
                ffpe.getMessage()
            ));
        } else {
            logErrorToFile(String.format(
                "READ_ERROR [%s] Error: %s",
                LocalDateTime.now().format(DATE_FORMATTER),
                throwable.getMessage()
            ));
        }
    }
    
    @Override
    public void onSkipInProcess(T item, Throwable throwable) {
        log.error("スキップされたレコード（処理時）: Item={}, Error={}", item, throwable.getMessage());
        
        if (throwable instanceof ValidationException) {
            logErrorToFile(String.format(
                "VALIDATION_ERROR [%s] Item: %s, Error: %s",
                LocalDateTime.now().format(DATE_FORMATTER),
                item.toString(),
                throwable.getMessage()
            ));
        } else {
            logErrorToFile(String.format(
                "PROCESS_ERROR [%s] Item: %s, Error: %s",
                LocalDateTime.now().format(DATE_FORMATTER),
                item.toString(),
                throwable.getMessage()
            ));
        }
    }
    
    @Override
    public void onSkipInWrite(S item, Throwable throwable) {
        log.error("スキップされたレコード（書き込み時）: Item={}, Error={}", item, throwable.getMessage());
        
        logErrorToFile(String.format(
            "WRITE_ERROR [%s] Item: %s, Error: %s",
            LocalDateTime.now().format(DATE_FORMATTER),
            item.toString(),
            throwable.getMessage()
        ));
    }
    
    private void logErrorToFile(String errorMessage) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ERROR_LOG_PATH, true))) {
            writer.println(errorMessage);
        } catch (IOException e) {
            log.error("エラーログファイルへの書き込みに失敗しました: {}", e.getMessage());
        }
    }
}