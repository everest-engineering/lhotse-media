package engineering.everest.starterkit.media.thumbnails.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThumbnailMappingRepository extends JpaRepository<PersistableThumbnailMapping, UUID> {}
