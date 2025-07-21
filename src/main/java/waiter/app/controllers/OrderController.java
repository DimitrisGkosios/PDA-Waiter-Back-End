package waiter.app.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import waiter.app.Enums.PaymentMethod;
import waiter.app.dto.OrderDto;
import waiter.app.Enums.OrderStatus;
import waiter.app.services.OrderService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

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
    public ResponseEntity<OrderDto> payOrder(
            @PathVariable Long id,
            @RequestParam PaymentMethod method
    ) {
        return ResponseEntity.ok(orderService.payOrder(id, method));
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
}