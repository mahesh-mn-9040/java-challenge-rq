package com.reliaquest.api.service.impl;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Value("${employee.server.url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    @Override
    @Retry(name = "employeeServerRetryForTooManyReq")
    public List<Employee> getAllEmployees() {
        log.debug("Calling getAllEmployees - baseUrl: {}", baseUrl);
        try {
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

            if (response.getBody() != null && response.getBody().data() != null) {
                return response.getBody().data();
            }
            return List.of();
        } catch (Exception e) {
            log.error("Failed to fetch employees from mock API: {}", e.getMessage());
            log.debug("Exception type: {}", e.getClass().getSimpleName());
            throw new RuntimeException("Failed to fetch employees", e);
        }
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
    @Retry(name = "employeeServerRetryForTooManyReq")
    public Optional<Employee> getEmployeeById(String id) {
        log.debug("Calling getEmployeeById for ID: {}", id);
        // Validate UUID format
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        try {
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl + "/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getBody() != null && response.getBody().data() != null) {
                return Optional.of(response.getBody().data());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to fetch employee by ID {}: {}", id, e.getMessage());
            log.debug("Exception type: {}", e.getClass().getSimpleName());
            return Optional.empty();
        }
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
    @Retry(name = "employeeServerRetryForTooManyReq")
    public Employee createEmployee(CreateEmployeeInput input) {
        log.debug("Calling createEmployee for: {}", input.getName());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            var requestBody = new CreateEmployeeRequest(
                    input.getName(), input.getSalary(), input.getAge(), input.getTitle(), input.getEmail());

            HttpEntity<CreateEmployeeRequest> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getBody() != null && response.getBody().data() != null) {
                return response.getBody().data();
            }

            throw new RuntimeException("Failed to create employee");
        } catch (Exception e) {
            log.error("Failed to create employee: {}", e.getMessage());
            log.debug("Exception type: {}", e.getClass().getSimpleName());
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    @Override
    public String deleteEmployeeById(String id) {
        Optional<Employee> employee = getEmployeeById(id);
        if (employee.isEmpty()) {
            throw new RuntimeException("Employee not found");
        }

        String employeeName = employee.get().getName();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            var requestBody = new DeleteEmployeeRequest(employeeName);
            HttpEntity<DeleteEmployeeRequest> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.DELETE, entity, new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

            if (response.getBody() != null
                    && Boolean.TRUE.equals(response.getBody().data())) {
                return employeeName;
            }

            throw new RuntimeException("Failed to delete employee");
        } catch (Exception e) {
            log.error("Failed to delete employee: {}", e.getMessage());
            throw new RuntimeException("Failed to delete employee", e);
        }
    }

    private record CreateEmployeeRequest(String name, Integer salary, Integer age, String title, String email) {}

    private record DeleteEmployeeRequest(String name) {}
}
