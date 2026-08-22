package com.example.studentdashboard;

import com.example.studentdashboard.dto.LoginRequest;
import com.example.studentdashboard.dto.RegisterRequest;
import com.example.studentdashboard.entity.Role;
import com.example.studentdashboard.entity.User;
import com.example.studentdashboard.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void testStudentRegistrationAndLoginFlow() throws Exception {
        // 1. Register a student
        RegisterRequest registerReq = RegisterRequest.builder()
                .name("Student Alice")
                .email("alice@student.com")
                .password("password123")
                .build();

        String registerJson = objectMapper.writeValueAsString(registerReq);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.name", is("Student Alice")))
                .andExpect(jsonPath("$.user.email", is("alice@student.com")))
                .andExpect(jsonPath("$.user.role", is("STUDENT")));

        // Verify password hashing in DB
        User dbUser = userRepository.findByEmail("alice@student.com").orElse(null);
        assertThat(dbUser).isNotNull();
        assertThat(dbUser.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", dbUser.getPassword())).isTrue();

        // 2. Login with valid credentials
        LoginRequest loginReq = LoginRequest.builder()
                .email("alice@student.com")
                .password("password123")
                .build();

        String loginJson = objectMapper.writeValueAsString(loginReq);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email", is("alice@student.com")))
                .andReturn().getResponse().getContentAsString();

        // Extract token
        String jwtToken = objectMapper.readTree(loginResponse).get("token").asText();

        // 3. Request current user profile using JWT token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("alice@student.com")))
                .andExpect(jsonPath("$.role", is("STUDENT")));
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginReq = LoginRequest.builder()
                .email("wrong@test.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @Test
    public void testUnauthorizedAccessToProtectedRoutes() throws Exception {
        // Accessing protected student endpoint without JWT
        mockMvc.perform(get("/api/student/dashboard/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Unauthorized")));
    }

    @Test
    public void testRoleBasedAccessControlLimits() throws Exception {
        // Save a Student and a Mentor
        User studentUser = userRepository.save(User.builder()
                .name("Student User")
                .email("student@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.STUDENT)
                .build());

        User mentorUser = userRepository.save(User.builder()
                .name("Mentor User")
                .email("mentor@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.MENTOR)
                .build());

        // Get student token
        String studentToken = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginRequest("student@test.com", "password"))))
                        .andReturn().getResponse().getContentAsString()
        ).get("token").asText();

        // Get mentor token
        String mentorToken = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginRequest("mentor@test.com", "password"))))
                        .andReturn().getResponse().getContentAsString()
        ).get("token").asText();

        // 1. Student trying to access mentor endpoint -> should return 403 Forbidden
        mockMvc.perform(get("/api/mentor/dashboard/summary")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // 2. Mentor trying to access student endpoint -> should return 403 Forbidden
        mockMvc.perform(get("/api/student/dashboard/summary")
                        .header("Authorization", "Bearer " + mentorToken))
                .andExpect(status().isForbidden());
    }
}
