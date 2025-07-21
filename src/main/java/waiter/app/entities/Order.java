package waiter.app.entities;

import jakarta.persistence.*;
import lombok.*;
import waiter.app.Enums.OrderStatus;
import waiter.app.Enums.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String waiterUsername;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Double totalAmount;

    private String refundedBy;

    private String refundReason;

    private LocalDateTime refundedAt;
}
