package com.example.batch.tasklet;

import com.example.batch.dto.CsvSales;
import com.example.batch.entity.Sales;
import com.example.batch.mapper.SalesMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 売上DB→CSVタスクレット
 */
@Slf4j
@Component
public class SalesDbToCsvTasklet extends DbToCsvTasklet<Sales, CsvSales> {
    
    @Autowired
    private SalesMapper salesMapper;
    
    @Override
    protected List<Sales> loadEntities() {
        return salesMapper.selectAll();
    }
    
    @Override
    protected CsvSales convertToCsvDto(Sales sales) {
        log.debug("Converting sales to CSV: {}", sales.getTransactionId());
        
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
    }
    
    @Override
    protected String getCsvHeader() {
        return "transaction_id,product_code,customer_name,quantity,unit_price,total_amount,sale_date,payment_method,status";
    }
}