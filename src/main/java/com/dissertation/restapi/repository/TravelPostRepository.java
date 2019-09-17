package com.dissertation.restapi.repository;

import com.dissertation.restapi.model.TravelPost;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TravelPostRepository extends CrudRepository<TravelPost, Long> {
    Optional<TravelPost> findByTitleAndUploaderId(String title, Long uploaderId);
    Optional<TravelPost> findById(Long id);
}

