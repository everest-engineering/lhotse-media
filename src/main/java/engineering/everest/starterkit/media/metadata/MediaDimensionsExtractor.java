package engineering.everest.starterkit.media.metadata;

import com.drew.metadata.Directory;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import engineering.everest.starterkit.filestorage.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.drew.imaging.ImageMetadataReader.readMetadata;

/**
 * Extracts the dimensions of images and videos stored on the {@code PermanentDeduplicatingFileStore} and the
 * {@code EphemeralDeduplicatingFileStore}.
 */
@Component
public class MediaDimensionsExtractor {

    private static final String WIDTH_KEY = "width";
    private static final String HEIGHT_KEY = "height";

    private final FileService fileService;

    @Autowired
    public MediaDimensionsExtractor(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Retrieve media dimensions.
     *
     * @param fileId UUID returned by the {@code PermanentDeduplicatingFileStore} or the {@code EphemeralDefuplicatingFileStore}
     * @return media dimensions
     * @throws Exception if the dimensions could not be extracted
     */
    public MediaDimensions getMediaDimension(UUID fileId) throws Exception {
        var dimensionsMap = new HashMap<String, Integer>();

        try (var streamOfKnownLength = fileService.stream(fileId)) {
            var metadata = readMetadata(streamOfKnownLength.getInputStream(), streamOfKnownLength.getLength());
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    updateWidthOrHeightIfTagIsRelevant(dimensionsMap, directory, tag);
                }
            }
        }
        return new MediaDimensions(dimensionsMap.get(WIDTH_KEY), dimensionsMap.get(HEIGHT_KEY));
    }

    private void updateWidthOrHeightIfTagIsRelevant(Map<String, Integer> dimensionsMap, Directory directory, Tag tag) {
        String tagName = tag.getTagName().toLowerCase();
        try {
            if (tagName.contains(WIDTH_KEY)) {
                setValueIfGreater(dimensionsMap, WIDTH_KEY, directory, tag);
            } else if (tagName.contains(HEIGHT_KEY)) {
                setValueIfGreater(dimensionsMap, HEIGHT_KEY, directory, tag);
            }
        } catch (MetadataException e) {
            throw new RuntimeException(e);
        }
    }

    private void setValueIfGreater(Map<String, Integer> dimensionsMap, String key, Directory directory, Tag tag) throws MetadataException {
        var newValue = directory.getInt(tag.getTagType());
        if (newValue > dimensionsMap.getOrDefault(key, -1)) {
            dimensionsMap.put(key, newValue);
        }
    }
}
