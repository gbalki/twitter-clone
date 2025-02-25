package com.balki.twitter_clone.controller;

import com.balki.twitter_clone.dto.TokenDTO;
import com.balki.twitter_clone.dto.UserDTO;
import com.balki.twitter_clone.model.User;
import com.balki.twitter_clone.repository.TokenRepository;
import com.balki.twitter_clone.request.UserSaveRequest;
import com.balki.twitter_clone.service.AuthenticationService;
import com.balki.twitter_clone.service.JwtService;
import com.balki.twitter_clone.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.time.LocalDateTime;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class UserControllerTest {

    private static final String BASE_URL = "/api/1.0/users";
    private static final String GETALL_URL = BASE_URL + "/getAll";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userServiceMock;

    @InjectMocks
    private UserController userController;

    private User validUser;

    @BeforeEach
    public void setUp() throws Exception {
        validUser = createUser("testexample@gmail.com", "P4ssword");
    }

    private User createUser(String email, String password) {
        User user = new User();
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setDisplayName("testDisplayName");
        user.setEmail(email);
        user.setPassword(password);
        user.setImage("profile-image.png");
        user.setOtp("123456");
        user.setOtpGeneratedTime(LocalDateTime.now());
        user.setActive(true);
        user.setPasswordResetToken("test-password-reset-token");

        entityManager.persist(user);
        entityManager.flush();

        return user;
    }

    @Test
    public void testGetAllUser() throws Exception {
        mockMvc.perform(get(GETALL_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName", is("testFirstName testLastName")))
                .andExpect(jsonPath("$.content[0].displayName", is("testDisplayName")));

        Pageable pageable = PageRequest.of(0, 10);
        Assertions.assertEquals(0, pageable.getPageNumber());
        Assertions.assertEquals(10, pageable.getPageSize());

    }
}
