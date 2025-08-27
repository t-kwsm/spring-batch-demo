package com.example.batch.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;

import static org.junit.jupiter.api.Assertions.*;

class JobParametersValidatorTest {
    
    private JobParametersValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new JobParametersValidator();
    }
    
    @Test
    void testValidParameters() {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "input.csv")
                .addString("outputFile", "output.csv")
                .addString("chunkSize", "100")
                .addString("processingDate", "2024-01-01")
                .toJobParameters();
        
        assertDoesNotThrow(() -> validator.validate(params));
    }
    
    @Test
    void testMissingRequiredParameters() {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "input.csv")
                .toJobParameters();
        
        assertThrows(JobParametersInvalidException.class, 
                () -> validator.validate(params));
    }
    
    @Test
    void testInvalidInputFileExtension() {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "input.txt")
                .addString("outputFile", "output.csv")
                .toJobParameters();
        
        JobParametersInvalidException exception = assertThrows(
                JobParametersInvalidException.class,
                () -> validator.validate(params)
        );
        
        assertTrue(exception.getMessage().contains("CSV file"));
    }
    
    @Test
    void testInvalidChunkSize() {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "input.csv")
                .addString("outputFile", "output.csv")
                .addString("chunkSize", "20000")
                .toJobParameters();
        
        JobParametersInvalidException exception = assertThrows(
                JobParametersInvalidException.class,
                () -> validator.validate(params)
        );
        
        assertTrue(exception.getMessage().contains("between 1 and 10000"));
    }
    
    @Test
    void testInvalidDateFormat() {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "input.csv")
                .addString("outputFile", "output.csv")
                .addString("processingDate", "01-01-2024")
                .toJobParameters();
        
        JobParametersInvalidException exception = assertThrows(
                JobParametersInvalidException.class,
                () -> validator.validate(params)
        );
        
        assertTrue(exception.getMessage().contains("YYYY-MM-DD"));
    }
    
    @Test
    void testPathTraversalAttack() {
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "../../../etc/passwd.csv")
                .addString("outputFile", "output.csv")
                .toJobParameters();
        
        JobParametersInvalidException exception = assertThrows(
                JobParametersInvalidException.class,
                () -> validator.validate(params)
        );
        
        assertTrue(exception.getMessage().contains("Invalid file path"));
    }
}