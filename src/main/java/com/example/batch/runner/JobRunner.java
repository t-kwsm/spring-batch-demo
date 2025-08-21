package com.example.batch.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ジョブ起動用のCommandLineRunner
 * コマンドライン引数でジョブ名とパラメータを指定して実行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobRunner implements CommandLineRunner {
    
    private final JobLauncher jobLauncher;
    private final ApplicationContext applicationContext;
    
    /**
     * コマンドライン実行時の処理
     * 
     * @param args コマンドライン引数
     * @throws Exception 実行エラー時の例外
     */
    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            log.info("No job specified. Available jobs:");
            printAvailableJobs();
            return;
        }
        
        String jobName = args[0];
        
        // ジョブが存在するか確認
        if (!applicationContext.containsBean(jobName)) {
            log.error("Job '{}' not found. Available jobs:", jobName);
            printAvailableJobs();
            return;
        }
        
        // ジョブパラメータを構築
        JobParametersBuilder parametersBuilder = new JobParametersBuilder();
        
        // 実行時刻を追加（ジョブの再実行を可能にするため）
        parametersBuilder.addString("run.time", 
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // コマンドライン引数からパラメータを追加
        for (int i = 1; i < args.length; i++) {
            String[] keyValue = args[i].split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                
                // ファイルパスの検証
                if (key.contains("file.path")) {
                    validateFilePath(key, value);
                }
                
                parametersBuilder.addString(key, value);
                log.info("Added parameter: {} = {}", key, value);
            }
        }
        
        JobParameters jobParameters = parametersBuilder.toJobParameters();
        
        // ジョブを実行
        try {
            Job job = applicationContext.getBean(jobName, Job.class);
            log.info("Starting job: {}", jobName);
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            log.error("Error running job: {}", jobName, e);
            throw e;
        }
    }
    
    /**
     * 利用可能なジョブ一覧を表示
     */
    private void printAvailableJobs() {
        String[] jobNames = applicationContext.getBeanNamesForType(Job.class);
        log.info("====================================");
        log.info("Available Jobs:");
        for (String name : jobNames) {
            log.info("  - {}", name);
        }
        log.info("====================================");
        log.info("Usage examples:");
        log.info("  CSV to DB (Chunk):");
        log.info("    mvn spring-boot:run -Dspring-boot.run.arguments=\"employeeCsvToDbChunkJob input.file.path=data/input/employees.csv\"");
        log.info("  CSV to DB (Tasklet):");
        log.info("    mvn spring-boot:run -Dspring-boot.run.arguments=\"employeeCsvToDbTaskletJob input.file.path=data/input/employees.csv\"");
        log.info("  DB to CSV (Chunk):");
        log.info("    mvn spring-boot:run -Dspring-boot.run.arguments=\"employeeDbToCsvChunkJob output.file.path=data/output/employees.csv\"");
        log.info("  DB to CSV (Tasklet):");
        log.info("    mvn spring-boot:run -Dspring-boot.run.arguments=\"employeeDbToCsvTaskletJob output.file.path=data/output/employees.csv\"");
    }
    
    /**
     * ファイルパスの検証
     * 
     * @param key パラメータキー
     * @param path ファイルパス
     * @throws Exception パス検証エラー時の例外
     */
    private void validateFilePath(String key, String path) throws Exception {
        if (key.contains("input")) {
            // 入力ファイルは存在チェック
            if (!Files.exists(Paths.get(path))) {
                throw new IllegalArgumentException("Input file not found: " + path);
            }
        } else if (key.contains("output")) {
            // 出力ファイルは親ディレクトリの存在チェック
            if (!Files.exists(Paths.get(path).getParent())) {
                // 親ディレクトリを作成
                Files.createDirectories(Paths.get(path).getParent());
                log.info("Created output directory: {}", Paths.get(path).getParent());
            }
        }
    }
}