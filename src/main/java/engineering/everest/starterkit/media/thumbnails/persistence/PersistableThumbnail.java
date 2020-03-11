package engineering.everest.starterkit.media.thumbnails.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
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
