package com.reliaquest.api.service.helper;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmployeeApiHelper {

    @Value("${employee.server.url}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;

    @Retry(name = "employeeServerRetry")
    public ApiResponse<List<Employee>> getAllEmployeesApiCall() {
        ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});
        
        return response.getBody();
    }

    @Retry(name = "employeeServerRetry")
    public ApiResponse<Employee> getEmployeeByIdApiCall(String id) {
        ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                baseUrl + "/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<Employee>>() {});
        
        return response.getBody();
    }

    @Retry(name = "employeeServerRetry")
    public ApiResponse<Employee> createEmployeeApiCall(CreateEmployeeInput input) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        var requestBody = new CreateEmployeeRequest(
                input.getName(), input.getSalary(), input.getAge(), input.getTitle(), input.getEmail());
        
        HttpEntity<CreateEmployeeRequest> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<Employee>>() {});
        
        return response.getBody();
    }

    @Retry(name = "employeeServerRetry")
    public ApiResponse<Boolean> deleteEmployeeApiCall(String employeeName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var requestBody = new DeleteEmployeeRequest(employeeName);
        HttpEntity<DeleteEmployeeRequest> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                baseUrl, HttpMethod.DELETE, entity,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {});
        
        return response.getBody();
    }

    private record CreateEmployeeRequest(String name, Integer salary, Integer age, String title, String email) {}
    private record DeleteEmployeeRequest(String name) {}
}