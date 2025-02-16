package com.balki.twitter_clone.repository;

import com.balki.twitter_clone.model.Twitter;
import com.balki.twitter_clone.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TwitterRepository extends JpaRepository<Twitter,Long>, JpaSpecificationExecutor<Twitter> {

    Page<Twitter> findByUser(User user, Pageable page);
}
