package com.balki.twitter_clone.controller;

import com.balki.twitter_clone.annotation.CurrentUser;
import com.balki.twitter_clone.dto.CommentDTO;
import com.balki.twitter_clone.dto.LikeDTO;
import com.balki.twitter_clone.dto.TwitterDTO;
import com.balki.twitter_clone.model.User;
import com.balki.twitter_clone.request.CommentRequest;
import com.balki.twitter_clone.request.TwitterSaveRequest;
import com.balki.twitter_clone.response.GenericResponse;
import com.balki.twitter_clone.service.TwitterService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/1.0")
public class TwitterController {

    @Autowired
    TwitterService twitterService;

    @PostMapping("/twitters/save")
    public GenericResponse saveTwitt(@CurrentUser User user
            , @RequestBody @Valid TwitterSaveRequest twitterSaveRequest) {

        twitterService.save(twitterSaveRequest, user);
        return new GenericResponse("Twitter is Saved");
    }

    @GetMapping("/twitters/getAll")
    Page<TwitterDTO> getAllTwitts(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page) {
        return twitterService.getAllTwitts(page);
    }

    @GetMapping("/users/{id:[0-9]+}/twitters")
    Page<TwitterDTO> getAllUserTwitts(@PathVariable Long id
            , @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page) {
        return twitterService.getTwittsOfUser(id, page);
    }

    @GetMapping("/twitter/search")
    Page<TwitterDTO> search(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page
            , @RequestParam String keyword) throws Exception {
        return twitterService.search(keyword,page);
    }

    @GetMapping({"/twitters/{id:[0-9]+}", "/users/{userId}/twitters/{id:[0-9]+}"})
    ResponseEntity<?> getTwittsRelative(@PathVariable long id, @PathVariable(required = false) Long userId,
                                        @RequestParam(name = "count", required = false, defaultValue = "false") boolean count,
                                        @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page,
                                        @RequestParam(name = "direction", defaultValue = "before") String direction) {
        if (count) {
            return ResponseEntity.ok(twitterService.getNewTwittsCount(id, userId));
        }
        if (direction.equals("after")) {
            return ResponseEntity.ok(twitterService.getNewTwitts(id, userId, page.getSort()));
        }
        return ResponseEntity.ok(twitterService.getOldTwitts(id, userId, page));
    }

    @PostMapping("/twitters/{id:[0-9]+}/like")
    ResponseEntity<String> likeTwitt(@PathVariable long id, @CurrentUser User user) {
        return ResponseEntity.ok(twitterService.likeTwitt(id, user));
    }

    @GetMapping("/twitters/{id:[0-9]+}/getLikes")
    Page<LikeDTO> getAllLikesOfTwitt(@PathVariable long id
            , @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page) {
        return twitterService.getAllLikesOfTwitt(id, page);
    }

    @PostMapping("/twitters/{id:[0-9]+}/comment")
    public GenericResponse commentTwitt(@CurrentUser User user, @PathVariable long id, @RequestBody @Valid CommentRequest commentRequest) {
        twitterService.commentTwitt(id, user, commentRequest);
        return new GenericResponse("Comment saved");
    }

    @GetMapping("/twitters/{id:[0-9]+}/getAllComments")
    Page<CommentDTO> getAllCommentsOfTwitt(@PathVariable long id
            , @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page) {
        return twitterService.getAllCommentsOfTwitt(id, page);
    }

    @DeleteMapping("/twitters/{id:[0-9]+}/delete")
    public GenericResponse deleteTwitt(@PathVariable long id, @CurrentUser User user) {
        twitterService.delete(id, user);
        return new GenericResponse("twitt deleted");
    }
}
