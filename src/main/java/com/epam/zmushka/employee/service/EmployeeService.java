package com.epam.zmushka.employee.service;

import com.epam.zmushka.employee.model.Employee;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;

public interface EmployeeService {

    List<Employee> findAll() throws IOException;

    Employee getEmployeeById(String id) throws IOException;

    String deleteEmployee(String id) throws IOException;

    String createEmployee(Employee employee, String id) throws IOException;

    List<Employee> searchByField(String fieldName, String fieldValue) throws IOException;

    JsonNode aggregateEmployees(String aggField, String metricType, String metricField) throws IOException;


}
