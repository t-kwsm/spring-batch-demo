package com.example.batch.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 売上CSV用DTOクラス
 * ヘッダー付きとヘッダーなし両方のCSVに対応
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvSales {
    
    /** 取引ID */
    @CsvBindByName(column = "transaction_id")
    @CsvBindByPosition(position = 0)
    private String transactionId;
    
    /** 商品コード */
    @CsvBindByName(column = "product_code")
    @CsvBindByPosition(position = 1)
    private String productCode;
    
    /** 顧客名 */
    @CsvBindByName(column = "customer_name")
    @CsvBindByPosition(position = 2)
    private String customerName;
    
    /** 数量 */
    @CsvBindByName(column = "quantity")
    @CsvBindByPosition(position = 3)
    private Integer quantity;
    
    /** 単価 */
    @CsvBindByName(column = "unit_price")
    @CsvBindByPosition(position = 4)
    private BigDecimal unitPrice;
    
    /** 合計金額 */
    @CsvBindByName(column = "total_amount")
    @CsvBindByPosition(position = 5)
    private BigDecimal totalAmount;
    
    /** 売上日時 */
    @CsvBindByName(column = "sale_date")
    @CsvBindByPosition(position = 6)
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleDate;
    
    /** 支払方法 */
    @CsvBindByName(column = "payment_method")
    @CsvBindByPosition(position = 7)
    private String paymentMethod;
    
    /** ステータス */
    @CsvBindByName(column = "status")
    @CsvBindByPosition(position = 8)
    private String status;
}