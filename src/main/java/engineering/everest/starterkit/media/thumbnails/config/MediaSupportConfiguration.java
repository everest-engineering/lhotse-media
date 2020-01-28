package engineering.everest.starterkit.media.thumbnails.config;

import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import engineering.everest.starterkit.media.thumbnails.persistence.ThumbnailMappingRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("engineering.everest.starterkit.media.thumbnails.persistence")
public class MediaSupportConfiguration {

    @Bean
    public ThumbnailService thumbnailService(FileService fileService, ThumbnailMappingRepository thumbnailMappingRepository) {
        return new ThumbnailService(fileService, thumbnailMappingRepository);
    }
}
