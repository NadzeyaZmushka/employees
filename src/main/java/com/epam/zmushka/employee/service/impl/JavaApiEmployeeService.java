package com.epam.zmushka.employee.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.epam.zmushka.employee.model.Employee;
import com.epam.zmushka.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class JavaApiEmployeeService implements EmployeeService {

    private final ObjectMapper objectMapper;
    private final ElasticsearchClient client;
    private static final String INDEX = "employees";

    @Autowired
    public JavaApiEmployeeService(ObjectMapper objectMapper) {
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200));
        Header[] defaultHeaders = new Header[]{new BasicHeader("Content-type", "application/json")};
        builder.setDefaultHeaders(defaultHeaders);
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.addInterceptorLast(
                    (HttpResponseInterceptor)
                            (response, context) ->
                                    response.addHeader("X-Elastic-Product", "Elasticsearch"));
            return httpClientBuilder;
        });
        RestClient client = builder.build();

        ElasticsearchTransport transport = new RestClientTransport(client, new JacksonJsonpMapper());
        this.objectMapper = objectMapper;
        this.client = new ElasticsearchClient(transport);
    }

    @Override
    public List<Employee> findAll() throws IOException {
        Query matchAllQuery = new MatchAllQuery.Builder().build()._toQuery();
        SearchResponse<Employee> response = client.search(s -> s
                .index(INDEX).query(matchAllQuery), Employee.class);
        return getResult(response);
    }

    @Override
    public Employee getEmployeeById(String id) throws IOException {
        GetResponse<Employee> response = client.get(g -> g.index(INDEX).id(id), Employee.class);
        return response.source();
    }

    @Override
    public String deleteEmployee(String id) throws IOException {
        DeleteResponse response = client.delete(d -> d
                .index(INDEX).id(id));
        return response.result().toString();
    }

    @Override
    public String createEmployee(Employee employee, String id) throws IOException {
        IndexResponse response = client.index(i -> i
                .index(INDEX).id(id).document(employee));
        return response.result().toString();
    }

    @Override
    public List<Employee> searchByField(String fieldName, String fieldValue) throws IOException {
        SearchResponse<Employee> response = client.search(s -> s
                .index(INDEX).query(q -> q
                        .match(t -> t
                                .field(fieldName)
                                .query(fieldValue))), Employee.class);
        return getResult(response);
    }

    @Override
    public JsonNode aggregateEmployees(String aggField, String metricType, String metricField) throws IOException {
        Reader jsonAgg = new StringReader(String.format("{\n" +
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
        SearchResponse<Employee> response = client.search(s -> s
                .index(INDEX).withJson(jsonAgg), Employee.class);
        return objectMapper.readTree(response.aggregations().get(aggField).toString().replace("Aggregate: ", ""));
    }

    private List<Employee> getResult(SearchResponse<Employee> response) {
        List<Hit<Employee>> hits = response.hits().hits();
        List<Employee> employees = new ArrayList<>();
        hits.forEach(hit -> employees.add(hit.source()));
        return employees;
    }
}
