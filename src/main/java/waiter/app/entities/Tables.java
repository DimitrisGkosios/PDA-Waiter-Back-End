package waiter.app.entities;

import jakarta.persistence.*;
import lombok.*;
import waiter.app.Enums.TableStatus;

@Entity
@Table(name = "tables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tables {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int number;

    @Enumerated(EnumType.STRING)
    private TableStatus status;

    private String assignedWaiter;
}
