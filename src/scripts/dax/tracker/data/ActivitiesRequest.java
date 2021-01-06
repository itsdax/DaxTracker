package scripts.dax.tracker.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActivitiesRequest {
    private String scriptID;
    private String period;
}
