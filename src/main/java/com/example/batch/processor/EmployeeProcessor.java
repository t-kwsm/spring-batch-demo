package com.example.batch.processor;

import com.example.batch.dto.CsvEmployee;
import com.example.batch.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 従業員データ処理プロセッサー
 * CSVデータをエンティティに変換し、ビジネスロジックを適用
 */
@Slf4j
@Component
public class EmployeeProcessor implements ItemProcessor<CsvEmployee, Employee> {
    
    /**
     * CSV従業員データをエンティティに変換
     * 
     * @param csvEmployee CSV従業員データ
     * @return 従業員エンティティ
     * @throws Exception 処理エラー時の例外
     */
    @Override
    public Employee process(CsvEmployee csvEmployee) throws Exception {
        log.debug("Processing employee: {}", csvEmployee.getEmployeeCode());
        
        // データ検証
        if (csvEmployee.getEmployeeCode() == null || csvEmployee.getEmployeeCode().trim().isEmpty()) {
            log.warn("Invalid employee code, skipping record");
            return null; // nullを返すとこのレコードはスキップされる
        }
        
        // CSV DTOからエンティティへの変換
        Employee employee = Employee.builder()
                .employeeCode(csvEmployee.getEmployeeCode())
                .firstName(csvEmployee.getFirstName())
                .lastName(csvEmployee.getLastName())
                .email(csvEmployee.getEmail())
                .department(csvEmployee.getDepartment())
                .position(csvEmployee.getPosition())
                .salary(csvEmployee.getSalary())
                .hireDate(csvEmployee.getHireDate())
                .status(csvEmployee.getStatus() != null ? csvEmployee.getStatus() : "ACTIVE")
                .build();
        
        // ビジネスロジックの適用（必要に応じて）
        // 例: メールアドレスの正規化
        if (employee.getEmail() != null) {
            employee.setEmail(employee.getEmail().toLowerCase());
        }
        
        log.debug("Processed employee: {}", employee.getEmployeeCode());
        return employee;
    }
}