package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.impl.EmployeeServiceImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private Employee testEmployee3;
    private List<Employee> testEmployees;

    @BeforeEach
    void setUp() {
        testEmployee1 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee X")
                .salary(75000)
                .age(30)
                .title("Developer")
                .email("x@company.com")
                .build();

        testEmployee2 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee Y")
                .salary(85000)
                .age(28)
                .title("Senior Developer")
                .email("y@company.com")
                .build();

        testEmployee3 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee Z")
                .salary(95000)
                .age(35)
                .title("Tech Lead")
                .email("z@company.com")
                .build();

        testEmployees = Arrays.asList(testEmployee1, testEmployee2, testEmployee3);
    }

    @Test
    void getAllEmployees_Success() throws Exception {
        // Given
        ApiResponse<List<Employee>> apiResponse = ApiResponse.handledWith(testEmployees);
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        CompletableFuture<List<Employee>> result = employeeService.getAllEmployees();

        // Then
        List<Employee> employees = result.get();
        assertEquals(3, employees.size());
        assertEquals("Employee X", employees.get(0).getName());
        assertEquals("Employee Y", employees.get(1).getName());
        assertEquals("Employee Z", employees.get(2).getName());
    }

    @Test
    void getEmployeesByNameSearch_Success() throws Exception {
        // Given
        ApiResponse<List<Employee>> apiResponse = ApiResponse.handledWith(testEmployees);
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        CompletableFuture<List<Employee>> result = employeeService.getEmployeesByNameSearch("Employee");

        // Then
        List<Employee> employees = result.get();
        assertEquals(3, employees.size()); // All employees contain "Employee"
        assertTrue(employees.stream().anyMatch(e -> e.getName().contains("Employee")));
    }

    @Test
    void getEmployeesByNameSearch_InvalidSearchString() throws Exception {
        // When
        CompletableFuture<List<Employee>> result = employeeService.getEmployeesByNameSearch("X@123");

        // Then
        assertTrue(result.isCompletedExceptionally());
        assertThrows(Exception.class, () -> result.get());
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        // Given
        String employeeId = testEmployee1.getId().toString();
        ApiResponse<Employee> apiResponse = ApiResponse.handledWith(testEmployee1);
        ResponseEntity<ApiResponse<Employee>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        CompletableFuture<Optional<Employee>> result = employeeService.getEmployeeById(employeeId);

        // Then
        Optional<Employee> employee = result.get();
        assertTrue(employee.isPresent());
        assertEquals("Employee X", employee.get().getName());
        assertEquals(75000, employee.get().getSalary());
    }

    @Test
    void getHighestSalaryOfEmployees_Success() throws Exception {
        // Given
        ApiResponse<List<Employee>> apiResponse = ApiResponse.handledWith(testEmployees);
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        CompletableFuture<Integer> result = employeeService.getHighestSalaryOfEmployees();

        // Then
        Integer highestSalary = result.get();
        assertEquals(95000, highestSalary); // Employee Z's salary
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() throws Exception {
        // Given
        ApiResponse<List<Employee>> apiResponse = ApiResponse.handledWith(testEmployees);
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        CompletableFuture<List<String>> result = employeeService.getTopTenHighestEarningEmployeeNames();

        // Then
        List<String> names = result.get();
        assertEquals(3, names.size());
        assertEquals("Employee Z", names.get(0)); // Highest salary first
        assertEquals("Employee Y", names.get(1));
        assertEquals("Employee X", names.get(2));
    }

    @Test
    void createEmployee_Success() throws Exception {
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
        ResponseEntity<ApiResponse<Employee>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.CREATED);

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        CompletableFuture<Employee> result = employeeService.createEmployee(input);

        // Then
        Employee employee = result.get();
        assertEquals("Employee W", employee.getName());
        assertEquals(80000, employee.getSalary());
        assertEquals(25, employee.getAge());
        assertEquals("Developer", employee.getTitle());
    }
}
