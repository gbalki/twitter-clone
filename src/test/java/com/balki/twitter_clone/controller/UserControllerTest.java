package com.balki.twitter_clone.controller;

import com.balki.twitter_clone.dto.TokenDTO;
import com.balki.twitter_clone.dto.UserDTO;
import com.balki.twitter_clone.model.User;
import com.balki.twitter_clone.repository.UserRepository;
import com.balki.twitter_clone.request.AuthenticationRequest;
import com.balki.twitter_clone.request.UserUpdateRequest;
import com.balki.twitter_clone.service.JwtService;
import com.balki.twitter_clone.service.UserService;
import com.balki.twitter_clone.util.OtpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.time.LocalDateTime;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class UserControllerTest {

    private static final String BASE_URL = "/api/1.0/users";
    private static final String LOGIN_URL = BASE_URL + "/login";
    private static final String GETALL_URL = BASE_URL + "/getAll";
    private static final String GETBYID_URL = BASE_URL + "/getById/{id}";
    private static final String UPDATE_URL = BASE_URL + "/update/{id}";
    private static final String DELETE_URL = BASE_URL + "/delete/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userServiceMock;

    @Autowired
    private UserRepository userRepository;

    private User validUser;

    private Page<UserDTO> pageOfUserDTO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private TokenDTO tokenDto;

    @Autowired
    private OtpUtil otpUtil;

    @PersistenceContext
    private EntityManager entityManager;

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

        entityManager.persist(user);
        entityManager.flush();

        return user;
    }

    private TokenDTO loggedInUser(String email, String password) throws Exception {
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

        TokenDTO tokenDto = objectMapper.readValue(tokenJson, TokenDTO.class);
        Assertions.assertEquals(email, jwtService.findEmail(tokenDto.getAccessToken()));
        return tokenDto;
    }

    @Test
    public void testGetAllUser() throws Exception {
        Pageable page = PageRequest.of(0, 10);

        when(userServiceMock.getAll(page, validUser)).thenReturn(this.pageOfUserDTO);

        mockMvc.perform(get(GETALL_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName", is("testFirstName testLastName")))
                .andExpect(jsonPath("$.content[0].displayName", is("testDisplayName")));


        Assertions.assertEquals(0, page.getPageNumber());
        Assertions.assertEquals(10, page.getPageSize());

    }

    @AfterEach
    public void clearDatabase() throws Exception {
        userRepository.deleteById(1L);
    }
}
