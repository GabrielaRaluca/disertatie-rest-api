package com.dissertation.restapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;


@Service
public class SentimentAnalysis {
    @Value("${analyzer.sentimentUrl}") String analyzerApi;

    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();

    public float analyze(String description) throws IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("description", description);

        HttpEntity<String> request = new HttpEntity(body, httpHeaders);
        String results = restTemplate.postForObject(analyzerApi, request, String.class);

        JsonNode response = objectMapper.readTree(results);
        float score = (float)response.get("score").asDouble();

        return score;
    }
}
