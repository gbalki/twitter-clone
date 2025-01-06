package com.balki.twitter_clone.controller;

import com.balki.twitter_clone.request.*;
import com.balki.twitter_clone.response.GenericResponse;
import com.balki.twitter_clone.response.TokenResponse;
import com.balki.twitter_clone.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/1.0/users")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> save(@RequestBody @Valid UserSaveRequest userSaveRequest) {
        return ResponseEntity.ok(authenticationService.save(userSaveRequest));
    }

    @PutMapping("/verify-account")
    public ResponseEntity<String> verifyAccount(@RequestParam String email,
                                                @RequestParam String otp) {
        return ResponseEntity.ok(authenticationService.verifyAccount(email, otp));
    }

    @PutMapping("/regenerate-otp")
    public ResponseEntity<String> regenerateOtp(@RequestParam String email) {
        return ResponseEntity.ok(authenticationService.regenerateOtp(email));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(authenticationService.login(authenticationRequest));
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody PasswordRequest passwordRequest) {
        return ResponseEntity.ok(authenticationService.forgotPassword(passwordRequest));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordRequest passwordRequest
    ) {
        return ResponseEntity.ok(authenticationService.resetPassword(passwordRequest));
    }

    @PostMapping("/logout")
    public GenericResponse logOut(@RequestHeader(name = "Authorization") String authorization) {
        String accessToken = authorization.substring(7);
        authenticationService.clearToken(accessToken);
        return new GenericResponse("Log out Successfully");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){
        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequest));
    }
}
