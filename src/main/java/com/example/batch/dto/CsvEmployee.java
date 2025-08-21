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
 * 従業員CSV用DTOクラス
 * ヘッダー付きとヘッダーなし両方のCSVに対応
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvEmployee {
    
    /** 従業員コード */
    @CsvBindByName(column = "employee_code")
    @CsvBindByPosition(position = 0)
    private String employeeCode;
    
    /** 名 */
    @CsvBindByName(column = "first_name")
    @CsvBindByPosition(position = 1)
    private String firstName;
    
    /** 姓 */
    @CsvBindByName(column = "last_name")
    @CsvBindByPosition(position = 2)
    private String lastName;
    
    /** メールアドレス */
    @CsvBindByName(column = "email")
    @CsvBindByPosition(position = 3)
    private String email;
    
    /** 部署 */
    @CsvBindByName(column = "department")
    @CsvBindByPosition(position = 4)
    private String department;
    
    /** 役職 */
    @CsvBindByName(column = "position")
    @CsvBindByPosition(position = 5)
    private String position;
    
    /** 給与 */
    @CsvBindByName(column = "salary")
    @CsvBindByPosition(position = 6)
    private BigDecimal salary;
    
    /** 入社日 */
    @CsvBindByName(column = "hire_date")
    @CsvBindByPosition(position = 7)
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate hireDate;
    
    /** ステータス */
    @CsvBindByName(column = "status")
    @CsvBindByPosition(position = 8)
    private String status;
}