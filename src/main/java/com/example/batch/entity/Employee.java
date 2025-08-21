package com.example.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 従業員エンティティクラス
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    /** ID */
    private Long id;
    
    /** 従業員コード */
    private String employeeCode;
    
    /** 名 */
    private String firstName;
    
    /** 姓 */
    private String lastName;
    
    /** メールアドレス */
    private String email;
    
    /** 部署 */
    private String department;
    
    /** 役職 */
    private String position;
    
    /** 給与 */
    private BigDecimal salary;
    
    /** 入社日 */
    private LocalDate hireDate;
    
    /** ステータス */
    private String status;
    
    /** 作成日時 */
    private LocalDateTime createdAt;
    
    /** 更新日時 */
    private LocalDateTime updatedAt;
}