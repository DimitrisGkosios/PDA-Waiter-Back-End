package waiter.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoveOrderItemRequest {
    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1; // default = 1
}
