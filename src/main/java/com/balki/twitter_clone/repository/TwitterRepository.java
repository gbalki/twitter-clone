package com.balki.twitter_clone.repository;

import com.balki.twitter_clone.model.Twitter;
import com.balki.twitter_clone.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TwitterRepository extends JpaRepository<Twitter, Long>, JpaSpecificationExecutor<Twitter> {

    Page<Twitter> findByUser(User user, Pageable page);

    @Query("SELECT t FROM Twitter t WHERE t.content LIKE %:keyword% OR t.user.firstName LIKE %:keyword% OR t.user.lastName LIKE %:keyword% OR t.user.email LIKE %:keyword%")
    Page<Twitter> search(@Param("keyword") String keyword, Pageable page);
}
