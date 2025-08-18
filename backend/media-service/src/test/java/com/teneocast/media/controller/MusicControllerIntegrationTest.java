package com.teneocast.media.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.media.dto.MusicDto;
import com.teneocast.media.entity.MusicGenre;
import com.teneocast.media.repository.MusicGenreRepository;
import com.teneocast.media.repository.MusicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class MusicControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private MusicRepository musicRepository;
    
    @Autowired
    private MusicGenreRepository musicGenreRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID testTenantId;
    private MusicGenre testGenre;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        // Clean the database before each test
        musicRepository.deleteAll();
        musicGenreRepository.deleteAll();
        
        // Create test data
        testTenantId = UUID.randomUUID();
        testGenre = MusicGenre.builder()
                .name("Test Genre")
                .description("Test genre description")
                .build();
        musicGenreRepository.save(testGenre);
        
        baseUrl = "http://localhost:" + port + "/media";
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAllGenres() throws Exception {
        mockMvc.perform(get("/api/media/music/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Test Genre"));
    }

    @Test
    void testGetMusicByTenant_Empty() throws Exception {
        mockMvc.perform(get("/api/media/music")
                .header("X-Tenant-ID", testTenantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    void testGetMusicByTenant_WithSearch() throws Exception {
        mockMvc.perform(get("/api/media/music")
                .header("X-Tenant-ID", testTenantId.toString())
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetMusicByTenant_WithGenre() throws Exception {
        mockMvc.perform(get("/api/media/music")
                .header("X-Tenant-ID", testTenantId.toString())
                .param("genreId", testGenre.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAllMusicByTenant_Empty() throws Exception {
        mockMvc.perform(get("/api/media/music/all")
                .header("X-Tenant-ID", testTenantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("media-service"));
    }
}
