package engineering.everest.starterkit.media.thumbnails.persistence;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.FetchType.EAGER;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "thumbnailedfiles")
public class PersistableThumbnailMapping {

    @Id
    private UUID sourceFileId;

    @ElementCollection(fetch = EAGER)
    private List<PersistableThumbnail> thumbnails = new ArrayList<>();

    public void addThumbnail(UUID thumbnailFileId, int width, int height) {
        thumbnails.add(new PersistableThumbnail(thumbnailFileId, width, height));
    }
}
