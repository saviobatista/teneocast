package com.teneocast.media.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MediaProcessingServiceTest {

    @InjectMocks
    private MediaProcessingService mediaProcessingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mediaProcessingService, "allowedAudioFormats", "mp3,wav,ogg,m4a,aac");
        ReflectionTestUtils.setField(mediaProcessingService, "maxFileSize", "100MB");
    }

    @Test
    void testValidateFile_ValidAudioFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "test audio content".getBytes()
        );

        assertDoesNotThrow(() -> mediaProcessingService.validateFile(file));
    }

    @Test
    void testValidateFile_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mediaProcessingService.validateFile(file)
        );

        assertEquals("File cannot be empty", exception.getMessage());
    }

    @Test
    void testValidateFile_InvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mediaProcessingService.validateFile(file)
        );

        assertTrue(exception.getMessage().contains("Invalid file type"));
    }

    @Test
    void testValidateFile_InvalidExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xyz",
                "audio/mpeg",
                "test audio content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mediaProcessingService.validateFile(file)
        );

        assertTrue(exception.getMessage().contains("Invalid file extension"));
    }

    @Test
    void testExtractMetadata_ValidAudioFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "test audio content".getBytes()
        );

        String metadata = mediaProcessingService.extractMetadata(file);
        assertNotNull(metadata);
        assertTrue(metadata.startsWith("{"));
        assertTrue(metadata.endsWith("}"));
    }

    @Test
    void testGetFileDuration_ValidAudioFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "test audio content".getBytes()
        );

        Integer duration = mediaProcessingService.getFileDuration(file);
        // Currently returns null as it's a placeholder implementation
        assertNull(duration);
    }
}
