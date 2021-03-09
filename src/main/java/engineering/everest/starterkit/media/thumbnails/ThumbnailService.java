package engineering.everest.starterkit.media.thumbnails;

import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.media.thumbnails.persistence.PersistableThumbnail;
import engineering.everest.starterkit.media.thumbnails.persistence.PersistableThumbnailMapping;
import engineering.everest.starterkit.media.thumbnails.persistence.ThumbnailMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static net.coobird.thumbnailator.Thumbnailator.createThumbnail;

/**
 * Service layer for thumbnails.
 * <p>
 * The maximum size of either height or width of a generated thumbnail can be configured using the
 * {@code application.media.thumbnail.max.dimension.pixels} property.
 */
@Component
public class ThumbnailService {

    private final int maxDimensionPixels;
    private final FileService fileService;
    private final ThumbnailMappingRepository thumbnailMappingRepository;

    @Autowired
    public ThumbnailService(@Value("${application.media.thumbnail.max.dimension.pixels:2400}") int maxDimensionPixels,
                            FileService fileService,
                            ThumbnailMappingRepository thumbnailMappingRepository) {
        this.maxDimensionPixels = maxDimensionPixels;
        this.fileService = fileService;
        this.thumbnailMappingRepository = thumbnailMappingRepository;
    }

    /**
     * Generates a thumbnail for a file stored by one of the deduplicating filestores.
     * <p>
     * Generated thumbnails are cached on the ephemeral file store. Callers are responsible for closing the returned
     * input stream.
     *
     * @param fileId UUID returned by the {@code PermanentDeduplicatingFileStore} or the {@code EphemeralDefuplicatingFileStore}
     * @param width desired thumbnail width
     * @param height desried thumbnail height
     * @return an input stream to retrieve a thumbnail.
     * @throws IOException if the original file cannot be read
     * @throws IllegalArgumentException if the thumbnail dimensions are impossibly small or larger than the maximum supported.
     */
    public InputStream streamThumbnailForOriginalFile(UUID fileId, int width, int height) throws IOException {
        var existingMapping = findExistingThumbnail(fileId, width, height);

        var thumbnailFileId = existingMapping.isPresent()
                ? existingMapping.get().getThumbnailFileId()
                : createThumbnailForOriginalFile(fileId, width, height);

        return fileService.stream(thumbnailFileId).getInputStream();
    }

    private Optional<PersistableThumbnail> findExistingThumbnail(UUID fileId, int width, int height) {
        Optional<PersistableThumbnailMapping> thumbnailMapping = thumbnailMappingRepository.findById(fileId);

        if (thumbnailMapping.isPresent()) {
            return thumbnailMapping.get().getThumbnails().stream()
                    .filter(x -> x.getWidth() == width && x.getHeight() == height)
                    .findFirst();
        }
        return Optional.empty();
    }

    private UUID createThumbnailForOriginalFile(UUID fileId, int width, int height) throws IOException {
        throwIfThumbnailDimensionsUnreasonable(width, height);

        var tempFile = fileService.createTemporaryFile();
        try (var originalInputStream = fileService.stream(fileId).getInputStream();
             var thumbnailOutputStream = newOutputStream(tempFile.toPath())) {
            createThumbnail(originalInputStream, thumbnailOutputStream, width, height);
            return persistThumbnailAndUpdateMapping(fileId, width, height, tempFile,
                    String.format("%s-thumbnail-%sx%s.png", fileId, width, height));
        } finally {
            Files.delete(tempFile.toPath());
        }
    }

    private void throwIfThumbnailDimensionsUnreasonable(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Thumbnail dimension can't be less than 1");
        }
        if (width > maxDimensionPixels || height > maxDimensionPixels) {
            throw new IllegalArgumentException(String.format("Thumbnail dimension cannot exceed %s pixels", maxDimensionPixels));
        }
    }

    private UUID persistThumbnailAndUpdateMapping(UUID originalFileId, int width, int height,
                                                  File tempFile, String thumbnailFilename) throws IOException {
        try (var thumbnailInputStream = newInputStream(tempFile.toPath())) {
            var thumbnailFileID = fileService.transferToEphemeralStore(thumbnailFilename, thumbnailInputStream);
            var thumbnailMapping = thumbnailMappingRepository.findById(originalFileId)
                    .orElseGet(() -> new PersistableThumbnailMapping(originalFileId, new ArrayList<>()));
            thumbnailMapping.addThumbnail(thumbnailFileID, width, height);
            thumbnailMappingRepository.save(thumbnailMapping);
            return thumbnailFileID;
        }
    }
}
