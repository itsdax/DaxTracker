package scripts.dax.tracker.data;

import com.allatori.annotations.DoNotRename;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@DoNotRename
@ToString
@AllArgsConstructor
public class JwtContainer {
    private String token;
    private String tokenExpiration;
    private String refreshToken;
    private String refreshTokenExpiration;
}
