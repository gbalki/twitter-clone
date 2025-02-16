package com.balki.twitter_clone.controller;

import com.balki.twitter_clone.annotation.CurrentUser;
import com.balki.twitter_clone.dto.TwitterDTO;
import com.balki.twitter_clone.model.User;
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

    @GetMapping("/users/{id}/twitters")
    Page<TwitterDTO> getAllUserTwitts(@PathVariable Long id
            , @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page) {
        return twitterService.getTwittsOfUser(id, page);
    }

    @GetMapping({"/twitters/{id:[0-9]+}", "/users/{userId}/twitters/{id:[0-9]+}"})
    ResponseEntity<?> getHoaxesRelative(@PathVariable long id, @PathVariable(required = false) Long userId,
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
}
