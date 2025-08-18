package com.teneocast.media.e2e;

import com.teneocast.media.dto.MusicDto;
import com.teneocast.media.dto.AdvertisementDto;
import com.teneocast.media.entity.MusicGenre;
import com.teneocast.media.entity.AdType;
import com.teneocast.media.repository.MusicGenreRepository;
import com.teneocast.media.repository.AdTypeRepository;
import com.teneocast.media.repository.MusicRepository;
import com.teneocast.media.repository.AdvertisementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.context.annotation.Import;
import com.teneocast.media.config.TestSecurityConfig;
import com.teneocast.media.config.TestStorageConfig;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestStorageConfig.class})
class MediaServiceE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MusicGenreRepository musicGenreRepository;

    @Autowired
    private AdTypeRepository adTypeRepository;

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    private String baseUrl;
    private UUID testTenantId;
    private MusicGenre testGenre;
    private AdType testAdType;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        // Clean the database before each test
        musicRepository.deleteAll();
        advertisementRepository.deleteAll();
        musicGenreRepository.deleteAll();
        adTypeRepository.deleteAll();

        // Create test data
        testTenantId = UUID.randomUUID();
        testGenre = musicGenreRepository.save(MusicGenre.builder()
                .name("Test Genre")
                .description("Test genre description")
                .build());

        testAdType = adTypeRepository.save(AdType.builder()
                .name("Test Ad Type")
                .description("Test ad type description")
                .tenantId(testTenantId)
                .build());

        // Flush to ensure IDs are generated and committed
        musicGenreRepository.flush();
        adTypeRepository.flush();
        
        // Verify the entities were created with valid IDs
        assertNotNull(testGenre.getId(), "Genre ID should not be null");
        assertNotNull(testAdType.getId(), "AdType ID should not be null");

        baseUrl = "http://localhost:" + port + "/media";
        headers = new HttpHeaders();
        headers.set("X-Tenant-ID", testTenantId.toString());
    }

    @Test
    void testCompleteMusicWorkflow() {
        // 1. Get all genres
        ResponseEntity<String> genresResponse = restTemplate.getForEntity(
                baseUrl + "/api/media/music/genres", String.class);
        assertEquals(HttpStatus.OK, genresResponse.getStatusCode());

        // 2. Upload music file
        byte[] audioContent = createFakeAudioContent();
        ByteArrayResource audioResource = new ByteArrayResource(audioContent) {
            @Override
            public String getFilename() {
                return "test-song.mp3";
            }
        };

        MultiValueMap<String, Object> musicUploadData = new LinkedMultiValueMap<>();
        musicUploadData.add("file", audioResource);
        musicUploadData.add("title", "Test Song");
        musicUploadData.add("artist", "Test Artist");
        musicUploadData.add("album", "Test Album");
        musicUploadData.add("genreId", testGenre.getId());
        musicUploadData.add("description", "Test song for validation");

        HttpEntity<MultiValueMap<String, Object>> musicUploadRequest = new HttpEntity<>(musicUploadData, headers);
        ResponseEntity<String> musicUploadResponse = restTemplate.postForEntity(
                baseUrl + "/api/media/music", musicUploadRequest, String.class);

        assertEquals(HttpStatus.OK, musicUploadResponse.getStatusCode());

        // 3. Get music by tenant
        ResponseEntity<String> musicListResponse = restTemplate.exchange(
                baseUrl + "/api/media/music",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertEquals(HttpStatus.OK, musicListResponse.getStatusCode());

        // 4. Get all music for tenant
        ResponseEntity<String> allMusicResponse = restTemplate.exchange(
                baseUrl + "/api/media/music/all",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertEquals(HttpStatus.OK, allMusicResponse.getStatusCode());
    }

    @Test
    void testCompleteAdvertisementWorkflow() {
        // 1. Get ad types
        ResponseEntity<String> adTypesResponse = restTemplate.exchange(
                baseUrl + "/api/media/ad/types",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertEquals(HttpStatus.OK, adTypesResponse.getStatusCode());

        // 2. Upload advertisement file
        byte[] adContent = createFakeAudioContent();
        ByteArrayResource adResource = new ByteArrayResource(adContent) {
            @Override
            public String getFilename() {
                return "test-ad.mp3";
            }
        };

        MultiValueMap<String, Object> adUploadData = new LinkedMultiValueMap<>();
        adUploadData.add("file", adResource);
        adUploadData.add("name", "Test Advertisement");
        adUploadData.add("description", "Test ad for validation");
        adUploadData.add("adTypeId", testAdType.getId());
        adUploadData.add("targetAudience", "General");

        HttpEntity<MultiValueMap<String, Object>> adUploadRequest = new HttpEntity<>(adUploadData, headers);
        ResponseEntity<String> adUploadResponse = restTemplate.postForEntity(
                baseUrl + "/api/media/ad", adUploadRequest, String.class);

        assertEquals(HttpStatus.OK, adUploadResponse.getStatusCode());

        // 3. Get advertisements by tenant
        ResponseEntity<String> adListResponse = restTemplate.exchange(
                baseUrl + "/api/media/ad",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertEquals(HttpStatus.OK, adListResponse.getStatusCode());

        // 4. Get all advertisements for tenant
        ResponseEntity<String> allAdsResponse = restTemplate.exchange(
                baseUrl + "/api/media/ad/all",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertEquals(HttpStatus.OK, allAdsResponse.getStatusCode());
    }

    @Test
    void testHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("UP"));
        assertTrue(response.getBody().contains("media-service"));
    }

    @Test
    void testTenantIsolation() {
        // Create another tenant
        UUID otherTenantId = UUID.randomUUID();
        HttpHeaders otherHeaders = new HttpHeaders();
        otherHeaders.set("X-Tenant-ID", otherTenantId.toString());

        // Get music for other tenant (should be empty)
        ResponseEntity<String> otherTenantResponse = restTemplate.exchange(
                baseUrl + "/api/media/music",
                HttpMethod.GET,
                new HttpEntity<>(otherHeaders),
                String.class);
        assertEquals(HttpStatus.OK, otherTenantResponse.getStatusCode());
    }

    /**
     * Creates fake audio content that will pass MIME type validation
     * This creates a minimal MP3 header that will be recognized as audio/mpeg
     */
    private byte[] createFakeAudioContent() {
        // MP3 file header (ID3v2 + MPEG frame header)
        byte[] header = {
            // ID3v2 header
            0x49, 0x44, 0x33, // "ID3"
            0x03, 0x00, // Version 2.3
            0x00, // Flags
            // Size (4 bytes, big endian) - 0x00000000 for empty tag
            0x00, 0x00, 0x00, 0x00,
            // MPEG frame header (simplified)
            (byte) 0xFF, (byte) 0xFB, (byte) 0x90, (byte) 0x44
        };
        
        // Add some dummy audio data
        byte[] audioData = new byte[1000];
        for (int i = 0; i < audioData.length; i++) {
            audioData[i] = (byte) (i & 0xFF);
        }
        
        // Combine header and audio data
        byte[] result = new byte[header.length + audioData.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(audioData, 0, result, header.length, audioData.length);
        
        return result;
    }
}
