package waiter.app.Tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import waiter.app.controllers.UserController;
import waiter.app.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void accessDeniedWithoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void accessAllowedForAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "waiter", roles = {"WAITER"})
    public void accessDeniedForWaiter() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isForbidden());
    }
}
