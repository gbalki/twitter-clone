package com.balki.twitter_clone.repository;

import com.balki.twitter_clone.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTwitterId(long id, Pageable page);
}
