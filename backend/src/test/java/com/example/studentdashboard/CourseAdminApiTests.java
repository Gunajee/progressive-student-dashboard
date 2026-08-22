package com.example.studentdashboard;

import com.example.studentdashboard.dto.CourseDto;
import com.example.studentdashboard.entity.Difficulty;
import com.example.studentdashboard.service.CourseAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CourseAdminApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseAdminService courseAdminService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateCourse_AsAdmin_Success() throws Exception {
        CourseDto courseDto = CourseDto.builder()
                .title("Advanced React")
                .description("Hooks and Context API")
                .category("Frontend")
                .difficulty(Difficulty.ADVANCED)
                .estimatedHours(10)
                .build();

        CourseDto createdDto = CourseDto.builder()
                .id(1L)
                .title("Advanced React")
                .build();

        when(courseAdminService.createCourse(any(CourseDto.class))).thenReturn(createdDto);

        mockMvc.perform(post("/api/admin/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Advanced React"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testCreateCourse_AsStudent_Forbidden() throws Exception {
        CourseDto courseDto = CourseDto.builder().title("Advanced React").build();

        mockMvc.perform(post("/api/admin/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MENTOR")
    public void testGetAllCourses_AsMentor_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/courses"))
                .andExpect(status().isForbidden());
    }
}
