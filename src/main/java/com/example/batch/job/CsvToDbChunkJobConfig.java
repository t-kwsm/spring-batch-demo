package com.example.batch.job;

import com.example.batch.dto.CsvEmployee;
import com.example.batch.dto.CsvProduct;
import com.example.batch.dto.CsvSales;
import com.example.batch.entity.Employee;
import com.example.batch.entity.Product;
import com.example.batch.entity.Sales;
import com.example.batch.listener.JobCompletionListener;
import com.example.batch.mapper.EmployeeMapper;
import com.example.batch.mapper.ProductMapper;
import com.example.batch.mapper.SalesMapper;
import com.example.batch.processor.EmployeeProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * CSV→DB（チャンクモデル）ジョブ設定クラス
 * チャンクモデルを使用してCSVファイルからデータベースへデータを登録
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CsvToDbChunkJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EmployeeMapper employeeMapper;
    private final ProductMapper productMapper;
    private final SalesMapper salesMapper;
    private final EmployeeProcessor employeeProcessor;
    
    @Value("${app.batch.chunk-size:100}")
    private int chunkSize;
    
    /**
     * 従業員CSV→DBジョブ（チャンクモデル）
     */
    @Bean
    public Job employeeCsvToDbChunkJob(Step employeeCsvToDbChunkStep, 
                                       JobCompletionListener listener) {
        return new JobBuilder("employeeCsvToDbChunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(employeeCsvToDbChunkStep)
                .end()
                .build();
    }
    
    /**
     * 従業員CSV→DBステップ（チャンクモデル）
     */
    @Bean
    public Step employeeCsvToDbChunkStep() {
        return new StepBuilder("employeeCsvToDbChunkStep", jobRepository)
                .<CsvEmployee, Employee>chunk(chunkSize, transactionManager)
                .reader(employeeCsvReader(null))
                .processor(employeeProcessor)
                .writer(employeeWriter())
                .build();
    }
    
    /**
     * 従業員CSVリーダー
     */
    @Bean
    @StepScope
    public FlatFileItemReader<CsvEmployee> employeeCsvReader(
            @Value("#{jobParameters['input.file.path']}") String filePath) {
        BeanWrapperFieldSetMapper<CsvEmployee> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CsvEmployee.class);
        fieldSetMapper.setConversionService(createConversionService());
        
        return new FlatFileItemReaderBuilder<CsvEmployee>()
                .name("employeeCsvReader")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .names("employeeCode", "firstName", "lastName", "email", 
                       "department", "position", "salary", "hireDate", "status")
                .fieldSetMapper(fieldSetMapper)
                .linesToSkip(1) // ヘッダー行をスキップ
                .build();
    }
    
    /**
     * 日付変換サービス
     */
    private org.springframework.core.convert.ConversionService createConversionService() {
        org.springframework.format.support.DefaultFormattingConversionService conversionService = 
                new org.springframework.format.support.DefaultFormattingConversionService();
        org.springframework.format.datetime.standard.DateTimeFormatterRegistrar registrar = 
                new org.springframework.format.datetime.standard.DateTimeFormatterRegistrar();
        registrar.setDateFormatter(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        registrar.setDateTimeFormatter(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        registrar.registerFormatters(conversionService);
        return conversionService;
    }
    
    /**
     * 従業員ライター
     */
    @Bean
    public ItemWriter<Employee> employeeWriter() {
        return items -> {
            for (Employee employee : items) {
                try {
                    // 既存チェック
                    Employee existing = employeeMapper.selectByEmployeeCode(employee.getEmployeeCode());
                    if (existing != null) {
                        employee.setId(existing.getId());
                        employeeMapper.update(employee);
                        log.debug("Updated employee: {}", employee.getEmployeeCode());
                    } else {
                        employeeMapper.insert(employee);
                        log.debug("Inserted employee: {}", employee.getEmployeeCode());
                    }
                } catch (Exception e) {
                    log.error("Error writing employee: {}", employee.getEmployeeCode(), e);
                    throw e;
                }
            }
        };
    }
    
    /**
     * 商品CSV→DBジョブ（チャンクモデル）
     */
    @Bean
    public Job productCsvToDbChunkJob(Step productCsvToDbChunkStep,
                                      JobCompletionListener listener) {
        return new JobBuilder("productCsvToDbChunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(productCsvToDbChunkStep)
                .end()
                .build();
    }
    
    /**
     * 商品CSV→DBステップ（チャンクモデル）
     */
    @Bean
    public Step productCsvToDbChunkStep() {
        return new StepBuilder("productCsvToDbChunkStep", jobRepository)
                .<CsvProduct, Product>chunk(chunkSize, transactionManager)
                .reader(productCsvReader(null))
                .processor(productProcessor())
                .writer(productWriter())
                .build();
    }
    
    /**
     * 商品CSVリーダー
     */
    @Bean
    @StepScope
    public FlatFileItemReader<CsvProduct> productCsvReader(
            @Value("#{jobParameters['input.file.path']}") String filePath) {
        return new FlatFileItemReaderBuilder<CsvProduct>()
                .name("productCsvReader")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .names("productCode", "productName", "category", "price", 
                       "stockQuantity", "description", "manufacturer", "releaseDate", "isActive")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(CsvProduct.class);
                }})
                .linesToSkip(1)
                .build();
    }
    
    /**
     * 商品プロセッサー
     */
    @Bean
    public ItemProcessor<CsvProduct, Product> productProcessor() {
        return csvProduct -> {
            log.debug("Processing product: {}", csvProduct.getProductCode());
            
            if (csvProduct.getProductCode() == null || csvProduct.getProductCode().trim().isEmpty()) {
                return null;
            }
            
            return Product.builder()
                    .productCode(csvProduct.getProductCode())
                    .productName(csvProduct.getProductName())
                    .category(csvProduct.getCategory())
                    .price(csvProduct.getPrice())
                    .stockQuantity(csvProduct.getStockQuantity())
                    .description(csvProduct.getDescription())
                    .manufacturer(csvProduct.getManufacturer())
                    .releaseDate(csvProduct.getReleaseDate())
                    .isActive(csvProduct.getIsActive() != null ? csvProduct.getIsActive() : true)
                    .build();
        };
    }
    
    /**
     * 商品ライター
     */
    @Bean
    public ItemWriter<Product> productWriter() {
        return items -> {
            for (Product product : items) {
                try {
                    Product existing = productMapper.selectByProductCode(product.getProductCode());
                    if (existing != null) {
                        product.setId(existing.getId());
                        productMapper.update(product);
                        log.debug("Updated product: {}", product.getProductCode());
                    } else {
                        productMapper.insert(product);
                        log.debug("Inserted product: {}", product.getProductCode());
                    }
                } catch (Exception e) {
                    log.error("Error writing product: {}", product.getProductCode(), e);
                    throw e;
                }
            }
        };
    }
    
    /**
     * 売上CSV→DBジョブ（チャンクモデル）
     */
    @Bean
    public Job salesCsvToDbChunkJob(Step salesCsvToDbChunkStep,
                                    JobCompletionListener listener) {
        return new JobBuilder("salesCsvToDbChunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(salesCsvToDbChunkStep)
                .end()
                .build();
    }
    
    /**
     * 売上CSV→DBステップ（チャンクモデル）
     */
    @Bean
    public Step salesCsvToDbChunkStep() {
        return new StepBuilder("salesCsvToDbChunkStep", jobRepository)
                .<CsvSales, Sales>chunk(chunkSize, transactionManager)
                .reader(salesCsvReader(null))
                .processor(salesProcessor())
                .writer(salesWriter())
                .build();
    }
    
    /**
     * 売上CSVリーダー
     */
    @Bean
    @StepScope
    public FlatFileItemReader<CsvSales> salesCsvReader(
            @Value("#{jobParameters['input.file.path']}") String filePath) {
        return new FlatFileItemReaderBuilder<CsvSales>()
                .name("salesCsvReader")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .names("transactionId", "productCode", "customerName", "quantity",
                       "unitPrice", "totalAmount", "saleDate", "paymentMethod", "status")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(CsvSales.class);
                }})
                .linesToSkip(1)
                .build();
    }
    
    /**
     * 売上プロセッサー
     */
    @Bean
    public ItemProcessor<CsvSales, Sales> salesProcessor() {
        return csvSales -> {
            log.debug("Processing sales: {}", csvSales.getTransactionId());
            
            if (csvSales.getTransactionId() == null || csvSales.getTransactionId().trim().isEmpty()) {
                return null;
            }
            
            return Sales.builder()
                    .transactionId(csvSales.getTransactionId())
                    .productCode(csvSales.getProductCode())
                    .customerName(csvSales.getCustomerName())
                    .quantity(csvSales.getQuantity())
                    .unitPrice(csvSales.getUnitPrice())
                    .totalAmount(csvSales.getTotalAmount())
                    .saleDate(csvSales.getSaleDate())
                    .paymentMethod(csvSales.getPaymentMethod())
                    .status(csvSales.getStatus() != null ? csvSales.getStatus() : "COMPLETED")
                    .build();
        };
    }
    
    /**
     * 売上ライター
     */
    @Bean
    public ItemWriter<Sales> salesWriter() {
        return items -> {
            for (Sales sales : items) {
                try {
                    Sales existing = salesMapper.selectByTransactionId(sales.getTransactionId());
                    if (existing != null) {
                        sales.setId(existing.getId());
                        salesMapper.update(sales);
                        log.debug("Updated sales: {}", sales.getTransactionId());
                    } else {
                        salesMapper.insert(sales);
                        log.debug("Inserted sales: {}", sales.getTransactionId());
                    }
                } catch (Exception e) {
                    log.error("Error writing sales: {}", sales.getTransactionId(), e);
                    throw e;
                }
            }
        };
    }
}