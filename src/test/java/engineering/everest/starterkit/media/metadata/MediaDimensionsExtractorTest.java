package engineering.everest.starterkit.media.metadata;

import com.drew.imaging.ImageProcessingException;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.filestorage.InputStreamOfKnownLength;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static java.lang.Thread.currentThread;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaDimensionsExtractorTest {

    private static final UUID VALID_SAMPLE_FILE_ID = randomUUID();
    private static final UUID INVALID_SAMPLE_FILE_ID = randomUUID();
    private static final MediaDimensions EXPECTED_SAMPLE_FILE_DIMENSION = new MediaDimensions(268, 188);

    private MediaDimensionsExtractor mediaDimensionsExtractor;

    @Mock
    private FileService fileService;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        mediaDimensionsExtractor = new MediaDimensionsExtractor(fileService);
    }

    @Test
    void getMediaDimension_WillRetrieveDimensionFromImage() throws Exception {
        when(fileService.stream(VALID_SAMPLE_FILE_ID)).thenReturn(createInputStream("new-image.jpg"));

        assertEquals(EXPECTED_SAMPLE_FILE_DIMENSION, mediaDimensionsExtractor.getMediaDimension(VALID_SAMPLE_FILE_ID));
    }

    @Test
    void getMediaDimension_WillFail_WhenMediaCannotBeRead() throws Exception {
        when(fileService.stream(INVALID_SAMPLE_FILE_ID)).thenReturn(createInputStream("note-to-self.txt"));

        assertThrows(ImageProcessingException.class, ()-> mediaDimensionsExtractor.getMediaDimension(INVALID_SAMPLE_FILE_ID));
    }

    private InputStreamOfKnownLength createInputStream(String filename) throws URISyntaxException, FileNotFoundException {
        File file = new File(currentThread().getContextClassLoader().getResource(filename).toURI());
        return new InputStreamOfKnownLength(new FileInputStream(file), file.length());
    }
}