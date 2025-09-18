package com.reliaquest.api.service.impl;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import com.reliaquest.api.service.helper.EmployeeApiHelper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeApiHelper employeeApiHelper;

    @Override
    @Cacheable("employees")
    public List<Employee> getAllEmployees() {
        log.info("Fetching employees from API (cache miss)");
        ApiResponse<List<Employee>> response = employeeApiHelper.getAllEmployeesApiCall();
        return response != null && response.data() != null ? response.data() : List.of();
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        // Validate search string
        if (searchString == null || searchString.trim().isEmpty()) {
            throw new IllegalArgumentException("Search string cannot be null or empty");
        }
        
        String trimmedSearchString = searchString.trim();
        if (!trimmedSearchString.matches("^[a-zA-Z]+$")) {
            throw new IllegalArgumentException("Search string must contain only letters");
        }
        
        List<Employee> allEmployees = getAllEmployees();
        return allEmployees.stream()
                .filter(employee -> employee.getName() != null
                        && employee.getName().toLowerCase().contains(trimmedSearchString.toLowerCase()))
                .toList();
    }

    @Override
    public Optional<Employee> getEmployeeById(String id) {
        // Validate UUID format
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        ApiResponse<Employee> response = employeeApiHelper.getEmployeeByIdApiCall(id);
        return Optional.ofNullable(response != null ? response.data() : null);
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .filter(employee -> employee.getSalary() != null)
                .map(Employee::getSalary)
                .max(Integer::compareTo)
                .orElse(0);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .filter(employee -> employee.getSalary() != null && employee.getName() != null)
                .sorted((e1, e2) -> e2.getSalary().compareTo(e1.getSalary()))
                .limit(10)
                .map(Employee::getName)
                .toList();
    }

    @Override
    public Employee createEmployee(CreateEmployeeInput input) {
        ApiResponse<Employee> response = employeeApiHelper.createEmployeeApiCall(input);
        
        if (response != null && response.data() != null) {
            return response.data();
        } else {
            throw new RuntimeException("Employee creation failed");
        }
    }

    @Override
    public String deleteEmployeeById(String id) {
        Optional<Employee> employee = getEmployeeById(id);
        if (employee.isEmpty()) {
            throw new RuntimeException("Employee not found");
        }

        String employeeName = employee.get().getName();
        ApiResponse<Boolean> response = employeeApiHelper.deleteEmployeeApiCall(employeeName);
        
        if (response != null && Boolean.TRUE.equals(response.data())) {
            return employeeName;
        } else {
            throw new RuntimeException("Failed to delete employee: " + employeeName);
        }
    }
}