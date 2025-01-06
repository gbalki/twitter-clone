package com.balki.twitter_clone.repository;

import com.balki.twitter_clone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    User findUserById(Long id);

    Optional<User> findByEmail(String email);
}
