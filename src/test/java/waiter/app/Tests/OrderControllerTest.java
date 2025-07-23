package waiter.app.Tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import waiter.app.Enums.OrderStatus;
import waiter.app.Enums.PaymentMethod;
import waiter.app.dto.OrderDto;
import waiter.app.entities.Order;
import waiter.app.repositories.OrderRepository;
import waiter.app.services.OrderService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllOrders() throws Exception {
        OrderDto order = new OrderDto();
        order.setId(1L);

        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void testGetOrderById_found() throws Exception {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testGetOrderById_notFound() throws Exception {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));
    }

    @Test
    @WithMockUser(roles = {"WAITER"})
    public void testCreateOrder() throws Exception {
        OrderDto orderInput = new OrderDto();
        OrderDto savedOrder = new OrderDto();
        savedOrder.setId(1L);

        when(orderService.createOrder(any(OrderDto.class), eq("user"))).thenReturn(savedOrder);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderInput))
                        .principal(() -> "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testUpdateStatus() throws Exception {
        OrderDto updatedOrder = new OrderDto();
        updatedOrder.setId(1L);
        when(orderService.updateStatus(eq(1L), eq(OrderStatus.PAID))).thenReturn(updatedOrder);

        mockMvc.perform(put("/api/orders/1/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testPayOrder_success() throws Exception {
        OrderDto paidOrder = new OrderDto();
        paidOrder.setId(1L);
        when(orderService.payOrder(1L, PaymentMethod.CASH)).thenReturn(paidOrder);

        mockMvc.perform(put("/api/orders/1/pay")
                        .param("method", "CASH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testCancelOrder() throws Exception {
        OrderDto canceled = new OrderDto();
        canceled.setId(1L);

        when(orderService.cancelOrder(1L, "user")).thenReturn(canceled);

        mockMvc.perform(put("/api/orders/1/cancel")
                        .principal(() -> "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testRefundOrder() throws Exception {
        OrderDto refunded = new OrderDto();
        refunded.setId(1L);

        when(orderService.refundOrder(eq(1L), eq("admin"), eq("mistake"))).thenReturn(refunded);

        mockMvc.perform(put("/api/orders/1/refund")
                        .param("reason", "mistake")
                        .principal(() -> "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testDeleteOrder_found() throws Exception {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order deleted successfully"));
    }

    @Test
    public void testDeleteOrder_notFound() throws Exception {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));
    }
}
