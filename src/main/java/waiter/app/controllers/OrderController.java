package waiter.app.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import waiter.app.Enums.PaymentMethod;
import waiter.app.dto.AddOrderItemRequest;
import waiter.app.dto.OrderDto;
import waiter.app.Enums.OrderStatus;
import waiter.app.dto.RemoveOrderItemRequest;
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
    public ResponseEntity<?> getAll() {
        try {
            List<OrderDto> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to retrieve orders: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDto orderDto, Principal principal) {
        try {
            OrderDto createdOrder = orderService.createOrder(orderDto, principal.getName());
            return ResponseEntity.ok(createdOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Order creation failed: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        try {
            OrderDto updatedOrder = orderService.updateStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to update order status: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<?> payOrder(@PathVariable Long id, @RequestParam(name = "method") PaymentMethod method) {
        if (method == null) {
            return ResponseEntity.badRequest().body("Payment method is required: CARD or CASH");
        }
        try {
            OrderDto paidOrder = orderService.payOrder(id, method);
            return ResponseEntity.ok(paidOrder);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to pay order: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, Principal principal) {
        try {
            OrderDto canceledOrder = orderService.cancelOrder(id, principal.getName());
            return ResponseEntity.ok(canceledOrder);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to cancel order: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refundOrder(@PathVariable Long id, @RequestParam String reason, Principal principal) {
        try {
            OrderDto refundedOrder = orderService.refundOrder(id, principal.getName(), reason);
            return ResponseEntity.ok(refundedOrder);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to refund order: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            OrderDto dto = orderService.getOrderDtoById(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to retrieve order: " + e.getMessage());
        }
    }



    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            return orderRepository.findById(id)
                    .map(order -> {
                        orderRepository.delete(order);
                        return ResponseEntity.ok("Order deleted successfully");
                    })
                    .orElseGet(() -> ResponseEntity.status(404).body("Order not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete order: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/add-item")
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<?> addItem(@PathVariable Long id, @Valid @RequestBody AddOrderItemRequest request) {
        try {
            OrderDto updatedOrder = orderService.addItemToOrder(id, request);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to add item: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/remove-item")
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<?> removeItem(@PathVariable Long id, @Valid @RequestBody RemoveOrderItemRequest request) {
        try {
            OrderDto updatedOrder = orderService.removeItemFromOrder(id, request);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to remove item: " + e.getMessage());
        }
    }
}
