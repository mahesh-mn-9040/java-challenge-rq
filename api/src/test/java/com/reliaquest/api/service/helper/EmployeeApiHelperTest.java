package com.reliaquest.api.service.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EmployeeApiHelperTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeApiHelper employeeApiHelper;

    private Employee testEmployeeX;
    private Employee testEmployeeY;
    private List<Employee> testEmployees;
    private final String baseUrl = "http://localhost:8112/api/v1/employee";

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(employeeApiHelper, "baseUrl", baseUrl);

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

        testEmployees = Arrays.asList(testEmployeeX, testEmployeeY);
    }

    @Test
    void getAllEmployeesApiCall_Success() {
        // Given
        ApiResponse<List<Employee>> apiResponse = ApiResponse.handledWith(testEmployees);
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(baseUrl),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        ApiResponse<List<Employee>> result = employeeApiHelper.getAllEmployeesApiCall();

        // Then
        assertNotNull(result);
        assertEquals(2, result.data().size());
        assertEquals("Employee X", result.data().get(0).getName());
        assertEquals("Employee Y", result.data().get(1).getName());
    }

    @Test
    void getAllEmployeesApiCall_TooManyRequests() {
        // Given
        when(restTemplate.exchange(
                eq(baseUrl),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When & Then
        assertThrows(HttpClientErrorException.class, 
            () -> employeeApiHelper.getAllEmployeesApiCall());
    }

    @Test
    void getEmployeeByIdApiCall_Success() {
        // Given
        String employeeId = testEmployeeX.getId().toString();
        ApiResponse<Employee> apiResponse = ApiResponse.handledWith(testEmployeeX);
        ResponseEntity<ApiResponse<Employee>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(baseUrl + "/" + employeeId),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        ApiResponse<Employee> result = employeeApiHelper.getEmployeeByIdApiCall(employeeId);

        // Then
        assertNotNull(result);
        assertEquals("Employee X", result.data().getName());
        assertEquals(75000, result.data().getSalary());
    }

    @Test
    void getEmployeeByIdApiCall_NotFound() {
        // Given
        String employeeId = UUID.randomUUID().toString();
        when(restTemplate.exchange(
                eq(baseUrl + "/" + employeeId),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When & Then
        assertThrows(HttpClientErrorException.class, 
            () -> employeeApiHelper.getEmployeeByIdApiCall(employeeId));
    }

    @Test
    void createEmployeeApiCall_Success() {
        // Given
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("Employee Z");
        input.setSalary(90000);
        input.setAge(32);
        input.setTitle("Tech Lead");
        input.setEmail("z@company.com");

        Employee createdEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .name("Employee Z")
                .salary(90000)
                .age(32)
                .title("Tech Lead")
                .email("z@company.com")
                .build();

        ApiResponse<Employee> apiResponse = ApiResponse.handledWith(createdEmployee);
        ResponseEntity<ApiResponse<Employee>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(baseUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        ApiResponse<Employee> result = employeeApiHelper.createEmployeeApiCall(input);

        // Then
        assertNotNull(result);
        assertEquals("Employee Z", result.data().getName());
        assertEquals(90000, result.data().getSalary());
        assertEquals("z@company.com", result.data().getEmail());
    }

    @Test
    void createEmployeeApiCall_TooManyRequests() {
        // Given
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("Employee Z");
        input.setSalary(90000);
        input.setAge(32);
        input.setTitle("Tech Lead");
        input.setEmail("z@company.com");

        when(restTemplate.exchange(
                eq(baseUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When & Then
        assertThrows(HttpClientErrorException.class, 
            () -> employeeApiHelper.createEmployeeApiCall(input));
    }

    @Test
    void deleteEmployeeApiCall_Success() {
        // Given
        String employeeName = "Employee X";
        ApiResponse<Boolean> apiResponse = ApiResponse.handledWith(true);
        ResponseEntity<ApiResponse<Boolean>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(baseUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        ApiResponse<Boolean> result = employeeApiHelper.deleteEmployeeApiCall(employeeName);

        // Then
        assertNotNull(result);
        assertTrue(result.data());
    }

    @Test
    void deleteEmployeeApiCall_TooManyRequests() {
        // Given
        String employeeName = "Employee X";
        when(restTemplate.exchange(
                eq(baseUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When & Then
        assertThrows(HttpClientErrorException.class, 
            () -> employeeApiHelper.deleteEmployeeApiCall(employeeName));
    }

    @Test
    void deleteEmployeeApiCall_Failed() {
        // Given
        String employeeName = "Employee X";
        ApiResponse<Boolean> apiResponse = ApiResponse.handledWith(false);
        ResponseEntity<ApiResponse<Boolean>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(baseUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        ApiResponse<Boolean> result = employeeApiHelper.deleteEmployeeApiCall(employeeName);

        // Then
        assertNotNull(result);
        assertFalse(result.data());
    }
}