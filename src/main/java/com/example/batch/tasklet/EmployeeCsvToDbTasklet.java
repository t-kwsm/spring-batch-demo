package com.example.batch.tasklet;

import com.example.batch.dto.CsvEmployee;
import com.example.batch.entity.Employee;
import com.example.batch.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 従業員CSV→DBタスクレット
 */
@Slf4j
@Component
public class EmployeeCsvToDbTasklet extends CsvToDbTasklet<CsvEmployee, Employee> {
    
    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Override
    protected Class<CsvEmployee> getCsvType() {
        return CsvEmployee.class;
    }
    
    @Override
    protected Employee convertToEntity(CsvEmployee csvEmployee) {
        if (csvEmployee.getEmployeeCode() == null || csvEmployee.getEmployeeCode().trim().isEmpty()) {
            log.warn("Invalid employee code, skipping record");
            return null;
        }
        
        return Employee.builder()
                .employeeCode(csvEmployee.getEmployeeCode())
                .firstName(csvEmployee.getFirstName())
                .lastName(csvEmployee.getLastName())
                .email(csvEmployee.getEmail() != null ? csvEmployee.getEmail().toLowerCase() : null)
                .department(csvEmployee.getDepartment())
                .position(csvEmployee.getPosition())
                .salary(csvEmployee.getSalary())
                .hireDate(csvEmployee.getHireDate())
                .status(csvEmployee.getStatus() != null ? csvEmployee.getStatus() : "ACTIVE")
                .build();
    }
    
    @Override
    protected void saveEntity(Employee employee) {
        Employee existing = employeeMapper.selectByEmployeeCode(employee.getEmployeeCode());
        if (existing != null) {
            employee.setId(existing.getId());
            employeeMapper.update(employee);
            log.debug("Updated employee: {}", employee.getEmployeeCode());
        } else {
            employeeMapper.insert(employee);
            log.debug("Inserted employee: {}", employee.getEmployeeCode());
        }
    }
}