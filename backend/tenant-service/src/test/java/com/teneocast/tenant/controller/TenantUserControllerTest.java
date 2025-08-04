package com.teneocast.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teneocast.tenant.dto.CreateUserRequest;
import com.teneocast.tenant.dto.TenantUserDto;
import com.teneocast.tenant.dto.UpdateUserRequest;
import com.teneocast.tenant.exception.TenantNotFoundException;
import com.teneocast.tenant.exception.TenantValidationException;
import com.teneocast.tenant.service.TenantUserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TenantUserControllerTest {

    @Mock
    private TenantUserService tenantUserService;

    @Mock
    private TenantValidationService tenantValidationService;

    @InjectMocks
    private TenantUserController tenantUserController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String tenantId;
    private TenantUserDto testUserDto;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tenantUserController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        tenantId = UUID.randomUUID().toString();

        testUserDto = TenantUserDto.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .email("test@example.com")
                .role(TenantUserDto.UserRole.MASTER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createUserRequest = CreateUserRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .role(TenantUserDto.UserRole.MASTER)
                .build();

        updateUserRequest = UpdateUserRequest.builder()
                .email("updated@example.com")
                .role(TenantUserDto.UserRole.PRODUCER)
                .isActive(true)
                .build();
    }

    @Test
    void testCreateUser_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.createTenantUser(eq(tenantId), any(CreateUserRequest.class))).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/users", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testUserDto.getId()))
                .andExpect(jsonPath("$.email").value(testUserDto.getEmail()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).createTenantUser(eq(tenantId), any(CreateUserRequest.class));
    }

    @Test
    void testCreateUser_TenantNotFound() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.createTenantUser(eq(tenantId), any(CreateUserRequest.class)))
                .thenThrow(new TenantNotFoundException("Tenant not found"));

        // When & Then
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/users", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).createTenantUser(eq(tenantId), any(CreateUserRequest.class));
    }

    @Test
    void testCreateUser_ValidationError() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.createTenantUser(eq(tenantId), any(CreateUserRequest.class)))
                .thenThrow(new TenantValidationException("Validation error"));

        // When & Then
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/users", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).createTenantUser(eq(tenantId), any(CreateUserRequest.class));
    }

    @Test
    void testGetUserByEmail_Success() throws Exception {
        // Given
        String email = "test@example.com";
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.getUserByEmail(tenantId, email)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/users/email/{email}", tenantId, email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserDto.getId()))
                .andExpect(jsonPath("$.email").value(testUserDto.getEmail()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).getUserByEmail(tenantId, email);
    }

    @Test
    void testGetUserByEmail_NotFound() throws Exception {
        // Given
        String email = "test@example.com";
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.getUserByEmail(tenantId, email))
                .thenThrow(new TenantNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/users/email/{email}", tenantId, email))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).getUserByEmail(tenantId, email);
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // Given
        String userId = testUserDto.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.updateUser(eq(tenantId), eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(put("/api/v1/tenants/{tenantId}/users/{userId}", tenantId, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).updateUser(eq(tenantId), eq(userId), any(UpdateUserRequest.class));
    }

    @Test
    void testUpdateUser_NotFound() throws Exception {
        // Given
        String userId = testUserDto.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.updateUser(eq(tenantId), eq(userId), any(UpdateUserRequest.class)))
                .thenThrow(new TenantNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/tenants/{tenantId}/users/{userId}", tenantId, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).updateUser(eq(tenantId), eq(userId), any(UpdateUserRequest.class));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // Given
        String userId = testUserDto.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        doNothing().when(tenantUserService).deleteUser(tenantId, userId);

        // When & Then
        mockMvc.perform(delete("/api/v1/tenants/{tenantId}/users/{userId}", tenantId, userId))
                .andExpect(status().isNoContent());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).deleteUser(tenantId, userId);
    }

    @Test
    void testDeleteUser_NotFound() throws Exception {
        // Given
        String userId = testUserDto.getId();
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        doThrow(new TenantNotFoundException("User not found")).when(tenantUserService).deleteUser(tenantId, userId);

        // When & Then
        mockMvc.perform(delete("/api/v1/tenants/{tenantId}/users/{userId}", tenantId, userId))
                .andExpect(status().isNotFound());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).deleteUser(tenantId, userId);
    }

    @Test
    void testGetUsersByTenantId_Success() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<TenantUserDto> userPage = new PageImpl<>(List.of(testUserDto), pageable, 1);
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        // validatePagination is void method, no need to mock return value
        when(tenantUserService.getUsersByTenantId(eq(tenantId), any())).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/users", tenantId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testUserDto.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantValidationService).validatePagination(0, 20);
        verify(tenantUserService).getUsersByTenantId(eq(tenantId), any());
    }

    @Test
    void testGetUsersByTenantId_InvalidPagination() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        doThrow(new TenantValidationException("Invalid pagination parameters"))
                .when(tenantValidationService).validatePagination(-1, 20);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/users", tenantId)
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantValidationService).validatePagination(-1, 20);
    }

    @Test
    void testGetUsersByRole_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.getUsersByRole(eq(tenantId), any())).thenReturn(List.of(testUserDto));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/users/role/MASTER", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testUserDto.getId()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).getUsersByRole(eq(tenantId), any());
    }

    @Test
    void testGetActiveUsers_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.getActiveUsers(tenantId)).thenReturn(List.of(testUserDto));

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/users/active", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testUserDto.getId()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).getActiveUsers(tenantId);
    }

    @Test
    void testGetUsersCount_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.getUsersCount(tenantId)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/users/count", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).getUsersCount(tenantId);
    }

    @Test
    void testGetActiveUsersCount_Success() throws Exception {
        // Given
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.getActiveUsersCount(tenantId)).thenReturn(3L);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/users/count/active", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).getActiveUsersCount(tenantId);
    }

    @Test
    void testExistsByEmail_Success() throws Exception {
        // Given
        String email = "test@example.com";
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.existsByEmail(tenantId, email)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/users/email/{email}/exists", tenantId, email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).existsByEmail(tenantId, email);
    }

    @Test
    void testAuthenticateUser_Success() throws Exception {
        // Given
        String email = "test@example.com";
        String password = "password123";
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.authenticateUser(tenantId, email, password))
                .thenReturn(java.util.Optional.of(testUserDto));

        // When & Then
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/users/authenticate", tenantId)
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserDto.getId()));

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).authenticateUser(tenantId, email, password);
    }

    @Test
    void testAuthenticateUser_Unauthorized() throws Exception {
        // Given
        String email = "test@example.com";
        String password = "wrongpassword";
        doNothing().when(tenantValidationService).validateTenantId(tenantId);
        when(tenantUserService.authenticateUser(tenantId, email, password))
                .thenReturn(java.util.Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/v1/tenants/{tenantId}/users/authenticate", tenantId)
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isUnauthorized());

        verify(tenantValidationService).validateTenantId(tenantId);
        verify(tenantUserService).authenticateUser(tenantId, email, password);
    }

    @Test
    void testHealth_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/users/health", tenantId))
                .andExpect(status().isOk())
                .andExpect(content().string("Tenant user service is healthy for tenant: " + tenantId));
    }
} 