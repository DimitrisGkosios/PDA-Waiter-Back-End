package waiter.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MenuItemDto {
    private Long id;
    private String name;
    private double price;
    private boolean available;
}
