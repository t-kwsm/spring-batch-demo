package com.example.batch.tasklet;

import com.example.batch.dto.CsvProduct;
import com.example.batch.entity.Product;
import com.example.batch.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 商品CSV→DBタスクレット
 */
@Slf4j
@Component
public class ProductCsvToDbTasklet extends CsvToDbTasklet<CsvProduct, Product> {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    protected Class<CsvProduct> getCsvType() {
        return CsvProduct.class;
    }
    
    @Override
    protected Product convertToEntity(CsvProduct csvProduct) {
        if (csvProduct.getProductCode() == null || csvProduct.getProductCode().trim().isEmpty()) {
            log.warn("Invalid product code, skipping record");
            return null;
        }
        
        return Product.builder()
                .productCode(csvProduct.getProductCode())
                .productName(csvProduct.getProductName())
                .category(csvProduct.getCategory())
                .price(csvProduct.getPrice())
                .stockQuantity(csvProduct.getStockQuantity())
                .description(csvProduct.getDescription())
                .manufacturer(csvProduct.getManufacturer())
                .releaseDate(csvProduct.getReleaseDate())
                .isActive(csvProduct.getIsActive() != null ? csvProduct.getIsActive() : true)
                .build();
    }
    
    @Override
    protected void saveEntity(Product product) {
        Product existing = productMapper.selectByProductCode(product.getProductCode());
        if (existing != null) {
            product.setId(existing.getId());
            productMapper.update(product);
            log.debug("Updated product: {}", product.getProductCode());
        } else {
            productMapper.insert(product);
            log.debug("Inserted product: {}", product.getProductCode());
        }
    }
}