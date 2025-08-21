package com.example.batch.mapper;

import com.example.batch.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品テーブル用Mapperインターフェース
 */
@Mapper
public interface ProductMapper {
    
    /**
     * 全商品を取得
     * 
     * @return 商品リスト
     */
    List<Product> selectAll();
    
    /**
     * ページング用の商品取得
     * 
     * @param offset オフセット
     * @param limit 取得件数
     * @return 商品リスト
     */
    List<Product> selectWithPaging(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 商品コードで検索
     * 
     * @param productCode 商品コード
     * @return 商品エンティティ
     */
    Product selectByProductCode(@Param("productCode") String productCode);
    
    /**
     * カテゴリで検索
     * 
     * @param category カテゴリ
     * @return 商品リスト
     */
    List<Product> selectByCategory(@Param("category") String category);
    
    /**
     * 商品を登録
     * 
     * @param product 商品エンティティ
     * @return 登録件数
     */
    int insert(Product product);
    
    /**
     * 商品を更新
     * 
     * @param product 商品エンティティ
     * @return 更新件数
     */
    int update(Product product);
    
    /**
     * 商品を削除
     * 
     * @param id 商品ID
     * @return 削除件数
     */
    int delete(@Param("id") Long id);
    
    /**
     * 全件削除
     * 
     * @return 削除件数
     */
    int deleteAll();
    
    /**
     * 件数取得
     * 
     * @return 商品数
     */
    int count();
}