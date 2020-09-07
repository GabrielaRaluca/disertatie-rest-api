package com.dissertation.restapi.service;

import com.dissertation.restapi.model.Label;
import com.dissertation.restapi.model.User;
import com.dissertation.restapi.model.UserLabelScore;
import com.dissertation.restapi.repository.UserLabelScoreRepository;
import com.dissertation.restapi.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
public class AnalysisService {
    @Value("${analyzer.sentimentUrl}") String analyzerApi;

    ObjectMapper objectMapper = new ObjectMapper();
    RestTemplate restTemplate = new RestTemplate();

    private final UserLabelScoreRepository userLabelScoreRepository;
    private final UserRepository userRepository;

    public AnalysisService(UserLabelScoreRepository userLabelScoreRepository, UserRepository userRepository) {
        this.userLabelScoreRepository = userLabelScoreRepository;
        this.userRepository = userRepository;
    }

    public void calculatePreferences(User user, float postScore, List<Label> labelList) {
        labelList.stream().forEach(label -> {
            userLabelScoreRepository.save(UserLabelScore.builder().userId(user)
                    .label(label.getDescription())
            .score(label.getScore() * postScore).build());
        });
    }

    public void getUserLabelScore() throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        ArrayNode userLabelScores = objectMapper.createArrayNode();
        Iterator<UserLabelScore> iterator = userLabelScoreRepository.findAll().iterator();
        while(iterator.hasNext()) {
            UserLabelScore userLabelScore = iterator.next();
            ObjectNode userLabelScoreObject = objectMapper.createObjectNode();
            userLabelScoreObject.put("userId", userLabelScore.getUserId().getId());
            userLabelScoreObject.put("label", userLabelScore.getLabel());
            userLabelScoreObject.put("score", userLabelScore.getScore());

            userLabelScores.add(userLabelScoreObject);
        }

        getSimilarities(userLabelScores);
    }

    public void getSimilarities(ArrayNode reqBody) throws IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode body = objectMapper.createObjectNode();
        body.set("scores", reqBody);

        HttpEntity<String> request = new HttpEntity(body, httpHeaders);
        String results = restTemplate.postForObject(analyzerApi + "/similarity", request, String.class);

        JsonNode response = objectMapper.readTree(results);
        Map<String, Object> treeMap = objectMapper.readValue(String.valueOf(response),
                new TypeReference<Map<String, Object>>() {});
        Iterator it = treeMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            Optional<User> optionalUser = userRepository.findById(Long.parseLong(entry.getKey().toString()));
            if(optionalUser.isPresent()) {
                User user = optionalUser.get();
                LinkedHashMap top = (LinkedHashMap)entry.getValue();
                Iterator fit = top.entrySet().iterator();
                while(fit.hasNext()) {
                    Map.Entry fEntry = (Map.Entry)fit.next();
                    Optional<User> optionalFollowing = userRepository
                            .findById(Long.parseLong(fEntry.getValue().toString()));
                    if(optionalFollowing.isPresent()) {
                        User following = optionalFollowing.get();
                        if (!user.getFollowing().contains(following))
                            user.getFollowing().add(following);
                    }
                }
                userRepository.save(user);
            }
        }
    }
}
