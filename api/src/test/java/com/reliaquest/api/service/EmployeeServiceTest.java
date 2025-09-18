package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.helper.EmployeeApiHelper;
import com.reliaquest.api.service.impl.EmployeeServiceImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeApiHelper employeeApiHelper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee testEmployeeX;
    private Employee testEmployeeY;
    private Employee testEmployeeZ;
    private List<Employee> testEmployees;

    @BeforeEach
    void setUp() {
        testEmployeeX = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee X")
                .salary(75000)
                .age(30)
                .title("Developer")
                .email("x@company.com")
                .build();

        testEmployeeY = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee Y")
                .salary(85000)
                .age(28)
                .title("Senior Developer")
                .email("y@company.com")
                .build();

        testEmployeeZ = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee Z")
                .salary(95000)
                .age(35)
                .title("Tech Lead")
                .email("z@company.com")
                .build();

        testEmployees = Arrays.asList(testEmployeeX, testEmployeeY, testEmployeeZ);
    }

    @Test
    void getAllEmployees_Success() {
        // Given
        ApiResponse<List<Employee>> apiResponse = ApiResponse.handledWith(testEmployees);
        when(employeeApiHelper.getAllEmployeesApiCall()).thenReturn(apiResponse);

        // When
        List<Employee> result = employeeService.getAllEmployees();

        // Then
        assertEquals(3, result.size());
        assertEquals("Employee X", result.get(0).getName());
        assertEquals("Employee Y", result.get(1).getName());
        assertEquals("Employee Z", result.get(2).getName());
    }

    @Test
    void getAllEmployees_EmptyResponse() {
        // Given
        ApiResponse<List<Employee>> apiResponse = ApiResponse.handledWith(null);
        when(employeeApiHelper.getAllEmployeesApiCall()).thenReturn(apiResponse);

        // When
        List<Employee> result = employeeService.getAllEmployees();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeesByNameSearch_Success() {
        // Given
        ApiResponse<List<Employee>> apiResponse = ApiResponse.handledWith(testEmployees);
        when(employeeApiHelper.getAllEmployeesApiCall()).thenReturn(apiResponse);

        // When
        List<Employee> result = employeeService.getEmployeesByNameSearch("Employee");

        // Then
        assertEquals(3, result.size()); // All employees contain "Employee"
        assertTrue(result.stream().allMatch(e -> e.getName().contains("Employee")));
    }

    @Test
    void getEmployeesByNameSearch_InvalidSearchString() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> employeeService.getEmployeesByNameSearch("X@123"));
        
        assertEquals("Search string must contain only letters", exception.getMessage());
        verifyNoInteractions(employeeApiHelper);
    }

    @Test
    void getEmployeeById_Success() {
        // Given
        String employeeId = testEmployeeX.getId().toString();
        ApiResponse<Employee> apiResponse = ApiResponse.handledWith(testEmployeeX);
        when(employeeApiHelper.getEmployeeByIdApiCall(employeeId)).thenReturn(apiResponse);

        // When
        Optional<Employee> result = employeeService.getEmployeeById(employeeId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Employee X", result.get().getName());
        assertEquals(75000, result.get().getSalary());
    }

    @Test
    void getEmployeeById_InvalidUUID() {
        // When
        Optional<Employee> result = employeeService.getEmployeeById("invalid-uuid");

        // Then
        assertFalse(result.isPresent());
        verifyNoInteractions(employeeApiHelper);
    }

    @Test
    void getHighestSalaryOfEmployees_Success() {
        // Given
        ApiResponse<List<Employee>> apiResponse = ApiResponse.handledWith(testEmployees);
        when(employeeApiHelper.getAllEmployeesApiCall()).thenReturn(apiResponse);

        // When
        Integer result = employeeService.getHighestSalaryOfEmployees();

        // Then
        assertEquals(95000, result); // Employee Z's salary
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() {
        // Given
        ApiResponse<List<Employee>> apiResponse = ApiResponse.handledWith(testEmployees);
        when(employeeApiHelper.getAllEmployeesApiCall()).thenReturn(apiResponse);

        // When
        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(3, result.size());
        assertEquals("Employee Z", result.get(0)); // Highest salary first
        assertEquals("Employee Y", result.get(1));
        assertEquals("Employee X", result.get(2));
    }

    @Test
    void createEmployee_Success() {
        // Given
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("Employee W");
        input.setSalary(80000);
        input.setAge(25);
        input.setTitle("Developer");
        input.setEmail("w@company.com");

        Employee createdEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee W")
                .salary(80000)
                .age(25)
                .title("Developer")
                .email("w@company.com")
                .build();

        ApiResponse<Employee> apiResponse = ApiResponse.handledWith(createdEmployee);
        when(employeeApiHelper.createEmployeeApiCall(input)).thenReturn(apiResponse);

        // When
        Employee result = employeeService.createEmployee(input);

        // Then
        assertEquals("Employee W", result.getName());
        assertEquals(80000, result.getSalary());
        assertEquals(25, result.getAge());
        assertEquals("Developer", result.getTitle());
    }

    @Test
    void deleteEmployeeById_Success() {
        // Given
        String employeeId = testEmployeeX.getId().toString();
        ApiResponse<Employee> getApiResponse = ApiResponse.handledWith(testEmployeeX);
        ApiResponse<Boolean> deleteApiResponse = ApiResponse.handledWith(true);
        
        when(employeeApiHelper.getEmployeeByIdApiCall(employeeId)).thenReturn(getApiResponse);
        when(employeeApiHelper.deleteEmployeeApiCall("Employee X")).thenReturn(deleteApiResponse);

        // When
        String result = employeeService.deleteEmployeeById(employeeId);

        // Then
        assertEquals("Employee X", result);
    }

    @Test
    void deleteEmployeeById_EmployeeNotFound() {
        // Given
        String employeeId = UUID.randomUUID().toString();
        ApiResponse<Employee> apiResponse = ApiResponse.handledWith(null);
        when(employeeApiHelper.getEmployeeByIdApiCall(employeeId)).thenReturn(apiResponse);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> employeeService.deleteEmployeeById(employeeId));
        
        assertEquals("Employee not found", exception.getMessage());
    }
}