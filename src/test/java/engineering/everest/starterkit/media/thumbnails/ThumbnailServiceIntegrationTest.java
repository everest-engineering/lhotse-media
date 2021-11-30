package engineering.everest.starterkit.media.thumbnails;

import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.filestorage.InputStreamOfKnownLength;
import engineering.everest.starterkit.media.thumbnails.persistence.PersistableThumbnail;
import engineering.everest.starterkit.media.thumbnails.persistence.PersistableThumbnailMapping;
import engineering.everest.starterkit.media.thumbnails.persistence.ThumbnailMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static java.lang.Thread.currentThread;
import static java.nio.file.Files.createTempFile;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ComponentScan("engineering.everest.starterkit.media.thumbnails")
@EnableJpaRepositories("engineering.everest.starterkit.media.thumbnails.persistence")
@EntityScan("engineering.everest.starterkit.media.thumbnails.persistence")
class ThumbnailServiceIntegrationTest {
    private static final int SMALL_WIDTH = 600;
    private static final int SMALL_HEIGHT = 400;
    private static final int LARGE_WIDTH = 800;
    private static final int LARGE_HEIGHT = 600;
    private static final int HUGE_DIMENSION = 90000;
    private static final UUID SOURCE_FILE_ID_1 = randomUUID();
    private static final UUID SOURCE_FILE_ID_2 = randomUUID();
    private static final UUID SOURCE_FILE_1_THUMBNAIL_ID_1 = randomUUID();
    private static final UUID SOURCE_FILE_1_THUMBNAIL_ID_2 = randomUUID();
    private static final UUID SOURCE_FILE_2_THUMBNAIL_ID_1 = randomUUID();
    private static final PersistableThumbnail FILE_1_THUMBNAIL_1 =
        new PersistableThumbnail(SOURCE_FILE_1_THUMBNAIL_ID_1, SMALL_WIDTH, SMALL_HEIGHT);
    private static final PersistableThumbnail FILE_1_THUMBNAIL_2 =
        new PersistableThumbnail(SOURCE_FILE_1_THUMBNAIL_ID_2, LARGE_WIDTH, LARGE_HEIGHT);
    private static final PersistableThumbnail FILE_2_THUMBNAIL_1 =
        new PersistableThumbnail(SOURCE_FILE_2_THUMBNAIL_ID_1, SMALL_WIDTH, SMALL_HEIGHT);
    private static final int MAX_DIMENSION_PIXELS = 2400;

    @Autowired
    private ThumbnailService thumbnailService;
    @Autowired
    private ThumbnailMappingRepository thumbnailMappingRepository;

    @MockBean
    private FileService fileService;

    private InputStream file1Thumbnail1InputStream;
    private InputStream file1Thumbnail2InputStream;
    private InputStream file2Thumbnail1InputStream;

    @BeforeEach
    void setUp() throws IOException {
        thumbnailService = new ThumbnailService(MAX_DIMENSION_PIXELS, fileService, thumbnailMappingRepository);

        thumbnailMappingRepository.save(new PersistableThumbnailMapping(SOURCE_FILE_ID_1, List.of(FILE_1_THUMBNAIL_1, FILE_1_THUMBNAIL_2)));
        thumbnailMappingRepository.save(new PersistableThumbnailMapping(SOURCE_FILE_ID_2, List.of(FILE_2_THUMBNAIL_1)));

        file1Thumbnail1InputStream = new ByteArrayInputStream("file-1-thumbnail-1-file-contents".getBytes());
        file1Thumbnail2InputStream = new ByteArrayInputStream("file-1-thumbnail-2-file-contents".getBytes());
        file2Thumbnail1InputStream = new ByteArrayInputStream("file-2-thumbnail-1-file-contents".getBytes());

        when(fileService.createTemporaryFile("thumbnail")).thenReturn(createTempFile("unit", "test").toFile());
        when(fileService.stream(SOURCE_FILE_1_THUMBNAIL_ID_1)).thenReturn(new InputStreamOfKnownLength(file1Thumbnail1InputStream, 11L));
        when(fileService.stream(SOURCE_FILE_1_THUMBNAIL_ID_2)).thenReturn(new InputStreamOfKnownLength(file1Thumbnail2InputStream, 22L));
        when(fileService.stream(SOURCE_FILE_2_THUMBNAIL_ID_1)).thenReturn(new InputStreamOfKnownLength(file2Thumbnail1InputStream, 33L));
    }

    @Test
    void willStreamExistingThumbnailForGivenDimension_WhenCachedInEphemeralFileStore() throws IOException {
        assertEquals(file1Thumbnail1InputStream,
            thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, SMALL_WIDTH, SMALL_HEIGHT));
        assertEquals(file1Thumbnail2InputStream,
            thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, LARGE_WIDTH, LARGE_HEIGHT));
        assertEquals(file2Thumbnail1InputStream,
            thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_2, SMALL_WIDTH, SMALL_HEIGHT));

        verify(fileService, never()).transferToEphemeralStore(anyString(), any(InputStream.class));
        verify(fileService, never()).transferToEphemeralStore(any(InputStream.class));
    }

    @Test
    void willCreateThumbnailOnTheFlyAndPersistInEphemeralStore_WhenSourceFileHasNoCachedThumbnails() throws IOException {
        UUID newSourceFileId = randomUUID();
        UUID newThumbnailFileId = randomUUID();

        String expectedThumbnailFilename = String.format("%s-thumbnail-%sx%s.png", newSourceFileId, SMALL_WIDTH, SMALL_HEIGHT);
        when(fileService.stream(newSourceFileId)).thenReturn(getTestInputStream("new-image.jpg", 100L));
        when(fileService.transferToEphemeralStore(eq(expectedThumbnailFilename), any(InputStream.class))).thenReturn(newThumbnailFileId);
        InputStream newThumbnailInputStream = mock(InputStream.class);
        when(fileService.stream(newThumbnailFileId)).thenReturn(new InputStreamOfKnownLength(newThumbnailInputStream, 100L));

        assertEquals(newThumbnailInputStream, thumbnailService.streamThumbnailForOriginalFile(newSourceFileId, SMALL_WIDTH, SMALL_HEIGHT));

        var expectedThumbnailMapping = new PersistableThumbnailMapping(newSourceFileId,
            List.of(new PersistableThumbnail(newThumbnailFileId, SMALL_WIDTH, SMALL_HEIGHT)));
        assertEquals(expectedThumbnailMapping, thumbnailMappingRepository.findById(newSourceFileId).orElseThrow());
    }

    @Test
    void willUpdateThumbnailsForExistingSourceFile_WhenThumbnailsExistForDifferentDimensions() throws IOException {
        UUID newThumbnailFileId = randomUUID();

        int newWidth = 1234;
        int newHeight = 2222;
        String expectedThumbnailFilename = String.format("%s-thumbnail-%sx%s.png", SOURCE_FILE_ID_1, newWidth, newHeight);
        when(fileService.transferToEphemeralStore(eq(expectedThumbnailFilename), any(InputStream.class))).thenReturn(newThumbnailFileId);
        when(fileService.stream(SOURCE_FILE_ID_1)).thenReturn(getTestInputStream("existing-image.jpg", 123L));
        InputStream newThumbnailInputStream = mock(InputStream.class);
        when(fileService.stream(newThumbnailFileId)).thenReturn(new InputStreamOfKnownLength(newThumbnailInputStream, 123L));

        assertEquals(newThumbnailInputStream, thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, newWidth, newHeight));

        var expectedThumbnailMapping = new PersistableThumbnailMapping(SOURCE_FILE_ID_1,
            List.of(FILE_1_THUMBNAIL_1, FILE_1_THUMBNAIL_2, new PersistableThumbnail(newThumbnailFileId, newWidth, newHeight)));
        assertEquals(expectedThumbnailMapping, thumbnailMappingRepository.findById(SOURCE_FILE_ID_1).orElseThrow());
    }

    @Test
    void willFail_WhenThumbnailSizeIsTooSmall() {
        assertThrows(IllegalArgumentException.class,
            () -> thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, SMALL_WIDTH, 0));
        assertThrows(IllegalArgumentException.class,
            () -> thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, 0, SMALL_HEIGHT));
    }

    @Test
    void willFail_WhenThumbnailSizeIsTooLarge() {
        assertThrows(IllegalArgumentException.class,
            () -> thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, SMALL_WIDTH, HUGE_DIMENSION));
        assertThrows(IllegalArgumentException.class,
            () -> thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, HUGE_DIMENSION, SMALL_HEIGHT));
    }

    private InputStreamOfKnownLength getTestInputStream(String filename, long fileSize) {
        return new InputStreamOfKnownLength(currentThread().getContextClassLoader().getResourceAsStream(filename), fileSize);
    }
}
