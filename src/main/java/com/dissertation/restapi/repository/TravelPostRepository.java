package com.dissertation.restapi.repository;

import com.dissertation.restapi.model.TravelPost;
import com.dissertation.restapi.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TravelPostRepository extends CrudRepository<TravelPost, Long> {
    Optional<TravelPost> findByTitleAndUploaderId(String title, Long uploaderId);
    Optional<List<TravelPost>> findByUploaderId(Long uploaderId);
    Optional<TravelPost> findById(Long id);
    Optional<List<TravelPost>> findAllByUploaderIdOrderByCreationDateDesc(Long uploaderId);
    Optional<List<TravelPost>> findAllByUploaderInOrderByCreationDateDesc(List<User> uploaders);
}

