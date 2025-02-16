package com.balki.twitter_clone.service;

import com.balki.twitter_clone.exception.UserNotFoundException;
import com.balki.twitter_clone.model.Token;
import com.balki.twitter_clone.model.User;
import com.balki.twitter_clone.repository.TokenRepository;
import com.balki.twitter_clone.repository.UserRepository;
import com.balki.twitter_clone.request.PasswordRequest;
import com.balki.twitter_clone.request.RefreshTokenRequest;
import com.balki.twitter_clone.request.UserSaveRequest;
import com.balki.twitter_clone.dto.TokenDTO;
import com.balki.twitter_clone.util.EmailUtil;
import com.balki.twitter_clone.util.OtpUtil;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@TestPropertySource("/application-test.properties")
@SpringBootTest
public class AuthenticationServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TokenDTO tokenDto;

    private User validUser;

    @Autowired
    private OtpUtil otpUtil;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ModelMapper mapper;

    @BeforeEach
    public void setUp() throws Exception {
        validUser = createUser("testexample@gmail.com", "P4ssword");
        tokenDto = loggedInUser(validUser.getEmail(), "P4ssword");
    }

    private User createUser(String email, String password) {
        User user = new User();
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setDisplayName("testDisplayName");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setImage("profile-image.png");
        user.setOtp(otpUtil.generateOtp());
        user.setActive(true);
        user.setOtpGeneratedTime(LocalDateTime.now());
        user.setPasswordResetToken("reset-password-token");
        userRepository.save(user);
        return user;
    }

    private TokenDTO loggedInUser(String email, String password) throws Exception {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + email));
        Assertions.assertTrue(user.getId() > 0L);
        Assertions.assertTrue(user.isActive());
        var token = new Token();
        token.setUser(user);
        token.setAccessToken(jwtService.generateAccessToken(user));
        token.setRefreshToken(jwtService.generateRefreshToken(user));

        return mapper.map(tokenRepository.save(token), TokenDTO.class);
    }

    @Test
    public void createUserTest() throws Exception {
        UserSaveRequest userSaveRequest = new UserSaveRequest();
        userSaveRequest.setFirstName("testFirstName");
        userSaveRequest.setLastName("testLastName");
        userSaveRequest.setDisplayName("testDisplayName");
        userSaveRequest.setEmail("testexample2@gmail.com");
        userSaveRequest.setPassword("P4ssword");

        String otp = otpUtil.generateOtp();

        User user = mapper.map(userSaveRequest, User.class);
        user.setPassword(passwordEncoder.encode(userSaveRequest.getPassword()));
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);
        emailUtil.sendOtpEmail(userSaveRequest.getEmail(), otp);

        User savedUser = userRepository.findByEmail(user.getEmail()).orElseThrow();
        Assertions.assertNotNull(savedUser);
        Assertions.assertTrue(user.getId() > 0L);
        Assertions.assertEquals(user.getEmail(), userSaveRequest.getEmail());
        Assertions.assertNotNull(user.getOtp());
        Assertions.assertNotNull(user.getOtpGeneratedTime());
    }

    @Test
    public void verifyAccountTest() throws Exception {
        String email = "testexample@gmail.com";
        String otp = validUser.getOtp();
        User user = userRepository.findByEmail(email)
                .orElseThrow();
        Assertions.assertTrue(user.getId() > 0L);
        if (user.getOtp().equals(otp) && Duration.between(user.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds() < (3 * 60)) {
            user.setActive(true);
            userRepository.save(user);
        }
        Assertions.assertTrue(user.isActive());
        String response = authenticationService.verifyAccount(email, otp);
        Assertions.assertEquals("OTP verified you can login", response);
    }

    @Test
    public void regenerateOtpTest() throws Exception {
        User user = userRepository.findByEmail(validUser.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + validUser.getEmail()));
        Assertions.assertTrue(user.getId() > 0L);
        String otp = otpUtil.generateOtp();
        Assertions.assertNotNull(otp);
        try {
            emailUtil.sendOtpEmail(validUser.getEmail(), otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send otp please try again");
        }
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);
        String response = authenticationService.regenerateOtp(validUser.getEmail());
        Assertions.assertEquals("Email sent... please verify account within 3 minute", response);
    }

    @Test
    public void forgotPasswordTest() throws Exception {
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setEmail("testexample@gmail.com");
        User user = userRepository.findByEmail(passwordRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + validUser.getEmail()));
        Assertions.assertNotNull(user);
        Assertions.assertEquals(passwordRequest.getEmail(), user.getEmail());
        try {
            String passwordResetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(passwordResetToken);
            userRepository.save(user);
            emailUtil.sendSetPasswordEmail(passwordRequest.getEmail(), passwordResetToken);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send set password email please try again");
        }
        Assertions.assertNotNull(user.getPasswordResetToken());
        String response = authenticationService.forgotPassword(passwordRequest);
        Assertions.assertEquals("Please check your email to set new password", response);
    }

    @Test
    public void resetPasswordTest() throws Exception {
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setPasswordResetToken("reset-password-token");
        passwordRequest.setEmail("testexample@gmail.com");
        passwordRequest.setNewPassword("P4ssword2");
        User user = userRepository.findByEmail(passwordRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + validUser.getEmail()));
        Assertions.assertNotNull(user);
        Assertions.assertEquals(passwordRequest.getEmail(), user.getEmail());
        Assertions.assertNotNull(user.getPasswordResetToken());
        Assertions.assertEquals(user.getPasswordResetToken(), passwordRequest.getPasswordResetToken());
        user.setPasswordResetToken(null);
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);
        Assertions.assertNull(user.getPasswordResetToken());
    }

    @Test
    public void clearTokenTest() throws Exception {
        Assertions.assertNotNull(tokenRepository.findById(tokenDto.getAccessToken()));
        tokenRepository.deleteById(tokenDto.getAccessToken());
        Assertions.assertTrue(tokenRepository.findById(tokenDto.getAccessToken()).isEmpty());
    }

    @Test
    public void refreshTokenTest() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken(tokenDto.getRefreshToken());
        String email = jwtService.findEmail(refreshTokenRequest.getRefreshToken());
        Assertions.assertNotNull(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + validUser.getEmail()));
        Assertions.assertNotNull(user);
        Assertions.assertTrue(user.getId()>0L);
        Assertions.assertTrue(jwtService.tokenControl(refreshTokenRequest.getRefreshToken(),user));

        var token = new Token();
        token.setUser(user);
        token.setAccessToken(jwtService.generateAccessToken(user));
        token.setRefreshToken(jwtService.generateRefreshToken(user));
        mapper.map(tokenRepository.save(token), TokenDTO.class);
        Assertions.assertNotNull(tokenRepository.findById(tokenDto.getAccessToken()));
        TokenDTO response = authenticationService.refreshToken(refreshTokenRequest);
        Assertions.assertNotNull(response);
    }

    @AfterEach
    public void clearDatabase() throws Exception {
        userRepository.deleteById(validUser.getId());
    }
}
