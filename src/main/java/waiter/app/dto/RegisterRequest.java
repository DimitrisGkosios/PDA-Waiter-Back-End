package waiter.app.dto;

import lombok.Data;
import waiter.app.Enums.Role;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private Role role;
}