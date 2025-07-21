package waiter.app.dto;

import waiter.app.Enums.TableStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableDto {
    private Long id;
    private int number;
    private TableStatus status;
    private String assignedWaiter;
}
