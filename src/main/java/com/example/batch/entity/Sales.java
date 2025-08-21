package com.example.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 売上エンティティクラス
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sales {
    
    /** ID */
    private Long id;
    
    /** 取引ID */
    private String transactionId;
    
    /** 商品コード */
    private String productCode;
    
    /** 顧客名 */
    private String customerName;
    
    /** 数量 */
    private Integer quantity;
    
    /** 単価 */
    private BigDecimal unitPrice;
    
    /** 合計金額 */
    private BigDecimal totalAmount;
    
    /** 売上日時 */
    private LocalDateTime saleDate;
    
    /** 支払方法 */
    private String paymentMethod;
    
    /** ステータス */
    private String status;
    
    /** 作成日時 */
    private LocalDateTime createdAt;
    
    /** 更新日時 */
    private LocalDateTime updatedAt;
}