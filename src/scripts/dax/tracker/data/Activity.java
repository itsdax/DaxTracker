package scripts.dax.tracker.data;

import com.allatori.annotations.DoNotRename;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.Map;

@DoNotRename
@Getter
@ToString
public class Activity {
    private String id;
    private String userID;
    private String scriptID;
    private Date timestamp;
    private Map<String, Integer> resources;
}
