package com.balki.twitter_clone.controller;

import com.balki.twitter_clone.model.User;
import com.balki.twitter_clone.repository.TokenRepository;

import com.balki.twitter_clone.request.AuthenticationRequest;
import com.balki.twitter_clone.request.PasswordRequest;
import com.balki.twitter_clone.request.RefreshTokenRequest;
import com.balki.twitter_clone.request.UserSaveRequest;
import com.balki.twitter_clone.response.TokenResponse;
import com.balki.twitter_clone.service.AuthenticationService;
import com.balki.twitter_clone.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class AuthenticationControllerTest {

    private static final String BASE_URL = "/api/1.0/users";
    private static final String LOGIN_URL = BASE_URL + "/login";
    private static final String REGISTER_URL = BASE_URL + "/register";
    private static final String VERIFY_ACCOUNT_URL = BASE_URL + "/verify-account";
    private static final String REGENERATE_OTP_URL = BASE_URL + "/regenerate-otp";
    private static final String FORGOT_PASSWORD_URL = BASE_URL + "/forgot-password";
    private static final String RESET_PASSWORD_URL = BASE_URL + "/reset-password";
    private static final String LOGOUT_URL = BASE_URL + "/logout";
    private static final String REFRESH_TOKEN_URL = BASE_URL + "/refresh-token";


    @PersistenceContext
    private EntityManager entityManager;

    @Mock
    private AuthenticationService authenticationServiceMock;

    @Mock
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private TokenResponse tokenResponse;

    private User validUser;

    @BeforeEach
    public void setUp() throws Exception {
        validUser = createUser("testexample@gmail.com", "P4ssword");
        tokenResponse = loggedInUser(validUser.getEmail(), "P4ssword");
    }

    private User createUser(String email, String password) {
        User user = new User();
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setDisplayName("testDisplayName");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setImage("profile-image.png");
        user.setOtp("123456");
        user.setOtpGeneratedTime(LocalDateTime.now());
        user.setActive(true);
        user.setPasswordResetToken("test-password-reset-token");

        entityManager.persist(user);
        entityManager.flush();

        return user;
    }

    private TokenResponse loggedInUser(String email, String password) throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail(email);
        request.setPassword(password);

        String tokenJson = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TokenResponse tokenResponse = objectMapper.readValue(tokenJson, TokenResponse.class);
        Assertions.assertEquals(email, jwtService.findEmail(tokenResponse.getAccessToken()));
        return tokenResponse;
    }

    @DisplayName("Test post creation")
    @Test
    public void createUserTest() throws Exception {
        UserSaveRequest userSaveRequest = new UserSaveRequest();
        userSaveRequest.setFirstName("testFirstName");
        userSaveRequest.setLastName("testLastName");
        userSaveRequest.setDisplayName("testDisplayName");
        userSaveRequest.setEmail("testexample2@gmail.com");
        userSaveRequest.setPassword("P4ssword");

        when(authenticationServiceMock.save(userSaveRequest)).thenReturn("Please verify your account within 3 minutes");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userSaveRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Please verify your account within 3 minutes")));
    }

    @DisplayName("Test for verify Account")
    @Test
    public void testVerifyAccount_Success() throws Exception {
        String email = validUser.getEmail();
        String otp = "123456";
        String expectedResponse = "OTP verified you can login";

        when(authenticationServiceMock.verifyAccount(email, otp)).thenReturn(expectedResponse);

        mockMvc.perform(put(VERIFY_ACCOUNT_URL)
                        .param("email", email)
                        .param("otp", otp))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @DisplayName("Test for regenerate Otp")
    @Test
    public void regenerateOtp_Success() throws Exception {
        String email = validUser.getEmail();
        String expectedResponse = "Email sent... please verify account within 3 minute";

        when(authenticationServiceMock.regenerateOtp(email)).thenReturn(expectedResponse);

        mockMvc.perform(put(REGENERATE_OTP_URL)
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @DisplayName("Test for forgot password")
    @Test
    public void forgotPassword_Success() throws Exception {
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setEmail(validUser.getEmail());

        String expectedResponse = "Please check your email to set new password";
        when(authenticationServiceMock.forgotPassword(passwordRequest)).thenReturn(expectedResponse);

        mockMvc.perform(put(FORGOT_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @DisplayName("Test for reset password")
    @Test
    public void resetPassword_Success() throws Exception {
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setEmail(validUser.getEmail());
        passwordRequest.setPasswordResetToken(validUser.getPasswordResetToken());
        passwordRequest.setNewPassword("P4ssword2");

        String expectedResponse = "Password reset successful login with new password";
        when(authenticationServiceMock.resetPassword(passwordRequest)).thenReturn(expectedResponse);

        mockMvc.perform(put(RESET_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void logOut_Success() throws Exception {
        mockMvc.perform(post(LOGOUT_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Log out Successfully"));

        Assertions.assertTrue(tokenRepository.findById(tokenResponse.getAccessToken()).isEmpty());
    }

    @DisplayName("Test for refresh token")
    @Test
    public void refreshToken_Success() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken(tokenResponse.getRefreshToken());

        String tokenJson = mockMvc.perform(post(REFRESH_TOKEN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getRefreshToken())
                        .content(objectMapper.writeValueAsString(refreshTokenRequest))) // Corrected parentheses
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TokenResponse tokenResponse = objectMapper.readValue(tokenJson, TokenResponse.class);
        Assertions.assertEquals(validUser.getEmail(), jwtService.findEmail(tokenResponse.getAccessToken()));
    }
}
