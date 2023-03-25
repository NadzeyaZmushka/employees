package com.epam.zmushka.employee.service.impl;

import com.epam.zmushka.employee.model.Employee;
import com.epam.zmushka.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LowLevelEmployeeService implements EmployeeService{

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    private static final String SEARCH_ENDPOINT = "/employees/_search";
    private static final String CRUDE_ENDPOINT = "/employees/_doc/";

    public LowLevelEmployeeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
    }

    private static final String index = "employees";
    @Override
    public List<Employee> findAll() throws IOException {
        Request request = new Request("GET", SEARCH_ENDPOINT);
        String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
        return getResult(objectMapper.readTree(responseBody));
    }

    @Override
    public Employee getEmployeeById(String id) throws IOException {
        Request request = new Request("GET", CRUDE_ENDPOINT + id);
        String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode source = jsonNode.findValue("_source");
        Employee employee = objectMapper.convertValue(source, Employee.class);
        return employee;
    }

    @Override
    public String deleteEmployee(String id) throws IOException {
        Request request = new Request("DELETE", CRUDE_ENDPOINT + id);
        String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
        return objectMapper.readTree(responseBody).get("result").asText();
    }

    @Override
    public String createEmployee(Employee employee, String id) throws IOException {
        Request request = new Request("POST", CRUDE_ENDPOINT + id);
        request.setJsonEntity(objectMapper.convertValue(employee, JsonNode.class).toString());
        String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
        return objectMapper.readTree(responseBody).get("result").asText();
    }

    @Override
    public List<Employee> searchByField(String fieldName, String fieldValue) throws IOException {
        Request request = new Request("POST", SEARCH_ENDPOINT);
        request.setJsonEntity(String.format("{\n" +
                "    \"query\": {\n" +
                "        \"match\": {\n" +
                "            \"%s\": \"%s\"\n" +
                "        }\n" +
                "    }\n" +
                "}", fieldName, fieldValue));
        String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
        return getResult(objectMapper.readTree(responseBody));
    }

    @Override
    public JsonNode aggregateEmployees(String aggField, String metricType, String metricField) throws IOException {
        Request request = new Request("POST", SEARCH_ENDPOINT);
        request.setJsonEntity(String.format("{\n" +
                "    \"size\": 0,\n" +
                "    \"aggs\": {\n" +
                "        \"%s\": {\n" +
                "            \"terms\": {\n" +
                "                \"field\": \"%s\"\n" +
                "            },\n" +
                "            \"aggs\": {\n" +
                "                \"rating_stats\": {\n" +
                "                    \"%s\": {\n" +
                "                        \"field\": \"%s\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}", aggField, aggField, metricType, metricField));
        String responseBody = EntityUtils.toString(restClient.performRequest(request).getEntity());
        return objectMapper.readTree(responseBody).get("aggregations");
    }

    private List<Employee> getResult(JsonNode jsonNode) {
        List<Employee> employees = new ArrayList<>();
        JsonNode jsonNode1 = jsonNode.get("hits").get("hits");
        List<JsonNode> source = jsonNode1.findValues("_source");
        source.forEach(node -> {
            Employee employee = objectMapper.convertValue(node, Employee.class);
            employees.add(employee);
        });
        return employees;
    }
}
