package waiter.app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waiter.app.Enums.PaymentMethod;
import waiter.app.Enums.OrderStatus;
import waiter.app.dto.OrderDto;
import waiter.app.dto.OrderItemDto;
import waiter.app.entities.MenuItem;
import waiter.app.entities.Order;
import waiter.app.entities.OrderItem;
import waiter.app.repositories.MenuItemRepository;
import waiter.app.repositories.OrderItemRepository;
import waiter.app.repositories.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto createOrder(OrderDto orderDto, String waiterName) {
        Order order = new Order();
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(LocalDateTime.now());
        order.setWaiterUsername(waiterName);
        order.setTotalAmount(0.0);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> items = orderDto.getItems()
                .stream()
                .map(itemDto -> {
                    MenuItem menuItem = menuItemRepository.findById(itemDto.getMenuItemId())
                            .orElseThrow(() -> new RuntimeException("Menu item not found"));

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(savedOrder);
                    orderItem.setMenuItem(menuItem);
                    orderItem.setQuantity(itemDto.getQuantity());
                    orderItem.setComments(itemDto.getComments());
                    return orderItemRepository.save(orderItem);
                }).collect(Collectors.toList());

        savedOrder.setItems(items);
        savedOrder.setTotalAmount(
                items.stream().mapToDouble(i -> i.getQuantity() * i.getMenuItem().getPrice()).sum()
        );

        return toDto(orderRepository.save(savedOrder));
    }

    public OrderDto updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return toDto(orderRepository.save(order));
    }

    public OrderDto payOrder(Long orderId, PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method must be specified (CARD or CASH)");
        }

        if (order.getStatus() != OrderStatus.READY) {
            throw new IllegalStateException("Only orders with status READY can be paid");
        }

        double total = order.getItems().stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();

        order.setPaymentMethod(paymentMethod);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PAID);

        return toDto(orderRepository.save(order));
    }

    public OrderDto cancelOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Cannot cancel a paid or refunded order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return toDto(orderRepository.save(order));
    }

    public OrderDto refundOrder(Long orderId, String adminUsername, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Only PAID orders can be refunded");
        }

        order.setStatus(OrderStatus.REFUNDED);
        order.setRefundedBy(adminUsername);
        order.setRefundReason(reason);
        order.setRefundedAt(LocalDateTime.now());

        return toDto(orderRepository.save(order));
    }

    private OrderDto toDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getId(),
                        item.getMenuItem().getId(),
                        item.getMenuItem().getName(),
                        item.getQuantity(),
                        item.getComments()
                )).collect(Collectors.toList());

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
}
