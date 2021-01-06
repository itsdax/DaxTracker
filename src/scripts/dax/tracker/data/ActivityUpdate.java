package scripts.dax.tracker.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ActivityUpdate {
    private String scriptId;
    private Map<String, Long> resources;
}
