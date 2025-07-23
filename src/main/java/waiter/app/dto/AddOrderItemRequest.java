package waiter.app.dto;

import lombok.Data;

@Data
public class AddOrderItemRequest {
    private Long menuItemId;
    private int quantity;
    private String comments;
}

