package waiter.app.mapper;

import org.springframework.stereotype.Component;
import waiter.app.dto.OrderDto;
import waiter.app.dto.OrderItemDto;
import waiter.app.entities.Order;
import waiter.app.entities.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        List<OrderItemDto> itemDtos = (order.getItems() == null) ?
                List.of() :
                order.getItems().stream()
                        .filter(item -> item != null && item.getMenuItem() != null) // extra safety
                        .map(this::toItemDto)
                        .collect(Collectors.toList());

        return new OrderDto(
                order.getId(),
                order.getWaiterUsername(),
                order.getStatus(),
                itemDtos,
                order.getRefundedBy(),
                order.getRefundReason(),
                order.getRefundedAt()
        );
    }

    private OrderItemDto toItemDto(OrderItem item) {
        if (item == null || item.getMenuItem() == null) {
            // Αν θέλεις, μπορείς να ρίξεις εξαίρεση ή να επιστρέψεις null ή default τιμές
            return null; // ή throw new IllegalStateException("Invalid OrderItem data");
        }

        return new OrderItemDto(
                item.getId(),
                item.getMenuItem().getId(),
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getComments()
        );
    }
}
