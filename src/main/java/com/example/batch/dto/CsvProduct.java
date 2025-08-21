package com.example.batch.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品CSV用DTOクラス
 * ヘッダー付きとヘッダーなし両方のCSVに対応
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvProduct {
    
    /** 商品コード */
    @CsvBindByName(column = "product_code")
    @CsvBindByPosition(position = 0)
    private String productCode;
    
    /** 商品名 */
    @CsvBindByName(column = "product_name")
    @CsvBindByPosition(position = 1)
    private String productName;
    
    /** カテゴリ */
    @CsvBindByName(column = "category")
    @CsvBindByPosition(position = 2)
    private String category;
    
    /** 価格 */
    @CsvBindByName(column = "price")
    @CsvBindByPosition(position = 3)
    private BigDecimal price;
    
    /** 在庫数 */
    @CsvBindByName(column = "stock_quantity")
    @CsvBindByPosition(position = 4)
    private Integer stockQuantity;
    
    /** 説明 */
    @CsvBindByName(column = "description")
    @CsvBindByPosition(position = 5)
    private String description;
    
    /** 製造元 */
    @CsvBindByName(column = "manufacturer")
    @CsvBindByPosition(position = 6)
    private String manufacturer;
    
    /** リリース日 */
    @CsvBindByName(column = "release_date")
    @CsvBindByPosition(position = 7)
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate releaseDate;
    
    /** 有効フラグ */
    @CsvBindByName(column = "is_active")
    @CsvBindByPosition(position = 8)
    private Boolean isActive;
}