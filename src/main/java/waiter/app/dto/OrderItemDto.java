package waiter.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {

    private Long id;

    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;

    @NotNull(message = "Menu item name is required")
    private String menuItemName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Size(max = 255, message = "Comments cannot exceed 255 characters")
    private String comments;
}
