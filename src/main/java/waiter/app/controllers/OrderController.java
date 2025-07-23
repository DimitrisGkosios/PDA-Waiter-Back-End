package waiter.app.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import waiter.app.Enums.PaymentMethod;
import waiter.app.dto.OrderDto;
import waiter.app.Enums.OrderStatus;
import waiter.app.entities.Order;
import waiter.app.repositories.OrderRepository;
import waiter.app.services.OrderService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;


    @GetMapping
    public ResponseEntity<List<OrderDto>> getAll() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto, Principal principal) {
        OrderDto created = orderService.createOrder(orderDto, principal.getName());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payOrder(
            @PathVariable Long id,
            @RequestParam(name = "method", required = true) PaymentMethod method
    ) {
        if (method == null) {
            return ResponseEntity.badRequest().body("Payment method is required: CARD or CASH");
        }

        try {
            OrderDto paidOrder = orderService.payOrder(id, method);
            return ResponseEntity.ok(paidOrder);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to process payment: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable Long id,
            Principal principal
    ) {
        return ResponseEntity.ok(orderService.cancelOrder(id, principal.getName()));
    }

    @PutMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> refundOrder(
            @PathVariable Long id,
            @RequestParam String reason,
            Principal principal
    ) {
        return ResponseEntity.ok(orderService.refundOrder(id, principal.getName(), reason));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Order not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    orderRepository.delete(order);
                    return ResponseEntity.ok("Order deleted successfully");
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Order not found"));
    }
}
