package com.teneocast.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadMusicRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String artist;
    
    private String album;
    
    @NotNull(message = "Genre ID is required")
    private Long genreId;
    
    private String description;
}
