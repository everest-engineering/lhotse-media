package engineering.everest.starterkit.media.thumbnails.persistence;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PersistableThumbnail {

    private UUID thumbnailFileId;
    private int width;
    private int height;

}
