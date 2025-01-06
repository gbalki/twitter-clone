package com.balki.twitter_clone.repository;

import com.balki.twitter_clone.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, String> {
}
