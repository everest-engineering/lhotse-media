package engineering.everest.starterkit.media.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MediaDimensions {
    private final int width;
    private final int height;
}
