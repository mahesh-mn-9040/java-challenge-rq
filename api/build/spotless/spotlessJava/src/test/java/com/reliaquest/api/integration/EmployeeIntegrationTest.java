package com.reliaquest.api.integration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.ApiApplication;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void testGetAllEmployees_Integration() throws Exception {
        // Given
        List<Employee> mockEmployees = Arrays.asList(
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Employee X")
                        .salary(75000)
                        .age(30)
                        .title("Developer")
                        .email("x@company.com")
                        .build(),
                Employee.builder()
                        .id(UUID.randomUUID())
                        .name("Employee Y")
                        .salary(85000)
                        .age(28)
                        .title("Senior Developer")
                        .email("y@company.com")
                        .build());

        when(employeeService.getAllEmployees()).thenReturn(CompletableFuture.completedFuture(mockEmployees));

        // When & Then
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employee_name").value("Employee X"))
                .andExpect(jsonPath("$[1].employee_name").value("Employee Y"));
    }

    @Test
    void testSearchEmployees_Integration() throws Exception {
        // Given
        List<Employee> filteredEmployees = Arrays.asList(Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee X")
                .salary(75000)
                .age(30)
                .title("Developer")
                .email("x@company.com")
                .build());

        when(employeeService.getEmployeesByNameSearch("X"))
                .thenReturn(CompletableFuture.completedFuture(filteredEmployees));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/X"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employee_name").value("Employee X"));
    }

    @Test
    void testGetEmployeeById_Integration() throws Exception {
        // Given
        UUID employeeId = UUID.randomUUID();
        Employee mockEmployee = Employee.builder()
                .id(employeeId)
                .name("Employee Z")
                .salary(90000)
                .age(33)
                .title("Tech Lead")
                .email("z@company.com")
                .build();

        when(employeeService.getEmployeeById(employeeId.toString()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(mockEmployee)));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employee_name").value("Employee Z"))
                .andExpect(jsonPath("$.employee_salary").value(90000));
    }

    @Test
    void testGetHighestSalary_Integration() throws Exception {
        // Given
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(CompletableFuture.completedFuture(100000));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("100000"));
    }

    @Test
    void testCreateEmployee_Integration() throws Exception {
        // Given
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("Employee Z");
        input.setSalary(80000);
        input.setAge(25);
        input.setTitle("Developer");
        input.setEmail("z@company.com");

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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employee_name").value("Employee Z"))
                .andExpect(jsonPath("$.employee_salary").value(80000));
    }

    @Test
    void testValidationErrors_Integration() throws Exception {

        CreateEmployeeInput invalidInput = new CreateEmployeeInput();
        invalidInput.setName(""); // blank name
        invalidInput.setSalary(-1000); // negative salary
        invalidInput.setAge(10); // age below minimum
        invalidInput.setTitle(""); // blank title

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testSearchValidation_Integration() throws Exception {
        // Test search validation
        mockMvc.perform(get("/api/v1/employee/search/test@123")).andExpect(status().is5xxServerError());

        mockMvc.perform(get("/api/v1/employee/search/")).andExpect(status().is4xxClientError());
    }
}
