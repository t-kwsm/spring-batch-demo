package com.example.batch.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
public class JobParametersValidator extends DefaultJobParametersValidator {
    
    private static final String INPUT_FILE = "inputFile";
    private static final String OUTPUT_FILE = "outputFile";
    private static final String CHUNK_SIZE = "chunkSize";
    private static final String PROCESSING_DATE = "processingDate";
    
    public JobParametersValidator() {
        setRequiredKeys(new String[]{INPUT_FILE, OUTPUT_FILE});
        setOptionalKeys(new String[]{CHUNK_SIZE, PROCESSING_DATE, "run.id"});
    }
    
    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        super.validate(parameters);
        
        Map<String, Object> params = parameters.getParameters();
        
        // Input file validation
        String inputFile = parameters.getString(INPUT_FILE);
        if (inputFile != null) {
            if (!inputFile.endsWith(".csv")) {
                throw new JobParametersInvalidException("Input file must be a CSV file: " + inputFile);
            }
            if (inputFile.contains("..")) {
                throw new JobParametersInvalidException("Invalid file path: " + inputFile);
            }
        }
        
        // Output file validation
        String outputFile = parameters.getString(OUTPUT_FILE);
        if (outputFile != null) {
            if (!outputFile.endsWith(".csv") && !outputFile.endsWith(".pdf")) {
                throw new JobParametersInvalidException("Output file must be CSV or PDF: " + outputFile);
            }
            if (outputFile.contains("..")) {
                throw new JobParametersInvalidException("Invalid file path: " + outputFile);
            }
        }
        
        // Chunk size validation
        String chunkSizeStr = parameters.getString(CHUNK_SIZE);
        if (chunkSizeStr != null) {
            try {
                int chunkSize = Integer.parseInt(chunkSizeStr);
                if (chunkSize <= 0 || chunkSize > 10000) {
                    throw new JobParametersInvalidException("Chunk size must be between 1 and 10000: " + chunkSize);
                }
            } catch (NumberFormatException e) {
                throw new JobParametersInvalidException("Invalid chunk size format: " + chunkSizeStr);
            }
        }
        
        // Processing date validation
        String processingDate = parameters.getString(PROCESSING_DATE);
        if (processingDate != null) {
            try {
                LocalDate.parse(processingDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw new JobParametersInvalidException("Invalid date format. Use YYYY-MM-DD: " + processingDate);
            }
        }
    }
}