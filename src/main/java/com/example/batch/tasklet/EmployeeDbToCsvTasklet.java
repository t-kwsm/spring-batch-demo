package com.example.batch.tasklet;

import com.example.batch.dto.CsvEmployee;
import com.example.batch.entity.Employee;
import com.example.batch.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 従業員DB→CSVタスクレット
 */
@Slf4j
@Component
public class EmployeeDbToCsvTasklet extends DbToCsvTasklet<Employee, CsvEmployee> {
    
    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Override
    protected List<Employee> loadEntities() {
        return employeeMapper.selectAll();
    }
    
    @Override
    protected CsvEmployee convertToCsvDto(Employee employee) {
        log.debug("Converting employee to CSV: {}", employee.getEmployeeCode());
        
        return CsvEmployee.builder()
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .build();
    }
    
    @Override
    protected String getCsvHeader() {
        return "employee_code,first_name,last_name,email,department,position,salary,hire_date,status";
    }
}