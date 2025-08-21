package com.example.batch.job;

import com.example.batch.dto.CsvEmployee;
import com.example.batch.dto.CsvProduct;
import com.example.batch.dto.CsvSales;
import com.example.batch.entity.Employee;
import com.example.batch.entity.Product;
import com.example.batch.entity.Sales;
import com.example.batch.listener.JobCompletionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.format.DateTimeFormatter;

/**
 * DB→CSV（チャンクモデル）ジョブ設定クラス
 * チャンクモデルを使用してデータベースからCSVファイルへデータを出力
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DbToCsvChunkJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SqlSessionFactory sqlSessionFactory;
    private final JobCompletionListener listener;
    
    @Value("${app.batch.chunk-size:100}")
    private int chunkSize;
    
    @Value("${app.batch.page-size:100}")
    private int pageSize;
    
    /**
     * 従業員DB→CSVジョブ（チャンクモデル）
     */
    @Bean
    public Job employeeDbToCsvChunkJob(Step employeeDbToCsvChunkStep) {
        return new JobBuilder("employeeDbToCsvChunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(employeeDbToCsvChunkStep)
                .end()
                .build();
    }
    
    /**
     * 従業員DB→CSVステップ（チャンクモデル）
     */
    @Bean
    public Step employeeDbToCsvChunkStep() {
        return new StepBuilder("employeeDbToCsvChunkStep", jobRepository)
                .<Employee, CsvEmployee>chunk(chunkSize, transactionManager)
                .reader(employeeDbReader())
                .processor(employeeToCsvProcessor())
                .writer(employeeCsvWriter(null))
                .build();
    }
    
    /**
     * 従業員DBリーダー
     */
    @Bean
    public MyBatisPagingItemReader<Employee> employeeDbReader() {
        return new MyBatisPagingItemReaderBuilder<Employee>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.example.batch.mapper.EmployeeMapper.selectAll")
                .pageSize(pageSize)
                .build();
    }
    
    /**
     * 従業員エンティティ→CSV DTOプロセッサー
     */
    @Bean
    public ItemProcessor<Employee, CsvEmployee> employeeToCsvProcessor() {
        return employee -> {
            log.debug("Processing employee for CSV: {}", employee.getEmployeeCode());
            
            return CsvEmployee.builder()
                    .employeeCode(employee.getEmployeeCode())
                    .firstName(employee.getFirstName())
                    .lastName(employee.getLastName())
                    .email(employee.getEmail())
                    .department(employee.getDepartment())
                    .position(employee.getPosition())
                    .salary(employee.getSalary())
                    .hireDate(employee.getHireDate())
                    .status(employee.getStatus())
                    .build();
        };
    }
    
    /**
     * 従業員CSVライター
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<CsvEmployee> employeeCsvWriter(
            @Value("#{jobParameters['output.file.path']}") String filePath) {
        
        BeanWrapperFieldExtractor<CsvEmployee> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"employeeCode", "firstName", "lastName", "email",
                "department", "position", "salary", "hireDate", "status"});
        
        DelimitedLineAggregator<CsvEmployee> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);
        
        return new FlatFileItemWriterBuilder<CsvEmployee>()
                .name("employeeCsvWriter")
                .resource(new FileSystemResource(filePath))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("employee_code,first_name,last_name,email,department,position,salary,hire_date,status"))
                .build();
    }
    
    /**
     * 商品DB→CSVジョブ（チャンクモデル）
     */
    @Bean
    public Job productDbToCsvChunkJob(Step productDbToCsvChunkStep) {
        return new JobBuilder("productDbToCsvChunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(productDbToCsvChunkStep)
                .end()
                .build();
    }
    
    /**
     * 商品DB→CSVステップ（チャンクモデル）
     */
    @Bean
    public Step productDbToCsvChunkStep() {
        return new StepBuilder("productDbToCsvChunkStep", jobRepository)
                .<Product, CsvProduct>chunk(chunkSize, transactionManager)
                .reader(productDbReader())
                .processor(productToCsvProcessor())
                .writer(productCsvWriter(null))
                .build();
    }
    
    /**
     * 商品DBリーダー
     */
    @Bean
    public MyBatisPagingItemReader<Product> productDbReader() {
        return new MyBatisPagingItemReaderBuilder<Product>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.example.batch.mapper.ProductMapper.selectAll")
                .pageSize(pageSize)
                .build();
    }
    
    /**
     * 商品エンティティ→CSV DTOプロセッサー
     */
    @Bean
    public ItemProcessor<Product, CsvProduct> productToCsvProcessor() {
        return product -> {
            log.debug("Processing product for CSV: {}", product.getProductCode());
            
            return CsvProduct.builder()
                    .productCode(product.getProductCode())
                    .productName(product.getProductName())
                    .category(product.getCategory())
                    .price(product.getPrice())
                    .stockQuantity(product.getStockQuantity())
                    .description(product.getDescription())
                    .manufacturer(product.getManufacturer())
                    .releaseDate(product.getReleaseDate())
                    .isActive(product.getIsActive())
                    .build();
        };
    }
    
    /**
     * 商品CSVライター
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<CsvProduct> productCsvWriter(
            @Value("#{jobParameters['output.file.path']}") String filePath) {
        
        BeanWrapperFieldExtractor<CsvProduct> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"productCode", "productName", "category", "price",
                "stockQuantity", "description", "manufacturer", "releaseDate", "isActive"});
        
        DelimitedLineAggregator<CsvProduct> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);
        
        return new FlatFileItemWriterBuilder<CsvProduct>()
                .name("productCsvWriter")
                .resource(new FileSystemResource(filePath))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("product_code,product_name,category,price,stock_quantity,description,manufacturer,release_date,is_active"))
                .build();
    }
    
    /**
     * 売上DB→CSVジョブ（チャンクモデル）
     */
    @Bean
    public Job salesDbToCsvChunkJob(Step salesDbToCsvChunkStep) {
        return new JobBuilder("salesDbToCsvChunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(salesDbToCsvChunkStep)
                .end()
                .build();
    }
    
    /**
     * 売上DB→CSVステップ（チャンクモデル）
     */
    @Bean
    public Step salesDbToCsvChunkStep() {
        return new StepBuilder("salesDbToCsvChunkStep", jobRepository)
                .<Sales, CsvSales>chunk(chunkSize, transactionManager)
                .reader(salesDbReader())
                .processor(salesToCsvProcessor())
                .writer(salesCsvWriter(null))
                .build();
    }
    
    /**
     * 売上DBリーダー
     */
    @Bean
    public MyBatisPagingItemReader<Sales> salesDbReader() {
        return new MyBatisPagingItemReaderBuilder<Sales>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.example.batch.mapper.SalesMapper.selectAll")
                .pageSize(pageSize)
                .build();
    }
    
    /**
     * 売上エンティティ→CSV DTOプロセッサー
     */
    @Bean
    public ItemProcessor<Sales, CsvSales> salesToCsvProcessor() {
        return sales -> {
            log.debug("Processing sales for CSV: {}", sales.getTransactionId());
            
            return CsvSales.builder()
                    .transactionId(sales.getTransactionId())
                    .productCode(sales.getProductCode())
                    .customerName(sales.getCustomerName())
                    .quantity(sales.getQuantity())
                    .unitPrice(sales.getUnitPrice())
                    .totalAmount(sales.getTotalAmount())
                    .saleDate(sales.getSaleDate())
                    .paymentMethod(sales.getPaymentMethod())
                    .status(sales.getStatus())
                    .build();
        };
    }
    
    /**
     * 売上CSVライター
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<CsvSales> salesCsvWriter(
            @Value("#{jobParameters['output.file.path']}") String filePath) {
        
        BeanWrapperFieldExtractor<CsvSales> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"transactionId", "productCode", "customerName", "quantity",
                "unitPrice", "totalAmount", "saleDate", "paymentMethod", "status"});
        
        DelimitedLineAggregator<CsvSales> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);
        
        return new FlatFileItemWriterBuilder<CsvSales>()
                .name("salesCsvWriter")
                .resource(new FileSystemResource(filePath))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("transaction_id,product_code,customer_name,quantity,unit_price,total_amount,sale_date,payment_method,status"))
                .build();
    }
}