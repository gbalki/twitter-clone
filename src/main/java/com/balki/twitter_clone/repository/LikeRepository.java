package com.balki.twitter_clone.repository;

import com.balki.twitter_clone.model.Like;
import com.balki.twitter_clone.model.Twitter;
import com.balki.twitter_clone.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like,Long> {
    Like findByUserAndTwitter(User user, Twitter twitter);

    Page<Like> findByTwitterId(long id, Pageable page);
}
