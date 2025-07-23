package waiter.app.dto;

import lombok.Getter;
import lombok.Setter;
import waiter.app.Enums.Role;

@Getter
@Setter
public class UpdateUserRequest {
    private String fullName;
    private String email;
    private String password;
    private Role role;  // ADMIN, WAITER κ.λπ.
}
