package com.balki.twitter_clone.service;

import com.balki.twitter_clone.dto.CommentDTO;
import com.balki.twitter_clone.dto.LikeDTO;
import com.balki.twitter_clone.dto.TwitterDTO;
import com.balki.twitter_clone.exception.FileAttachmentNotFoundException;
import com.balki.twitter_clone.exception.TwitterNotFoundException;
import com.balki.twitter_clone.model.*;
import com.balki.twitter_clone.repository.*;
import com.balki.twitter_clone.request.CommentRequest;
import com.balki.twitter_clone.request.TwitterSaveRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TwitterService {

    private final TwitterRepository twitterRepository;

    private final FileAttachmentRepository fileAttachmentRepository;

    private final UserRepository userRepository;

    private final LikeRepository likeRepository;

    private final CommentRepository commentRepository;

    private final FileService fileService;

    private final ModelMapper mapper;


    public void save(TwitterSaveRequest twitterSaveRequest, User user) {
        Twitter twitter = new Twitter();
        twitter.setContent(twitterSaveRequest.getContent());
        twitter.setTimestamp(LocalDateTime.now());
        twitter.setUser(user);
        twitterRepository.save(twitter);
        long attachmentId = twitterSaveRequest.getAttachmentId();
        if (attachmentId != 0) {
            FileAttachment fileAttachment = fileAttachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new FileAttachmentNotFoundException("File not found with this id: " + attachmentId));
            fileAttachment.setTwitter(twitter);
            fileAttachmentRepository.save(fileAttachment);
        }
    }

    public Page<TwitterDTO> getAllTwitts(Pageable page) {
        Page<Twitter> twitters = twitterRepository.findAll(page);
        return new PageImpl<>(twitters.stream().map(twits -> mapper.map(twits, TwitterDTO.class)).collect(Collectors.toList()));
    }

    public Page<TwitterDTO> getTwittsOfUser(Long id, Pageable page) {
        User user = userRepository.findUserById(id);
        Page<Twitter> userTwitters = twitterRepository.findByUser(user, page);
        return new PageImpl<>(userTwitters.stream().map(twits -> mapper.map(twits, TwitterDTO.class)).collect(Collectors.toList()));
    }

    public Page<TwitterDTO> search(String keyword, Pageable page) {
        Page<Twitter> twitters = twitterRepository.search(keyword, page);
        return new PageImpl<>(twitters.stream().map(twits -> mapper.map(twits, TwitterDTO.class)).collect(Collectors.toList()));
    }

    public Page<TwitterDTO> getOldTwitts(Long id, Long userId, Pageable page) {
        Specification<Twitter> specification = idLessThan(id);
        if (userId != null) {
            User user = userRepository.findUserById(userId);
            specification = specification.and(userIs(user));
        }
        Page<Twitter> twitterPage = twitterRepository.findAll(specification, page);
        return twitterPage.map(twitter -> mapper.map(twitter, TwitterDTO.class));
    }

    public long getNewTwittsCount(Long id, Long userId) {
        Specification<Twitter> specification = idGreaterThan(id);
        if (userId != null) {
            User user = userRepository.findUserById(userId);
            specification = specification.and(userIs(user));
        }
        return twitterRepository.count(specification);
    }

    public List<TwitterDTO> getNewTwitts(Long id, Long userId, Sort sort) {
        Specification<Twitter> specification = idGreaterThan(id);
        if (userId != null) {
            User user = userRepository.findUserById(userId);
            specification = specification.and(userIs(user));
        }
        List<Twitter> twitters = twitterRepository.findAll(specification, sort);
        return twitters.stream().map(twit -> mapper.map(twit, TwitterDTO.class)).collect(Collectors.toList());
    }

    public String likeTwitt(long id, User user) {
        Twitter twitter = twitterRepository.findById(id).orElseThrow();
        Like existingLike = likeRepository.findByUserAndTwitter(user, twitter);
        if (existingLike != null) {
            likeRepository.delete(existingLike);
            return "Favorite removed";
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setTwitter(twitter);
            likeRepository.save(like);
            return "twit favorite";
        }
    }

    public Page<LikeDTO> getAllLikesOfTwitt(long id, Pageable page) {
        Page<Like> likesPage = likeRepository.findByTwitterId(id, page);
        return new PageImpl<>(likesPage.stream().map(likes -> mapper.map(likes, LikeDTO.class)).collect(Collectors.toList()));
    }

    public void commentTwitt(long id, User user, CommentRequest commentRequest) {
        Twitter twitter = twitterRepository.findById(id)
                .orElseThrow(() -> new TwitterNotFoundException("Twitter not found with this id:" + id));
        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        comment.setTimestamp(LocalDateTime.now());
        comment.setTwitter(twitter);
        comment.setUser(user);
        commentRepository.save(comment);
    }

    public Page<CommentDTO> getAllCommentsOfTwitt(long id, Pageable page) {
        Page<Comment> commentsPage = commentRepository.findByTwitterId(id, page);
        return new PageImpl<>(commentsPage.stream().map(comment -> mapper.map(comment, CommentDTO.class)).collect(Collectors.toList()));
    }

    Specification<Twitter> idLessThan(long id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.lessThan(root.get("id"), id);
        };
    }

    Specification<Twitter> userIs(User user) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("user"), user);
        };
    }

    Specification<Twitter> idGreaterThan(long id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.greaterThan(root.get("id"), id);
        };
    }

    public void delete(long id, User user) {
        Twitter twitter = twitterRepository.getOne(id);
        if (twitter.getUser().getId() != user.getId()) {
            throw new RuntimeException("Only authorized user can be delete twitt");
        }
        if (twitter.getFileAttachment() != null) {
            String fileName = twitter.getFileAttachment().getName();
            fileService.deleteAttachmentFile(fileName);
        }
        twitterRepository.deleteById(id);
    }
}
