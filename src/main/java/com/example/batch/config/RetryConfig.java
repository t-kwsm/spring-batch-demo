package com.example.batch.config;

import com.example.batch.exception.BusinessException;
import com.example.batch.listener.RetryListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.net.SocketTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRetry
@RequiredArgsConstructor
public class RetryConfig {
    
    private final RetryListener retryListener;
    
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // リトライポリシーの設定
        retryTemplate.setRetryPolicy(retryPolicy());
        
        // バックオフポリシーの設定
        retryTemplate.setBackOffPolicy(backOffPolicy());
        
        // リトライリスナーの設定
        retryTemplate.registerListener(retryListener);
        
        return retryTemplate;
    }
    
    @Bean
    public RetryPolicy retryPolicy() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        
        // リトライ可能な例外を定義
        retryableExceptions.put(TransientDataAccessException.class, true);
        retryableExceptions.put(SQLTransientConnectionException.class, true);
        retryableExceptions.put(SocketTimeoutException.class, true);
        
        // ビジネス例外はretryableフラグで判定
        retryableExceptions.put(BusinessException.class, false);
        
        // リトライ不可能な例外
        retryableExceptions.put(ValidationException.class, false);
        retryableExceptions.put(IllegalArgumentException.class, false);
        retryableExceptions.put(NullPointerException.class, false);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
        return retryPolicy;
    }
    
    @Bean
    public BackOffPolicy backOffPolicy() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 初期待機時間: 1秒
        backOffPolicy.setMultiplier(2.0);       // 乗数: 2倍
        backOffPolicy.setMaxInterval(10000);    // 最大待機時間: 10秒
        return backOffPolicy;
    }
}