package com.teneocast.tenant.service;

import com.teneocast.tenant.dto.CreateTenantRequest;
import com.teneocast.tenant.dto.TenantDto;
import com.teneocast.tenant.dto.UpdateTenantRequest;
import com.teneocast.tenant.entity.Tenant;
import com.teneocast.tenant.exception.DuplicateSubdomainException;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.repository.TenantRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantValidationService tenantValidationService;

    @InjectMocks
    private TenantService tenantService;

    private Tenant testTenant;
    private CreateTenantRequest createRequest;
    private UpdateTenantRequest updateRequest;

    @BeforeEach
    void setUp() {
        testTenant = Tenant.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Tenant")
                .subdomain("test-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateTenantRequest.builder()
                .name("Test Tenant")
                .subdomain("test-tenant")
                .build();

        updateRequest = UpdateTenantRequest.builder()
                .name("Updated Tenant")
                .subdomain("updated-tenant")
                .status(TenantDto.TenantStatus.ACTIVE)
                .build();
    }

    @Test
    void testCreateTenant_Success() {
        // Given
        when(tenantRepository.existsBySubdomain(createRequest.getSubdomain())).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // When
        TenantDto result = tenantService.createTenant(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(testTenant.getId(), result.getId());
        assertEquals(testTenant.getName(), result.getName());
        assertEquals(testTenant.getSubdomain(), result.getSubdomain());
        verify(tenantValidationService).validateCreateTenantRequest(createRequest);
        verify(tenantRepository).existsBySubdomain(createRequest.getSubdomain());
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void testCreateTenant_DuplicateSubdomain() {
        // Given
        when(tenantRepository.existsBySubdomain(createRequest.getSubdomain())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateSubdomainException.class, () -> tenantService.createTenant(createRequest));
        verify(tenantValidationService).validateCreateTenantRequest(createRequest);
        verify(tenantRepository).existsBySubdomain(createRequest.getSubdomain());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void testGetTenantById_Success() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));

        // When
        TenantDto result = tenantService.getTenantById(testTenant.getId());

        // Then
        assertNotNull(result);
        assertEquals(testTenant.getId(), result.getId());
        assertEquals(testTenant.getName(), result.getName());
        verify(tenantRepository).findById(testTenant.getId());
    }

    @Test
    void testGetTenantById_NotFound() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantService.getTenantById(testTenant.getId()));
        verify(tenantRepository).findById(testTenant.getId());
    }

    @Test
    void testGetTenantBySubdomain_Success() {
        // Given
        when(tenantRepository.findBySubdomain(testTenant.getSubdomain())).thenReturn(Optional.of(testTenant));

        // When
        TenantDto result = tenantService.getTenantBySubdomain(testTenant.getSubdomain());

        // Then
        assertNotNull(result);
        assertEquals(testTenant.getId(), result.getId());
        assertEquals(testTenant.getSubdomain(), result.getSubdomain());
        verify(tenantRepository).findBySubdomain(testTenant.getSubdomain());
    }

    @Test
    void testGetTenantBySubdomain_NotFound() {
        // Given
        when(tenantRepository.findBySubdomain(testTenant.getSubdomain())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantService.getTenantBySubdomain(testTenant.getSubdomain()));
        verify(tenantRepository).findBySubdomain(testTenant.getSubdomain());
    }

    @Test
    void testUpdateTenant_Success() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        when(tenantRepository.existsBySubdomain(updateRequest.getSubdomain())).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // When
        TenantDto result = tenantService.updateTenant(testTenant.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(testTenant.getId(), result.getId());
        verify(tenantValidationService).validateUpdateTenantRequest(updateRequest);
        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantRepository).existsBySubdomain(updateRequest.getSubdomain());
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void testUpdateTenant_NotFound() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantService.updateTenant(testTenant.getId(), updateRequest));
        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void testUpdateTenant_DuplicateSubdomain() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        when(tenantRepository.existsBySubdomain(updateRequest.getSubdomain())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateSubdomainException.class, () -> tenantService.updateTenant(testTenant.getId(), updateRequest));
        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantRepository).existsBySubdomain(updateRequest.getSubdomain());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void testUpdateTenant_SameSubdomain() {
        // Given
        updateRequest.setSubdomain(testTenant.getSubdomain());
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // When
        TenantDto result = tenantService.updateTenant(testTenant.getId(), updateRequest);

        // Then
        assertNotNull(result);
        verify(tenantRepository).findById(testTenant.getId());
        verify(tenantRepository, never()).existsBySubdomain(anyString());
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void testDeleteTenant_Success() {
        // Given
        when(tenantRepository.existsById(testTenant.getId())).thenReturn(true);

        // When
        tenantService.deleteTenant(testTenant.getId());

        // Then
        verify(tenantRepository).existsById(testTenant.getId());
        verify(tenantRepository).deleteById(testTenant.getId());
    }

    @Test
    void testDeleteTenant_NotFound() {
        // Given
        when(tenantRepository.existsById(testTenant.getId())).thenReturn(false);

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> tenantService.deleteTenant(testTenant.getId()));
        verify(tenantRepository).existsById(testTenant.getId());
        verify(tenantRepository, never()).deleteById(anyString());
    }

    @Test
    void testGetAllTenants_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tenant> tenantPage = new PageImpl<>(List.of(testTenant), pageable, 1);
        when(tenantRepository.findAll(pageable)).thenReturn(tenantPage);

        // When
        Page<TenantDto> result = tenantService.getAllTenants(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testTenant.getId(), result.getContent().get(0).getId());
        verify(tenantRepository).findAll(pageable);
    }

    @Test
    void testGetTenantsByStatus_Success() {
        // Given
        Tenant.TenantStatus status = Tenant.TenantStatus.ACTIVE;
        when(tenantRepository.findByStatus(status)).thenReturn(List.of(testTenant));

        // When
        List<TenantDto> result = tenantService.getTenantsByStatus(status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTenant.getId(), result.get(0).getId());
        verify(tenantRepository).findByStatus(status);
    }

    @Test
    void testGetTenantsByStatusWithPagination_Success() {
        // Given
        Tenant.TenantStatus status = Tenant.TenantStatus.ACTIVE;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tenant> tenantPage = new PageImpl<>(List.of(testTenant), pageable, 1);
        when(tenantRepository.findByStatus(status, pageable)).thenReturn(tenantPage);

        // When
        Page<TenantDto> result = tenantService.getTenantsByStatus(status, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testTenant.getId(), result.getContent().get(0).getId());
        verify(tenantRepository).findByStatus(status, pageable);
    }

    @Test
    void testSearchTenantsByName_Success() {
        // Given
        String searchName = "Test";
        when(tenantRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(List.of(testTenant));

        // When
        List<TenantDto> result = tenantService.searchTenantsByName(searchName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTenant.getId(), result.get(0).getId());
        verify(tenantRepository).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    void testGetActiveTenantsCount_Success() {
        // Given
        when(tenantRepository.countByStatus(Tenant.TenantStatus.ACTIVE)).thenReturn(5L);

        // When
        long result = tenantService.getActiveTenantsCount();

        // Then
        assertEquals(5L, result);
        verify(tenantRepository).countByStatus(Tenant.TenantStatus.ACTIVE);
    }

    @Test
    void testExistsById_Success() {
        // Given
        when(tenantRepository.existsById(testTenant.getId())).thenReturn(true);

        // When
        boolean result = tenantService.existsById(testTenant.getId());

        // Then
        assertTrue(result);
        verify(tenantRepository).existsById(testTenant.getId());
    }

    @Test
    void testExistsBySubdomain_Success() {
        // Given
        when(tenantRepository.existsBySubdomain(testTenant.getSubdomain())).thenReturn(true);

        // When
        boolean result = tenantService.existsBySubdomain(testTenant.getSubdomain());

        // Then
        assertTrue(result);
        verify(tenantRepository).existsBySubdomain(testTenant.getSubdomain());
    }

    @Test
    void testGetTenantEntityById_Success() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.of(testTenant));

        // When
        Optional<Tenant> result = tenantService.getTenantEntityById(testTenant.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testTenant.getId(), result.get().getId());
        verify(tenantRepository).findById(testTenant.getId());
    }

    @Test
    void testGetTenantEntityById_NotFound() {
        // Given
        when(tenantRepository.findById(testTenant.getId())).thenReturn(Optional.empty());

        // When
        Optional<Tenant> result = tenantService.getTenantEntityById(testTenant.getId());

        // Then
        assertFalse(result.isPresent());
        verify(tenantRepository).findById(testTenant.getId());
    }
} 