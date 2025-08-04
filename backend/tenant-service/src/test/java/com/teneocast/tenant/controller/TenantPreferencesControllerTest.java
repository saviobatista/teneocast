package com.teneocast.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.tenant.controller.GlobalExceptionHandler;
import com.teneocast.tenant.dto.TenantPreferencesDto;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.service.TenantPreferencesService;
import com.teneocast.tenant.service.TenantValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TenantPreferencesControllerTest {

    @Mock
    private TenantPreferencesService tenantPreferencesService;

    @Mock
    private TenantValidationService tenantValidationService;

    @InjectMocks
    private TenantPreferencesController tenantPreferencesController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String tenantId;
    private TenantPreferencesDto testPreferencesDto;
    private TenantPreferencesDto createRequest;
    private TenantPreferencesDto updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tenantPreferencesController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        tenantId = UUID.randomUUID().toString();

        testPreferencesDto = TenantPreferencesDto.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .playbackSettings("{\"volume\":75,\"autoplay\":true}")
                .genrePreferences("[\"rock\",\"jazz\",\"classical\"]")
                .adRules("{\"maxAdsPerHour\":2,\"skipAfter\":30}")
                .volumeDefault(75)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = TenantPreferencesDto.builder()
                .playbackSettings("{\"volume\":80,\"autoplay\":false}")
                .genrePreferences("[\"pop\",\"electronic\"]")
                .adRules("{\"maxAdsPerHour\":1,\"skipAfter\":15}")
                .volumeDefault(80)
                .build();

        updateRequest = TenantPreferencesDto.builder()
                .playbackSettings("{\"volume\":85,\"autoplay\":true}")
                .volumeDefault(85)
                .build();
    }

    @Test
    void testSavePreferences_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantPreferencesService.savePreferences(eq(tenantId), any(TenantPreferencesDto.class)))
                .thenReturn(testPreferencesDto);

        // When & Then
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/preferences", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testPreferencesDto.getId()))
                .andExpect(jsonPath("$.volumeDefault").value(testPreferencesDto.getVolumeDefault()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantPreferencesService).savePreferences(eq(tenantId), any(TenantPreferencesDto.class));
    }

    @Test
    void testSavePreferences_TenantNotFound() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantPreferencesService.savePreferences(eq(tenantId), any(TenantPreferencesDto.class)))
                .thenThrow(new TenantNotFoundException("Tenant not found"));

        // When & Then
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/preferences", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantPreferencesService).savePreferences(eq(tenantId), any(TenantPreferencesDto.class));
    }

    @Test
    void testGetPreferences_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantPreferencesService.getPreferences(tenantId)).thenReturn(testPreferencesDto);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPreferencesDto.getId()))
                .andExpect(jsonPath("$.volumeDefault").value(testPreferencesDto.getVolumeDefault()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantPreferencesService).getPreferences(tenantId);
    }

    @Test
    void testGetPreferences_NotFound() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantPreferencesService.getPreferences(tenantId))
                .thenThrow(new TenantNotFoundException("Preferences not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences", tenantId))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantPreferencesService).getPreferences(tenantId);
    }

    @Test
    void testUpdatePreferences_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantPreferencesService.updatePreferences(eq(tenantId), any(TenantPreferencesDto.class)))
                .thenReturn(testPreferencesDto);

        // When & Then
        mockMvc.perform(put("/api/v1/tenants/{tenantId}/preferences", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPreferencesDto.getId()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantPreferencesService).updatePreferences(eq(tenantId), any(TenantPreferencesDto.class));
    }

    @Test
    void testUpdatePreferences_NotFound() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantPreferencesService.updatePreferences(eq(tenantId), any(TenantPreferencesDto.class)))
                .thenThrow(new TenantNotFoundException("Preferences not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/tenants/{tenantId}/preferences", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantPreferencesService).updatePreferences(eq(tenantId), any(TenantPreferencesDto.class));
    }

    @Test
    void testDeletePreferences_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        doNothing().when(tenantPreferencesService).deletePreferences(tenantId);

        // When & Then
        mockMvc.perform(delete("/api/v1/tenants/{tenantId}/preferences", tenantId))
                .andExpect(status().isNoContent());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantPreferencesService).deletePreferences(tenantId);
    }

    @Test
    void testDeletePreferences_NotFound() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        doThrow(new TenantNotFoundException("Preferences not found"))
                .when(tenantPreferencesService).deletePreferences(tenantId);

        // When & Then
        mockMvc.perform(delete("/api/v1/tenants/{tenantId}/preferences", tenantId))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantPreferencesService).deletePreferences(tenantId);
    }

    @Test
    void testGetPreferencesByVolumeDefault_Success() throws Exception {
        // Given
        Integer volumeDefault = 75;
        when(tenantPreferencesService.getPreferencesByVolumeDefault(volumeDefault))
                .thenReturn(List.of(testPreferencesDto));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/volume/{volumeDefault}", tenantId, volumeDefault))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testPreferencesDto.getId()));

        verify(tenantPreferencesService).getPreferencesByVolumeDefault(volumeDefault);
    }

    @Test
    void testGetPreferencesByVolumeRange_Success() throws Exception {
        // Given
        Integer minVolume = 70;
        Integer maxVolume = 80;
        when(tenantPreferencesService.getPreferencesByVolumeRange(minVolume, maxVolume))
                .thenReturn(List.of(testPreferencesDto));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/volume/range", tenantId)
                        .param("minVolume", minVolume.toString())
                        .param("maxVolume", maxVolume.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testPreferencesDto.getId()));

        verify(tenantPreferencesService).getPreferencesByVolumeRange(minVolume, maxVolume);
    }

    @Test
    void testGetPreferencesByPlaybackSettings_Success() throws Exception {
        // Given
        String setting = "volume";
        when(tenantPreferencesService.getPreferencesByPlaybackSettings(setting))
                .thenReturn(List.of(testPreferencesDto));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/playback-settings", tenantId)
                        .param("setting", setting))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testPreferencesDto.getId()));

        verify(tenantPreferencesService).getPreferencesByPlaybackSettings(setting);
    }

    @Test
    void testGetPreferencesByPlaybackSettings_EmptySetting() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/playback-settings", tenantId)
                        .param("setting", ""))
                .andExpect(status().isBadRequest());

        verify(tenantPreferencesService, never()).getPreferencesByPlaybackSettings(anyString());
    }

    @Test
    void testGetPreferencesByGenrePreferences_Success() throws Exception {
        // Given
        String genre = "rock";
        when(tenantPreferencesService.getPreferencesByGenrePreferences(genre))
                .thenReturn(List.of(testPreferencesDto));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/genre-preferences", tenantId)
                        .param("genre", genre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testPreferencesDto.getId()));

        verify(tenantPreferencesService).getPreferencesByGenrePreferences(genre);
    }

    @Test
    void testGetPreferencesByGenrePreferences_EmptyGenre() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/genre-preferences", tenantId)
                        .param("genre", ""))
                .andExpect(status().isBadRequest());

        verify(tenantPreferencesService, never()).getPreferencesByGenrePreferences(anyString());
    }

    @Test
    void testGetPreferencesByAdRules_Success() throws Exception {
        // Given
        String rule = "maxAdsPerHour";
        when(tenantPreferencesService.getPreferencesByAdRules(rule))
                .thenReturn(List.of(testPreferencesDto));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/ad-rules", tenantId)
                        .param("rule", rule))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testPreferencesDto.getId()));

        verify(tenantPreferencesService).getPreferencesByAdRules(rule);
    }

    @Test
    void testGetPreferencesByAdRules_EmptyRule() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/ad-rules", tenantId)
                        .param("rule", ""))
                .andExpect(status().isBadRequest());

        verify(tenantPreferencesService, never()).getPreferencesByAdRules(anyString());
    }

    @Test
    void testGetPreferencesCountByVolumeDefault_Success() throws Exception {
        // Given
        Integer volumeDefault = 75;
        when(tenantPreferencesService.getPreferencesCountByVolumeDefault(volumeDefault)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/count/volume/{volumeDefault}", tenantId, volumeDefault))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(tenantPreferencesService).getPreferencesCountByVolumeDefault(volumeDefault);
    }

    @Test
    void testGetPreferencesCountByVolumeRange_Success() throws Exception {
        // Given
        Integer minVolume = 70;
        Integer maxVolume = 80;
        when(tenantPreferencesService.getPreferencesCountByVolumeRange(minVolume, maxVolume)).thenReturn(3L);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/count/volume/range", tenantId)
                        .param("minVolume", minVolume.toString())
                        .param("maxVolume", maxVolume.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));

        verify(tenantPreferencesService).getPreferencesCountByVolumeRange(minVolume, maxVolume);
    }

    @Test
    void testExistsByTenantId_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantPreferencesService.existsByTenantId(tenantId)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/exists", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantPreferencesService).existsByTenantId(tenantId);
    }

    @Test
    void testExistsByTenantId_InvalidId() throws Exception {
        // Given
        doThrow(new TenantValidationException("Invalid tenant ID format"))
                .when(tenantValidationService).validateTenantId(tenantId);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/exists", tenantId))
                .andExpect(status().isBadRequest());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantPreferencesService, never()).existsByTenantId(anyString());
    }

    @Test
    void testHealth_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/preferences/health", tenantId))
                .andExpect(status().isOk())
                .andExpect(content().string("Tenant preferences service is healthy for tenant: " + tenantId));
    }
} 