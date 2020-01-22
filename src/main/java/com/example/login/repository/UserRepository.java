package com.example.login.repository;

import com.example.login.model.UserProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserProfile, Long> {

    Optional<UserProfile> findByEmail(String email);

    Boolean existsByEmail(String email);

}
