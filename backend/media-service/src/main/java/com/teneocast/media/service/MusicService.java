package com.teneocast.media.service;

import com.teneocast.media.dto.MusicDto;
import com.teneocast.media.dto.MusicGenreDto;
import com.teneocast.media.dto.UploadMusicRequest;
import com.teneocast.media.entity.Music;
import com.teneocast.media.entity.MusicGenre;
import com.teneocast.media.repository.MusicGenreRepository;
import com.teneocast.media.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicService {
    
    private final MusicRepository musicRepository;
    private final MusicGenreRepository musicGenreRepository;
    private final StorageService storageService;
    private final MediaProcessingService mediaProcessingService;
    private final MeterRegistry meterRegistry;
    
    private Counter musicUploadCounter;
    private Counter musicDeleteCounter;
    private Counter musicDownloadCounter;
    
    @PostConstruct
    public void init() {
        // Initialize counters
        this.musicUploadCounter = Counter.builder("music.uploads.total")
                .description("Total number of music uploads")
                .register(meterRegistry);
        this.musicDeleteCounter = Counter.builder("music.deletes.total")
                .description("Total number of music deletions")
                .register(meterRegistry);
        this.musicDownloadCounter = Counter.builder("music.downloads.total")
                .description("Total number of music downloads")
                .register(meterRegistry);
    }
    
    /**
     * Upload music file
     * @param file The audio file
     * @param request Upload request with metadata
     * @param tenantId The tenant ID
     * @return The created music DTO
     * @throws IOException If upload fails
     */
    @Transactional
    @Timed(value = "music.upload", longTask = true)
    public MusicDto uploadMusic(MultipartFile file, UploadMusicRequest request, UUID tenantId) throws IOException {
        log.info("Uploading music file: name={}, tenant={}", file.getOriginalFilename(), tenantId);
        
        // Validate file
        mediaProcessingService.validateFile(file);
        
        // Check if music already exists
        if (musicRepository.existsByTenantIdAndTitleAndArtist(tenantId, request.getTitle(), request.getArtist())) {
            throw new IllegalArgumentException("Music with this title and artist already exists for this tenant");
        }
        
        // Get genre
        MusicGenre genre = musicGenreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid genre ID: " + request.getGenreId()));
        
        // Upload file to storage
        String filePath = storageService.uploadFile(file, tenantId, "music");
        
        // Extract metadata
        String metadata = mediaProcessingService.extractMetadata(file);
        Integer duration = mediaProcessingService.getFileDuration(file);
        
        // Create music entity
        Music music = Music.builder()
                .tenantId(tenantId)
                .genre(genre)
                .title(request.getTitle())
                .artist(request.getArtist())
                .album(request.getAlbum())
                .durationSeconds(duration)
                .filePath(filePath)
                .fileSize(file.getSize())
                .fileFormat(getFileExtension(file.getOriginalFilename()))
                .metadata(metadata)
                .build();
        
        // Save to database
        Music savedMusic = musicRepository.save(music);
        
        // Increment metrics
        musicUploadCounter.increment();
        
        log.info("Music uploaded successfully: id={}, title={}, artist={}", 
                savedMusic.getId(), savedMusic.getTitle(), savedMusic.getArtist());
        
        return mapToDto(savedMusic);
    }
    
    /**
     * Get music by ID
     * @param id Music ID
     * @param tenantId Tenant ID for validation
     * @return Music DTO
     */
    public MusicDto getMusicById(Long id, UUID tenantId) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Music not found with ID: " + id));
        
        if (!music.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied to music from different tenant");
        }
        
        return mapToDto(music);
    }
    
    /**
     * Get music by tenant with pagination
     * @param tenantId Tenant ID
     * @param pageable Pagination parameters
     * @return Page of music DTOs
     */
    public Page<MusicDto> getMusicByTenant(UUID tenantId, Pageable pageable) {
        Page<Music> musicPage = musicRepository.findByTenantId(tenantId, pageable);
        return musicPage.map(this::mapToDto);
    }
    
    /**
     * Get music by tenant and genre
     * @param tenantId Tenant ID
     * @param genreId Genre ID
     * @param pageable Pagination parameters
     * @return Page of music DTOs
     */
    public Page<MusicDto> getMusicByTenantAndGenre(UUID tenantId, Long genreId, Pageable pageable) {
        Page<Music> musicPage = musicRepository.findByTenantIdAndGenreId(tenantId, genreId, pageable);
        return musicPage.map(this::mapToDto);
    }
    
    /**
     * Search music by tenant and search term
     * @param tenantId Tenant ID
     * @param searchTerm Search term
     * @param pageable Pagination parameters
     * @return Page of music DTOs
     */
    public Page<MusicDto> searchMusic(UUID tenantId, String searchTerm, Pageable pageable) {
        Page<Music> musicPage = musicRepository.findByTenantIdAndSearchTerm(tenantId, searchTerm, pageable);
        return musicPage.map(this::mapToDto);
    }
    
    /**
     * Get all music for tenant (for player service)
     * @param tenantId Tenant ID
     * @return List of music DTOs
     */
    public List<MusicDto> getAllMusicByTenant(UUID tenantId) {
        List<Music> musicList = musicRepository.findAllByTenantId(tenantId);
        return musicList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete music file
     * @param id Music ID
     * @param tenantId Tenant ID for validation
     * @throws IOException If deletion fails
     */
    @Transactional
    @Timed(value = "music.delete", longTask = true)
    public void deleteMusic(Long id, UUID tenantId) throws IOException {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Music not found with ID: " + id));
        
        if (!music.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied to music from different tenant");
        }
        
        // Delete from storage
        storageService.deleteFile(music.getFilePath(), tenantId);
        
        // Delete from database
        musicRepository.delete(music);
        
        // Increment metrics
        musicDeleteCounter.increment();
        
        log.info("Music deleted successfully: id={}, title={}", id, music.getTitle());
    }
    
    /**
     * Get all music genres
     * @return List of genre DTOs
     */
    public List<MusicGenreDto> getAllGenres() {
        List<MusicGenre> genres = musicGenreRepository.findAll();
        return genres.stream()
                .map(this::mapGenreToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Download music file
     * @param id Music ID
     * @param tenantId Tenant ID for validation
     * @return File resource
     * @throws IOException If download fails
     */
    @Timed(value = "music.download", longTask = true)
    public Resource downloadMusicFile(Long id, UUID tenantId) throws IOException {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Music not found with ID: " + id));
        
        if (!music.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied to music from different tenant");
        }
        
        // Increment metrics
        musicDownloadCounter.increment();
        
        // Get file from storage
        return storageService.getFileAsResource(music.getFilePath(), tenantId);
    }
    
    private MusicDto mapToDto(Music music) {
        return MusicDto.builder()
                .id(music.getId())
                .tenantId(music.getTenantId())
                .genreId(music.getGenre() != null ? music.getGenre().getId() : null)
                .genreName(music.getGenre() != null ? music.getGenre().getName() : null)
                .title(music.getTitle())
                .artist(music.getArtist())
                .album(music.getAlbum())
                .durationSeconds(music.getDurationSeconds())
                .filePath(music.getFilePath())
                .fileSize(music.getFileSize())
                .fileFormat(music.getFileFormat())
                .bitrate(music.getBitrate())
                .metadata(music.getMetadata())
                .createdAt(music.getCreatedAt())
                .updatedAt(music.getUpdatedAt())
                .build();
    }
    
    private MusicGenreDto mapGenreToDto(MusicGenre genre) {
        return MusicGenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .description(genre.getDescription())
                .createdAt(genre.getCreatedAt())
                .updatedAt(genre.getUpdatedAt())
                .build();
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
