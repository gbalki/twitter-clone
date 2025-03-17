package com.balki.twitter_clone.controller;

import com.balki.twitter_clone.annotation.CurrentUser;
import com.balki.twitter_clone.dto.UserDTO;
import com.balki.twitter_clone.model.User;
import com.balki.twitter_clone.request.UserUpdateRequest;
import com.balki.twitter_clone.response.GenericResponse;
import com.balki.twitter_clone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/1.0/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/getAll")
    Page<UserDTO> getAllUser(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page,
                             @CurrentUser User user) {
        return userService.getAll(page, user);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
                                              @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userUpdateRequest));
    }

    @DeleteMapping("/delete/{id}")
    public GenericResponse deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return new GenericResponse("User is removed");
    }
}
