package com.dissertation.restapi.repository;

import com.dissertation.restapi.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository  extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}
