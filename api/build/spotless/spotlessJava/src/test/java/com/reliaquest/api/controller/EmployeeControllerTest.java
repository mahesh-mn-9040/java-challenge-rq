package com.reliaquest.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee testEmployee1;
    private Employee testEmployee2;
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

        testEmployees = Arrays.asList(testEmployee1, testEmployee2);
    }

    @Test
    void getAllEmployees_Success() throws Exception {
        // Given
        when(employeeService.getAllEmployees()).thenReturn(CompletableFuture.completedFuture(testEmployees));

        // When & Then
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employee_name").value("Employee X"))
                .andExpect(jsonPath("$[1].employee_name").value("Employee Y"));
    }

    @Test
    void getAllEmployees_ServiceException() throws Exception {
        // Given
        when(employeeService.getAllEmployees())
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service error")));

        // When & Then
        mockMvc.perform(get("/api/v1/employee")).andExpect(status().isInternalServerError());
    }

    @Test
    void getEmployeesByNameSearch_Success() throws Exception {
        // Given
        List<Employee> filteredEmployees = Arrays.asList(testEmployee1);
        when(employeeService.getEmployeesByNameSearch("X"))
                .thenReturn(CompletableFuture.completedFuture(filteredEmployees));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/X"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employee_name").value("Employee X"));
    }

    @Test
    void getEmployeesByNameSearch_InvalidSearchString() throws Exception {
        // Given
        when(employeeService.getEmployeesByNameSearch("X@123"))
                .thenReturn(CompletableFuture.failedFuture(
                        new IllegalArgumentException("Search string must contain only letters")));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/X@123")).andExpect(status().is5xxServerError());
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        // Given
        String employeeId = testEmployee1.getId().toString();
        when(employeeService.getEmployeeById(employeeId))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(testEmployee1)));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name").value("Employee X"))
                .andExpect(jsonPath("$.employee_salary").value(75000));
    }

    @Test
    void getEmployeeById_NotFound() throws Exception {
        // Given
        String employeeId = UUID.randomUUID().toString();
        when(employeeService.getEmployeeById(employeeId))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", employeeId)).andExpect(status().isNotFound());
    }

    @Test
    void getHighestSalaryOfEmployees_Success() throws Exception {
        // Given
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(CompletableFuture.completedFuture(85000));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("85000"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() throws Exception {
        // Given
        List<String> topNames = Arrays.asList("Employee Y", "Employee X");
        when(employeeService.getTopTenHighestEarningEmployeeNames())
                .thenReturn(CompletableFuture.completedFuture(topNames));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Employee Y"))
                .andExpect(jsonPath("$[1]").value("Employee X"));
    }

    @Test
    void createEmployee_Success() throws Exception {
        // Given
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("Employee Z");
        input.setSalary(80000);
        input.setAge(25);
        input.setTitle("Developer");

        Employee createdEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee Z")
                .salary(80000)
                .age(25)
                .title("Developer")
                .email("z@company.com")
                .build();

        when(employeeService.createEmployee(any(CreateEmployeeInput.class)))
                .thenReturn(CompletableFuture.completedFuture(createdEmployee));

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employee_name").value("Employee Z"))
                .andExpect(jsonPath("$.employee_salary").value(80000));
    }

    @Test
    void createEmployee_ServiceException() throws Exception {
        // Given
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("Employee Z");
        input.setSalary(80000);
        input.setAge(25);
        input.setTitle("Developer");

        when(employeeService.createEmployee(any(CreateEmployeeInput.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Creation failed")));

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteEmployeeById_Success() throws Exception {
        // Given
        String employeeId = UUID.randomUUID().toString();
        when(employeeService.deleteEmployeeById(employeeId))
                .thenReturn(CompletableFuture.completedFuture("Employee X"));

        // When & Then
        mockMvc.perform(delete("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee X"));
    }

    @Test
    void deleteEmployeeById_NotFound() throws Exception {
        // Given
        String employeeId = UUID.randomUUID().toString();
        when(employeeService.deleteEmployeeById(employeeId))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Employee not found")));

        // When & Then
        mockMvc.perform(delete("/api/v1/employee/{id}", employeeId)).andExpect(status().is5xxServerError());
    }
}
