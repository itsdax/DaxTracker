package scripts.dax.tracker.data;

import com.allatori.annotations.DoNotRename;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@DoNotRename
public class UserCreationRequest {
    private String name;
}
