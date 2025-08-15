package com.teneocast.media.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.tika.Tika;
import java.io.InputStream;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class MediaProcessingServiceTest {

    @Mock
    private Tika tika;

    private MediaProcessingService mediaProcessingService;

    @BeforeEach
    void setUp() {
        mediaProcessingService = new MediaProcessingService(tika);
        ReflectionTestUtils.setField(mediaProcessingService, "allowedAudioFormats", "mp3,wav,ogg,m4a,aac");
        ReflectionTestUtils.setField(mediaProcessingService, "maxFileSize", "100MB");
        
        // Mock Tika to return audio MIME type for our test files
        try {
            lenient().when(tika.detect(any(InputStream.class), eq("test.mp3")))
                .thenReturn("audio/mpeg");
            lenient().when(tika.detect(any(InputStream.class), eq("test.xyz")))
                .thenReturn("audio/mpeg");
            lenient().when(tika.detect(any(InputStream.class), eq("test.txt")))
                .thenReturn("text/plain");
        } catch (IOException e) {
            // This shouldn't happen in tests
            throw new RuntimeException(e);
        }
    }

    @Test
    void testValidateFile_ValidAudioFile() {
        // Create a mock file that will pass validation
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "fake mp3 content".getBytes()
        );

        // This should not throw an exception
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
                "audio/mpeg",  // This MIME type will pass the first check
                "test audio content".getBytes()
        );

        // This should fail on extension validation, not MIME type
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
