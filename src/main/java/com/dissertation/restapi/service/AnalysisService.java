package com.dissertation.restapi.service;

import com.dissertation.restapi.model.Label;
import com.dissertation.restapi.model.User;
import com.dissertation.restapi.model.UserLabelScore;
import com.dissertation.restapi.repository.UserLabelScoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalysisService {
    private final UserLabelScoreRepository userLabelScoreRepository;

    public AnalysisService(UserLabelScoreRepository userLabelScoreRepository) {
        this.userLabelScoreRepository = userLabelScoreRepository;
    }

    public void calculatePreferences(User user, float postScore, List<Label> labelList) {
        labelList.stream().forEach(label -> {
            userLabelScoreRepository.save(UserLabelScore.builder().userId(user)
                    .label(label.getDescription())
            .score( label.getScore() * postScore).build());
        });
    }
}
