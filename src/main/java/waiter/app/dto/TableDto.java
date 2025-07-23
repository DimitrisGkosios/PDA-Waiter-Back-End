package waiter.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import waiter.app.Enums.TableStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableDto {

    private Long id;

    @Min(value = 1, message = "Table number must be at least 1")
    private int number;

    @NotNull(message = "Table status is required")
    private TableStatus status;

    private String assignedWaiter; // Optional, μπορεί να είναι null
}
