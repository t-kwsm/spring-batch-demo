package com.example.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Batchデモアプリケーションのメインクラス
 */
@SpringBootApplication
public class SpringBatchDemoApplication {
    
    /**
     * アプリケーションのエントリーポイント
     * 
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBatchDemoApplication.class, args);
    }
}