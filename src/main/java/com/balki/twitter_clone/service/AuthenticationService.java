package com.balki.twitter_clone.service;

import com.balki.twitter_clone.exception.UserNotFoundException;
import com.balki.twitter_clone.model.Token;
import com.balki.twitter_clone.model.User;
import com.balki.twitter_clone.repository.TokenRepository;
import com.balki.twitter_clone.repository.UserRepository;
import com.balki.twitter_clone.request.*;
import com.balki.twitter_clone.dto.TokenDto;
import com.balki.twitter_clone.util.EmailUtil;
import com.balki.twitter_clone.util.OtpUtil;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper mapper;

    private final OtpUtil otpUtil;

    private final EmailUtil emailUtil;

    @Transactional
    public String save(UserSaveRequest userSaveRequest) {
        String otp = otpUtil.generateOtp();
        User user = mapper.map(userSaveRequest, User.class);
        user.setPassword(passwordEncoder.encode(userSaveRequest.getPassword()));
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);
        try {
            emailUtil.sendOtpEmail(userSaveRequest.getEmail(), otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send otp please try again");
        }
        return "Please verify your account within 3 minutes";
    }

    public String verifyAccount(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + email));
        if (user.getOtp().equals(otp) && Duration.between(user.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds() < (3 * 60)) {
            user.setActive(true);
            user.setOtp(null);
            userRepository.save(user);
            return "OTP verified you can login";
        }
        return "Please regenerate otp and try again";
    }

    public String regenerateOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + email));
        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(email, otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send otp please try again");
        }
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);
        return "Email sent... please verify account within 3 minute";
    }

    public TokenDto login(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        User user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + authenticationRequest.getEmail()));
        if (!user.isActive()) {
            throw new RuntimeException("user is not active");
        }
        var token = new Token();
        token.setUser(user);
        token.setAccessToken(jwtService.generateAccessToken(user));
        token.setRefreshToken(jwtService.generateRefreshToken(user));
        return mapper.map(tokenRepository.save(token), TokenDto.class);
    }

    public String forgotPassword(PasswordRequest passwordRequest) {
        User user = userRepository.findByEmail(passwordRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + passwordRequest.getEmail()));
        try {
            String passwordResetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(passwordResetToken);
            userRepository.save(user);
            emailUtil.sendSetPasswordEmail(passwordRequest.getEmail(), passwordResetToken);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send set password email please try again");
        }
        return "Please check your email to set new password";
    }

    public String resetPassword(PasswordRequest passwordRequest) {
        User user = userRepository.findByEmail(passwordRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + passwordRequest.getEmail()));
        if (user.getPasswordResetToken() == null || !user.getPasswordResetToken().equals(passwordRequest.getPasswordResetToken())) {
            throw new RuntimeException("Password reset token or email not found");
        }
        user.setPasswordResetToken(null);
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);
        return "Password reset successful login with new password";
    }

    public void clearToken(String accessToken) {
        tokenRepository.deleteById(accessToken);
    }

    public TokenDto refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String email = jwtService.findEmail(refreshTokenRequest.getRefreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + email));
        if (jwtService.tokenControl(refreshTokenRequest.getRefreshToken(), user)) {
            var token = new Token();
            token.setUser(user);
            token.setAccessToken(jwtService.generateAccessToken(user));
            token.setRefreshToken(refreshTokenRequest.getRefreshToken());
            return mapper.map(tokenRepository.save(token), TokenDto.class);
        }
        throw new RuntimeException();
    }
}
