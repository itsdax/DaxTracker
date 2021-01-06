package scripts.dax.tracker.data;

import com.allatori.annotations.DoNotRename;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@DoNotRename
public class UserCredentials {
    private String name;
    private String id;
    private String secretKey;
}
