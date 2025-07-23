package waiter.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import waiter.app.Enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderDto {
    private Long id;

    @NotNull(message = "Waiter username is required")
    private String waiterUsername;

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    @NotEmpty(message = "Order must contain at least one item")
    private List<@Valid OrderItemDto> items;

    private String refundedBy;
    private String refundReason;
    private LocalDateTime refundedAt;


    public OrderDto(Long id, String waiterUsername, OrderStatus status, List<OrderItemDto> items,
                    String refundedBy, String refundReason, LocalDateTime refundedAt) {
        this.id = id;
        this.waiterUsername = waiterUsername;
        this.status = status;
        this.items = items;
        this.refundedBy = refundedBy;
        this.refundReason = refundReason;
        this.refundedAt = refundedAt;
    }
}
