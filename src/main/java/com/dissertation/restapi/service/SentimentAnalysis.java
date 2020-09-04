package com.dissertation.restapi.service;

import com.dissertation.restapi.repository.UserLabelScoreRepository;
import com.dissertation.restapi.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final UserLabelScoreRepository userLabelScoreRepository;

    public SentimentAnalysis(UserRepository userRepository, UserLabelScoreRepository userLabelScoreRepository) {
        this.userRepository = userRepository;
        this.userLabelScoreRepository = userLabelScoreRepository;
    }

    public float analyze(String description) throws IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("description", description);

        HttpEntity<String> request = new HttpEntity(body, httpHeaders);
        String results = restTemplate.postForObject(analyzerApi + "/sentiment", request, String.class);

        JsonNode response = objectMapper.readTree(results);
        float score = (float)response.get("score").asDouble();

        return score;
    }

}
