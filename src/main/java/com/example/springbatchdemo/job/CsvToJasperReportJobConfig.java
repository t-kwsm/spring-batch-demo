package com.example.springbatchdemo.job;

import com.example.springbatchdemo.entity.Employee;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class CsvToJasperReportJobConfig {
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @Bean
    public ItemReader<Employee> csvEmployeeReader() {
        return new ItemReader<Employee>() {
            private MappingIterator<Employee> iterator;
            private boolean initialized = false;
            
            @Override
            public Employee read() throws Exception {
                if (!initialized) {
                    initialize();
                }
                
                if (iterator != null && iterator.hasNext()) {
                    return iterator.next();
                }
                return null;
            }
            
            private void initialize() throws Exception {
                CsvMapper csvMapper = new CsvMapper();
                csvMapper.registerModule(new JavaTimeModule());
                
                CsvSchema schema = CsvSchema.builder()
                        .addColumn("employeeId")
                        .addColumn("firstName")
                        .addColumn("lastName")
                        .addColumn("email")
                        .addColumn("department")
                        .addColumn("salary")
                        .addColumn("hireDate")
                        .build()
                        .withHeader();
                
                ObjectReader reader = csvMapper
                        .readerFor(Employee.class)
                        .with(schema);
                
                File csvFile = new FileSystemResource("input/employees.csv").getFile();
                iterator = reader.readValues(csvFile);
                initialized = true;
            }
        };
    }
    
    @Bean
    public ItemProcessor<Employee, Employee> csvEmployeeProcessor() {
        return employee -> {
            // You can add any processing logic here if needed
            // For example, validation or transformation
            return employee;
        };
    }
    
    @Bean
    public ItemWriter<Employee> csvJasperReportWriter() {
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
                parameters.put("ReportTitle", "Employee Report from CSV");
                parameters.put("GeneratedDate", LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                // Fill the report
                JasperPrint jasperPrint = JasperFillManager.fillReport(
                        jasperReport, parameters, dataSource);
                
                // Export to PDF
                String outputPath = "output/employee_report_csv_" + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                        ".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
                
                // Also export to other formats if needed
                // Export to Excel
                String excelPath = "output/employee_report_csv_" + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                        ".xlsx";
                net.sf.jasperreports.export.SimpleXlsxReportConfiguration xlsxConfig = 
                        new net.sf.jasperreports.export.SimpleXlsxReportConfiguration();
                xlsxConfig.setOnePagePerSheet(false);
                xlsxConfig.setRemoveEmptySpaceBetweenRows(true);
                
                net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter xlsxExporter = 
                        new net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter();
                xlsxExporter.setExporterInput(new net.sf.jasperreports.export.SimpleExporterInput(jasperPrint));
                xlsxExporter.setExporterOutput(new net.sf.jasperreports.export.SimpleOutputStreamExporterOutput(excelPath));
                xlsxExporter.setConfiguration(xlsxConfig);
                xlsxExporter.exportReport();
                
                System.out.println("PDF Report generated: " + outputPath);
                System.out.println("Excel Report generated: " + excelPath);
            }
        };
    }
    
    @Bean
    public Step csvToJasperReportStep() {
        return new StepBuilder("csvToJasperReportStep", jobRepository)
                .<Employee, Employee>chunk(100, transactionManager)
                .reader(csvEmployeeReader())
                .processor(csvEmployeeProcessor())
                .writer(csvJasperReportWriter())
                .build();
    }
    
    @Bean
    public Job csvToJasperReportJob() {
        return new JobBuilder("csvToJasperReportJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(csvToJasperReportStep())
                .end()
                .build();
    }
}