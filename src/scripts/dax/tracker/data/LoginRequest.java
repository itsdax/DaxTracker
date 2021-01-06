package scripts.dax.tracker.data;

import com.allatori.annotations.DoNotRename;
import lombok.AllArgsConstructor;
import lombok.Getter;

@DoNotRename
@Getter
@AllArgsConstructor
public class LoginRequest {
    private String userId;
    private String secretKey;
    private String refreshToken;
}
