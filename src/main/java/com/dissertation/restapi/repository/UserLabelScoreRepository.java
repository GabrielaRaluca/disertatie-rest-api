package com.dissertation.restapi.repository;

import com.dissertation.restapi.model.UserLabelScore;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserLabelScoreRepository extends CrudRepository<UserLabelScore, Long> {
    Optional<List<UserLabelScore>> findByUserId(Long userId);
}
