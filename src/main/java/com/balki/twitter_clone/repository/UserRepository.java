package com.balki.twitter_clone.repository;

import com.balki.twitter_clone.dto.UserDto;
import com.balki.twitter_clone.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    User findUserById(Long id);

    Optional<User> findByEmail(String email);

    Page<User> findByEmailNotAndActiveTrue(String email, Pageable page);

    Page<User> findUserByActiveTrue(Pageable page);
}
