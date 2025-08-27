package com.example.batch.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "従業員コードは必須です")
    @Pattern(regexp = "^EMP[0-9]{6}$", message = "従業員コードの形式が不正です（例：EMP000001）")
    private String employeeCode;
    
    /** 名 */
    @CsvBindByName(column = "first_name")
    @CsvBindByPosition(position = 1)
    @NotBlank(message = "名は必須です")
    @Size(max = 50, message = "名は50文字以内で入力してください")
    private String firstName;
    
    /** 姓 */
    @CsvBindByName(column = "last_name")
    @CsvBindByPosition(position = 2)
    @NotBlank(message = "姓は必須です")
    @Size(max = 50, message = "姓は50文字以内で入力してください")
    private String lastName;
    
    /** メールアドレス */
    @CsvBindByName(column = "email")
    @CsvBindByPosition(position = 3)
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が不正です")
    private String email;
    
    /** 部署 */
    @CsvBindByName(column = "department")
    @CsvBindByPosition(position = 4)
    @NotBlank(message = "部署は必須です")
    @Pattern(regexp = "^(営業部|開発部|人事部|経理部|総務部)$", message = "部署名が不正です")
    private String department;
    
    /** 役職 */
    @CsvBindByName(column = "position")
    @CsvBindByPosition(position = 5)
    @NotBlank(message = "役職は必須です")
    private String position;
    
    /** 給与 */
    @CsvBindByName(column = "salary")
    @CsvBindByPosition(position = 6)
    @NotNull(message = "給与は必須です")
    @DecimalMin(value = "0.0", inclusive = false, message = "給与は0より大きい値を入力してください")
    @DecimalMax(value = "99999999.99", message = "給与の上限を超えています")
    private BigDecimal salary;
    
    /** 入社日 */
    @CsvBindByName(column = "hire_date")
    @CsvBindByPosition(position = 7)
    @CsvDate(value = "yyyy-MM-dd")
    @NotNull(message = "入社日は必須です")
    @PastOrPresent(message = "入社日は未来の日付を指定できません")
    private LocalDate hireDate;
    
    /** ステータス */
    @CsvBindByName(column = "status")
    @CsvBindByPosition(position = 8)
    @Pattern(regexp = "^(ACTIVE|INACTIVE|SUSPENDED)$", message = "ステータスはACTIVE、INACTIVE、SUSPENDEDのいずれかを指定してください")
    private String status;
}