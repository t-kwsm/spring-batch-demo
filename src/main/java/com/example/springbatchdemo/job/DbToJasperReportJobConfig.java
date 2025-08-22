package com.example.springbatchdemo.job;

import com.example.springbatchdemo.entity.Employee;
import com.example.springbatchdemo.mapper.EmployeeMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class DbToJasperReportJobConfig {
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @Autowired
    private DataSource dataSource;
    
    @Bean
    public MyBatisCursorItemReader<Employee> dbEmployeeReader() {
        return new MyBatisCursorItemReaderBuilder<Employee>()
                .sqlSessionFactory(sqlSessionFactory())
                .queryId("com.example.springbatchdemo.mapper.EmployeeMapper.findAll")
                .build();
    }
    
    @Bean
    public org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory() throws Exception {
        org.mybatis.spring.SqlSessionFactoryBean sqlSessionFactoryBean = 
                new org.mybatis.spring.SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }
    
    @Bean
    public ItemProcessor<Employee, Employee> dbEmployeeProcessor() {
        return employee -> {
            // You can add any processing logic here if needed
            return employee;
        };
    }
    
    @Bean
    public ItemWriter<Employee> jasperReportWriter() {
        return new ItemWriter<Employee>() {
            private List<Employee> allEmployees = new ArrayList<>();
            
            @Override
            public void write(List<? extends Employee> items) throws Exception {
                allEmployees.addAll(items);
                
                // Generate report after collecting all data
                if (isLastChunk()) {
                    generateJasperReport(allEmployees);
                }
            }
            
            private boolean isLastChunk() {
                // This is a simplified check - in production you might want a more robust solution
                return true;
            }
            
            private void generateJasperReport(List<Employee> employees) throws Exception {
                // Load the Jasper template
                InputStream reportStream = new ClassPathResource("reports/employee_report.jrxml")
                        .getInputStream();
                
                // Compile the report
                JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
                
                // Create data source
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);
                
                // Set parameters
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("ReportTitle", "Employee Report from Database");
                parameters.put("GeneratedDate", LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                // Fill the report
                JasperPrint jasperPrint = JasperFillManager.fillReport(
                        jasperReport, parameters, dataSource);
                
                // Export to PDF
                String outputPath = "output/employee_report_db_" + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                        ".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
                
                System.out.println("Report generated successfully: " + outputPath);
            }
        };
    }
    
    @Bean
    public Step dbToJasperReportStep() {
        return new StepBuilder("dbToJasperReportStep", jobRepository)
                .<Employee, Employee>chunk(100, transactionManager)
                .reader(dbEmployeeReader())
                .processor(dbEmployeeProcessor())
                .writer(jasperReportWriter())
                .build();
    }
    
    @Bean
    public Job dbToJasperReportJob() {
        return new JobBuilder("dbToJasperReportJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(dbToJasperReportStep())
                .end()
                .build();
    }
}