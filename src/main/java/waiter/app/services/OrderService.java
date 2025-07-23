package waiter.app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waiter.app.Enums.OrderStatus;
import waiter.app.Enums.PaymentMethod;
import waiter.app.dto.AddOrderItemRequest;
import waiter.app.dto.OrderDto;
import waiter.app.dto.RemoveOrderItemRequest;
import waiter.app.entities.MenuItem;
import waiter.app.entities.Order;
import waiter.app.entities.OrderItem;
import waiter.app.mapper.OrderMapper;
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
    private final OrderMapper orderMapper;

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderDtoById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return toDto(order);
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
                            .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

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

        return orderMapper.toDto(orderRepository.save(savedOrder));
    }

    @Transactional
    public OrderDto updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(status);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto payOrder(Long orderId, PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method must be specified (CARD or CASH)");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.READY) {
            throw new IllegalStateException("Only orders with status READY can be paid");
        }

        double total = order.getItems().stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();

        order.setPaymentMethod(paymentMethod);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PAID);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Cannot cancel a paid or refunded order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto refundOrder(Long orderId, String adminUsername, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Only PAID orders can be refunded");
        }

        order.setStatus(OrderStatus.REFUNDED);
        order.setRefundedBy(adminUsername);
        order.setRefundReason(reason);
        order.setRefundedAt(LocalDateTime.now());

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto addItemToOrder(Long orderId, AddOrderItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        // Check if item already exists, increase quantity if so
        OrderItem existingItem = order.getItems().stream()
                .filter(item -> item.getMenuItem().getId().equals(request.getMenuItemId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            orderItemRepository.save(existingItem);
        } else {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(request.getQuantity());
            orderItem.setComments(request.getComments());
            orderItemRepository.save(orderItem);
            order.getItems().add(orderItem);
        }

        // Recalculate total amount
        double total = order.getItems().stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();
        order.setTotalAmount(total);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto removeItemFromOrder(Long orderId, RemoveOrderItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Use iterator for safe removal while looping
        var iterator = order.getItems().iterator();
        while (iterator.hasNext()) {
            OrderItem item = iterator.next();
            if (item.getMenuItem().getId().equals(request.getMenuItemId())) {
                if (item.getQuantity() > request.getQuantity()) {
                    item.setQuantity(item.getQuantity() - request.getQuantity());
                    orderItemRepository.save(item);
                } else {
                    iterator.remove();
                    orderItemRepository.delete(item);
                }
                break; // Found and processed the item, exit loop
            }
        }

        // Recalculate total amount
        double total = order.getItems().stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();
        order.setTotalAmount(total);

        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto toDto(Order order) {
        return orderMapper.toDto(order);
    }



}
