package com.teneocast.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.tenant.controller.GlobalExceptionHandler;
import com.teneocast.tenant.dto.CreateTenantRequest;
import com.teneocast.tenant.dto.TenantDto;
import com.teneocast.tenant.dto.UpdateTenantRequest;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.exception.DuplicateSubdomainException;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.service.TenantService;
import com.teneocast.tenant.service.TenantValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @Mock
    private TenantValidationService tenantValidationService;

    @InjectMocks
    private TenantController tenantController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Tenant testTenant;
    private TenantDto testTenantDto;
    private CreateTenantRequest createRequest;
    private UpdateTenantRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tenantController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testTenant = Tenant.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testTenantDto = TenantDto.builder()
                .id(testTenant.getId())
                .name(testTenant.getName())
                .subdomain(testTenant.getSubdomain())
                .status(TenantDto.TenantStatus.ACTIVE)
                .createdAt(testTenant.getCreatedAt())
                .updatedAt(testTenant.getUpdatedAt())
                .build();

        createRequest = CreateTenantRequest.builder()
                .name("New Tenant")
                .subdomain("new-tenant")
                .build();

        updateRequest = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated-tenant")
                .build();
    }

    @Test
    void testCreateTenant_Success() throws Exception {
        // Given
        when(tenantService.createTenant(any(CreateTenantRequest.class))).thenReturn(testTenantDto);

        // When & Then
        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testTenantDto.getId()))
                .andExpect(jsonPath("$.name").value(testTenantDto.getName()))
                .andExpect(jsonPath("$.subdomain").value(testTenantDto.getSubdomain()));

        verify(tenantService).createTenant(any(CreateTenantRequest.class));
    }

    @Test
    void testCreateTenant_DuplicateSubdomain() throws Exception {
        // Given
        when(tenantService.createTenant(any(CreateTenantRequest.class)))
                .thenThrow(new DuplicateSubdomainException("Subdomain already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        verify(tenantService).createTenant(any(CreateTenantRequest.class));
    }

    @Test
    void testCreateTenant_ValidationError() throws Exception {
        // Given
        when(tenantService.createTenant(any(CreateTenantRequest.class)))
                .thenThrow(new TenantValidationException("Invalid tenant data"));

        // When & Then
        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(tenantService).createTenant(any(CreateTenantRequest.class));
    }

    @Test
    void testGetTenantById_Success() throws Exception {
        // Given
        String tenantId = testTenant.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantService.getTenantById(tenantId)).thenReturn(testTenantDto);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{id}", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTenantDto.getId()))
                .andExpect(jsonPath("$.name").value(testTenantDto.getName()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantService).getTenantById(tenantId);
    }

    @Test
    void testGetTenantById_NotFound() throws Exception {
        // Given
        String tenantId = testTenant.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantService.getTenantById(tenantId))
                .thenThrow(new TenantNotFoundException("Tenant not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{id}", tenantId))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantService).getTenantById(tenantId);
    }

    @Test
    void testGetTenantById_InvalidId() throws Exception {
        // Given
        String invalidId = "invalid-id";
        doThrow(new TenantValidationException("Invalid tenant ID format"))
                .when(tenantValidationService).validateTenantId(invalidId);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{id}", invalidId))
                .andExpect(status().isBadRequest());

        verify(tenantValidationService).validateTenantId(invalidId);
        verify(tenantService, never()).getTenantById(anyString());
    }

    @Test
    void testGetTenantBySubdomain_Success() throws Exception {
        // Given
        String subdomain = "test-tenant";
        when(tenantService.getTenantBySubdomain(subdomain)).thenReturn(testTenantDto);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/subdomain/{subdomain}", subdomain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTenantDto.getId()))
                .andExpect(jsonPath("$.subdomain").value(testTenantDto.getSubdomain()));

        verify(tenantService).getTenantBySubdomain(subdomain);
    }

    @Test
    void testGetTenantBySubdomain_NotFound() throws Exception {
        // Given
        String subdomain = "non-existent";
        when(tenantService.getTenantBySubdomain(subdomain))
                .thenThrow(new TenantNotFoundException("Tenant not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/subdomain/{subdomain}", subdomain))
                .andExpect(status().isNotFound());

        verify(tenantService).getTenantBySubdomain(subdomain);
    }

    @Test
    void testUpdateTenant_Success() throws Exception {
        // Given
        String tenantId = testTenant.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantService.updateTenant(eq(tenantId), any(UpdateTenantRequest.class)))
                .thenReturn(testTenantDto);

        // When & Then
        mockMvc.perform(put("/api/v1/tenants/{id}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTenantDto.getId()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantService).updateTenant(eq(tenantId), any(UpdateTenantRequest.class));
    }

    @Test
    void testUpdateTenant_NotFound() throws Exception {
        // Given
        String tenantId = testTenant.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantService.updateTenant(eq(tenantId), any(UpdateTenantRequest.class)))
                .thenThrow(new TenantNotFoundException("Tenant not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/tenants/{id}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantService).updateTenant(eq(tenantId), any(UpdateTenantRequest.class));
    }

    @Test
    void testDeleteTenant_Success() throws Exception {
        // Given
        String tenantId = testTenant.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        doNothing().when(tenantService).deleteTenant(tenantId);

        // When & Then
        mockMvc.perform(delete("/api/v1/tenants/{id}", tenantId))
                .andExpect(status().isNoContent());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantService).deleteTenant(tenantId);
    }

    @Test
    void testDeleteTenant_NotFound() throws Exception {
        // Given
        String tenantId = testTenant.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        doThrow(new TenantNotFoundException("Tenant not found")).when(tenantService).deleteTenant(tenantId);

        // When & Then
        mockMvc.perform(delete("/api/v1/tenants/{id}", tenantId))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantService).deleteTenant(tenantId);
    }

    @Test
    void testGetAllTenants_Success() throws Exception {
        // Given
        Page<TenantDto> tenantPage = new PageImpl<>(List.of(testTenantDto), PageRequest.of(0, 20), 1);
        doNothing().when(tenantValidationService).validatePagination(0, 20);
        when(tenantService.getAllTenants(any(Pageable.class))).thenReturn(tenantPage);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testTenantDto.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(tenantValidationService).validatePagination(0, 20);
        verify(tenantService).getAllTenants(any(Pageable.class));
    }

    @Test
    void testGetAllTenants_InvalidPagination() throws Exception {
        // Given
        doThrow(new TenantValidationException("Invalid pagination parameters"))
                .when(tenantValidationService).validatePagination(-1, 20);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest());

        verify(tenantValidationService).validatePagination(-1, 20);
        verify(tenantService, never()).getAllTenants(any(Pageable.class));
    }

    @Test
    void testGetTenantsByStatus_Success() throws Exception {
        // Given
        Tenant.TenantStatus status = Tenant.TenantStatus.ACTIVE;
        when(tenantService.getTenantsByStatus(status)).thenReturn(List.of(testTenantDto));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/status/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testTenantDto.getId()));

        verify(tenantService).getTenantsByStatus(status);
    }

    @Test
    void testGetTenantsByStatusWithPagination_Success() throws Exception {
        // Given
        Tenant.TenantStatus status = Tenant.TenantStatus.ACTIVE;
        Page<TenantDto> tenantPage = new PageImpl<>(List.of(testTenantDto), PageRequest.of(0, 20), 1);
        doNothing().when(tenantValidationService).validatePagination(0, 20);
        when(tenantService.getTenantsByStatus(eq(status), any(Pageable.class))).thenReturn(tenantPage);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/status/{status}/page", status)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testTenantDto.getId()));

        verify(tenantValidationService).validatePagination(0, 20);
        verify(tenantService).getTenantsByStatus(eq(status), any(Pageable.class));
    }

    @Test
    void testSearchTenantsByName_Success() throws Exception {
        // Given
        String searchName = "test";
        when(tenantService.searchTenantsByName(searchName)).thenReturn(List.of(testTenantDto));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/search")
                        .param("name", searchName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testTenantDto.getId()));

        verify(tenantService).searchTenantsByName(searchName);
    }

    @Test
    void testSearchTenantsByName_EmptyName() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tenants/search")
                        .param("name", ""))
                .andExpect(status().isBadRequest());

        verify(tenantService, never()).searchTenantsByName(anyString());
    }

    @Test
    void testGetActiveTenantsCount_Success() throws Exception {
        // Given
        when(tenantService.getActiveTenantsCount()).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/count/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(tenantService).getActiveTenantsCount();
    }

    @Test
    void testExistsById_Success() throws Exception {
        // Given
        String tenantId = testTenant.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantService.existsById(tenantId)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{id}/exists", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantService).existsById(tenantId);
    }

    @Test
    void testExistsById_InvalidId() throws Exception {
        // Given
        String invalidId = "invalid-id";
        doThrow(new TenantValidationException("Invalid tenant ID format"))
                .when(tenantValidationService).validateTenantId(invalidId);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{id}/exists", invalidId))
                .andExpect(status().isBadRequest());

        verify(tenantValidationService).validateTenantId(invalidId);
        verify(tenantService, never()).existsById(anyString());
    }

    @Test
    void testExistsBySubdomain_Success() throws Exception {
        // Given
        String subdomain = "test-tenant";
        when(tenantService.existsBySubdomain(subdomain)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/subdomain/{subdomain}/exists", subdomain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(tenantService).existsBySubdomain(subdomain);
    }

    @Test
    void testHealth_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tenants/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tenant service is healthy"));
    }
} 