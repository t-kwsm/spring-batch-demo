package com.example.batch.tasklet;

import com.example.batch.dto.CsvSales;
import com.example.batch.entity.Sales;
import com.example.batch.mapper.SalesMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 売上CSV→DBタスクレット
 */
@Slf4j
@Component
public class SalesCsvToDbTasklet extends CsvToDbTasklet<CsvSales, Sales> {
    
    @Autowired
    private SalesMapper salesMapper;
    
    @Override
    protected Class<CsvSales> getCsvType() {
        return CsvSales.class;
    }
    
    @Override
    protected Sales convertToEntity(CsvSales csvSales) {
        if (csvSales.getTransactionId() == null || csvSales.getTransactionId().trim().isEmpty()) {
            log.warn("Invalid transaction ID, skipping record");
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
    }
    
    @Override
    protected void saveEntity(Sales sales) {
        Sales existing = salesMapper.selectByTransactionId(sales.getTransactionId());
        if (existing != null) {
            sales.setId(existing.getId());
            salesMapper.update(sales);
            log.debug("Updated sales: {}", sales.getTransactionId());
        } else {
            salesMapper.insert(sales);
            log.debug("Inserted sales: {}", sales.getTransactionId());
        }
    }
}