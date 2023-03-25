package com.epam.zmushka.employee.controller;

import com.epam.zmushka.employee.model.Employee;
import com.epam.zmushka.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAll() throws IOException {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<Employee> findById(@PathVariable String id) throws IOException {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping("/employee/{id}")
    public ResponseEntity<String> create(@RequestBody Employee employee, @PathVariable String id) throws IOException {
        return ResponseEntity.ok(employeeService.createEmployee(employee, id));
    }

    @DeleteMapping("/employee/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) throws IOException {
        return ResponseEntity.ok(employeeService.deleteEmployee(id));
    }

    @GetMapping("/employee")
    public ResponseEntity<List<Employee>> searchByField(@RequestParam String fieldName,
                                                        @RequestParam String fieldValue) throws IOException {
        return  ResponseEntity.ok(employeeService.searchByField(fieldName, fieldValue));
    }

    @GetMapping("/employee/aggregation")
    public ResponseEntity<JsonNode> aggregate(@RequestParam String aggField,
                                              @RequestParam String metricType,
                                              @RequestParam String metricField) throws IOException {
        return ResponseEntity.ok(employeeService.aggregateEmployees(aggField, metricType, metricField));
    }
}
