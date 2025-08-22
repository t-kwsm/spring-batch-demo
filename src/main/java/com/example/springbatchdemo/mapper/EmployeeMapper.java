package com.example.springbatchdemo.mapper;

import com.example.springbatchdemo.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {
    
    @Select("SELECT employee_id as employeeId, first_name as firstName, " +
            "last_name as lastName, email, department, salary, hire_date as hireDate " +
            "FROM employees")
    List<Employee> findAll();
}