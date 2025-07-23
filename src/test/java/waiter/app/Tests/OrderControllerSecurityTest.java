package waiter.app.Tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import waiter.app.controllers.OrderController;
import waiter.app.services.OrderService;
import waiter.app.repositories.OrderRepository;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @Test
    public void getOrders_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"WAITER"})
    public void getOrders_AsWaiter_ShouldReturn200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WAITER"})
    public void createOrder_AsWaiter_ShouldReturn200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void refundOrder_AsAdmin_ShouldReturn200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/1/refund?reason=error"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WAITER"})
    public void refundOrder_AsWaiter_ShouldReturn403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/1/refund?reason=error"))
                .andExpect(status().isForbidden());
    }
}
