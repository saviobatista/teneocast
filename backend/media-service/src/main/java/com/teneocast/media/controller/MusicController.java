package com.teneocast.media.controller;

import com.teneocast.media.dto.ApiResponse;
import com.teneocast.media.dto.MusicDto;
import com.teneocast.media.dto.MusicGenreDto;
import com.teneocast.media.dto.UploadMusicRequest;
import com.teneocast.media.service.MusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/media/music")
@RequiredArgsConstructor
@Slf4j
public class MusicController {
    
    private final MusicService musicService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MusicDto>> uploadMusic(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") @NotBlank(message = "Title is required") String title,
            @RequestParam("artist") @NotBlank(message = "Artist is required") String artist,
            @RequestParam("album") String album,
            @RequestParam("genreId") @NotNull(message = "Genre ID is required") @Positive(message = "Genre ID must be positive") Long genreId,
            @RequestParam("description") String description,
            @RequestHeader("X-Tenant-ID") @NotNull(message = "Tenant ID is required") UUID tenantId) {
        
        try {
            UploadMusicRequest request = UploadMusicRequest.builder()
                    .title(title)
                    .artist(artist)
                    .album(album)
                    .genreId(genreId)
                    .description(description)
                    .build();
            
            MusicDto music = musicService.uploadMusic(file, request, tenantId);
            return ResponseEntity.ok(ApiResponse.success(music, "Music uploaded successfully"));
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error during music upload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            log.error("IO error during music upload: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to upload music file"));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MusicDto>> getMusicById(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        
        try {
            MusicDto music = musicService.getMusicById(id, tenantId);
            return ResponseEntity.ok(ApiResponse.success(music));
        } catch (IllegalArgumentException e) {
            log.error("Error getting music by ID: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MusicDto>>> getMusicByTenant(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) String search) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<MusicDto> musicPage;
            
            if (genreId != null) {
                musicPage = musicService.getMusicByTenantAndGenre(tenantId, genreId, pageable);
            } else if (search != null && !search.trim().isEmpty()) {
                musicPage = musicService.searchMusic(tenantId, search.trim(), pageable);
            } else {
                musicPage = musicService.getMusicByTenant(tenantId, pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(musicPage));
        } catch (Exception e) {
            log.error("Error getting music by tenant: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve music"));
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MusicDto>>> getAllMusicByTenant(
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        
        try {
            List<MusicDto> musicList = musicService.getAllMusicByTenant(tenantId);
            return ResponseEntity.ok(ApiResponse.success(musicList));
        } catch (Exception e) {
            log.error("Error getting all music by tenant: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve music"));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMusic(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        
        try {
            musicService.deleteMusic(id, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Music deleted successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Error deleting music: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            log.error("IO error during music deletion: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to delete music file"));
        }
    }
    
    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<MusicGenreDto>>> getAllGenres() {
        try {
            List<MusicGenreDto> genres = musicService.getAllGenres();
            return ResponseEntity.ok(ApiResponse.success(genres));
        } catch (Exception e) {
            log.error("Error getting music genres: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve music genres"));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMusic(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        
        try {
            MusicDto music = musicService.getMusicById(id, tenantId);
            
            // Get the file from storage
            Resource fileResource = musicService.downloadMusicFile(id, tenantId);
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + music.getTitle() + "." + music.getFileFormat() + "\"")
                    .header("Content-Type", "audio/" + music.getFileFormat())
                    .body(fileResource);
                    
        } catch (IllegalArgumentException e) {
            log.error("Error downloading music: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("IO error during music download: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamMusic(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        
        try {
            MusicDto music = musicService.getMusicById(id, tenantId);
            
            // Get the file from storage for streaming
            Resource fileResource = musicService.downloadMusicFile(id, tenantId);
            
            return ResponseEntity.ok()
                    .header("Content-Type", "audio/" + music.getFileFormat())
                    .header("Accept-Ranges", "bytes")
                    .body(fileResource);
                    
        } catch (IllegalArgumentException e) {
            log.error("Error streaming music: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("IO error during music streaming: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
