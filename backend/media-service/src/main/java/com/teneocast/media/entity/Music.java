package com.teneocast.media.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "music", schema = "media")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Music {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    private MusicGenre genre;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(length = 255)
    private String artist;
    
    @Column(length = 255)
    private String album;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "file_format", length = 20)
    private String fileFormat;
    
    @Column
    private Integer bitrate;
    
    @Column(columnDefinition = "JSONB")
    private String metadata;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
