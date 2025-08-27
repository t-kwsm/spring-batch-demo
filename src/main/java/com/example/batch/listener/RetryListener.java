package com.example.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryListener implements RetryListener {
    
    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        log.debug("リトライ処理開始: {}", context.getAttribute("item"));
        return true;
    }
    
    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (throwable != null) {
            log.error("リトライ失敗（最大試行回数到達）: {}, Error: {}", 
                context.getAttribute("item"), throwable.getMessage());
        } else {
            log.info("リトライ成功: {}, 試行回数: {}", 
                context.getAttribute("item"), context.getRetryCount());
        }
    }
    
    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        log.warn("リトライ中エラー発生（試行回数: {}）: {}, Error: {}", 
            context.getRetryCount(), 
            context.getAttribute("item"), 
            throwable.getMessage());
    }
}