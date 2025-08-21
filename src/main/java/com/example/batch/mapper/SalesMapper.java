package com.example.batch.mapper;

import com.example.batch.entity.Sales;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 売上テーブル用Mapperインターフェース
 */
@Mapper
public interface SalesMapper {
    
    /**
     * 全売上を取得
     * 
     * @return 売上リスト
     */
    List<Sales> selectAll();
    
    /**
     * ページング用の売上取得
     * 
     * @param offset オフセット
     * @param limit 取得件数
     * @return 売上リスト
     */
    List<Sales> selectWithPaging(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 取引IDで検索
     * 
     * @param transactionId 取引ID
     * @return 売上エンティティ
     */
    Sales selectByTransactionId(@Param("transactionId") String transactionId);
    
    /**
     * 期間で検索
     * 
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @return 売上リスト
     */
    List<Sales> selectByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * 売上を登録
     * 
     * @param sales 売上エンティティ
     * @return 登録件数
     */
    int insert(Sales sales);
    
    /**
     * 売上を更新
     * 
     * @param sales 売上エンティティ
     * @return 更新件数
     */
    int update(Sales sales);
    
    /**
     * 売上を削除
     * 
     * @param id 売上ID
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
     * @return 売上件数
     */
    int count();
}