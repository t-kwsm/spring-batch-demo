package com.example.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品エンティティクラス
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    /** ID */
    private Long id;
    
    /** 商品コード */
    private String productCode;
    
    /** 商品名 */
    private String productName;
    
    /** カテゴリ */
    private String category;
    
    /** 価格 */
    private BigDecimal price;
    
    /** 在庫数 */
    private Integer stockQuantity;
    
    /** 説明 */
    private String description;
    
    /** 製造元 */
    private String manufacturer;
    
    /** リリース日 */
    private LocalDate releaseDate;
    
    /** 有効フラグ */
    private Boolean isActive;
    
    /** 作成日時 */
    private LocalDateTime createdAt;
    
    /** 更新日時 */
    private LocalDateTime updatedAt;
}