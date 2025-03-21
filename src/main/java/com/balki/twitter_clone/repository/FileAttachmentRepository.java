package com.balki.twitter_clone.repository;

import com.balki.twitter_clone.model.FileAttachment;
import com.balki.twitter_clone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment,Long> {

    List<FileAttachment> findByTwitterUser(User user);

    List<FileAttachment> findByDateBeforeAndTwitterIsNull(LocalDateTime twentyFourHoursAgo);
}
