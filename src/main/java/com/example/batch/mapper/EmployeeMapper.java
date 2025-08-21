package com.example.batch.mapper;

import com.example.batch.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 従業員テーブル用Mapperインターフェース
 */
@Mapper
public interface EmployeeMapper {
    
    /**
     * 全従業員を取得
     * 
     * @return 従業員リスト
     */
    List<Employee> selectAll();
    
    /**
     * ページング用の従業員取得
     * 
     * @param offset オフセット
     * @param limit 取得件数
     * @return 従業員リスト
     */
    List<Employee> selectWithPaging(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 従業員コードで検索
     * 
     * @param employeeCode 従業員コード
     * @return 従業員エンティティ
     */
    Employee selectByEmployeeCode(@Param("employeeCode") String employeeCode);
    
    /**
     * 従業員を登録
     * 
     * @param employee 従業員エンティティ
     * @return 登録件数
     */
    int insert(Employee employee);
    
    /**
     * 従業員を更新
     * 
     * @param employee 従業員エンティティ
     * @return 更新件数
     */
    int update(Employee employee);
    
    /**
     * 従業員を削除
     * 
     * @param id 従業員ID
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
     * @return 従業員数
     */
    int count();
}