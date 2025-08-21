package com.example.batch.tasklet;

import com.example.batch.dto.CsvProduct;
import com.example.batch.entity.Product;
import com.example.batch.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品DB→CSVタスクレット
 */
@Slf4j
@Component
public class ProductDbToCsvTasklet extends DbToCsvTasklet<Product, CsvProduct> {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    protected List<Product> loadEntities() {
        return productMapper.selectAll();
    }
    
    @Override
    protected CsvProduct convertToCsvDto(Product product) {
        log.debug("Converting product to CSV: {}", product.getProductCode());
        
        return CsvProduct.builder()
                .productCode(product.getProductCode())
                .productName(product.getProductName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .description(product.getDescription())
                .manufacturer(product.getManufacturer())
                .releaseDate(product.getReleaseDate())
                .isActive(product.getIsActive())
                .build();
    }
    
    @Override
    protected String getCsvHeader() {
        return "product_code,product_name,category,price,stock_quantity,description,manufacturer,release_date,is_active";
    }
}