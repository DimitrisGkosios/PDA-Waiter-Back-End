package waiter.app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import waiter.app.dto.MenuItemDto;
import waiter.app.entities.MenuItem;
import waiter.app.repositories.MenuItemRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public List<MenuItemDto> getAllMenuItems() {
        return menuItemRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MenuItemDto createMenuItem(MenuItemDto dto) {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(dto.getName());
        menuItem.setPrice(dto.getPrice());
        menuItem.setAvailable(dto.isAvailable());

        MenuItem saved = menuItemRepository.save(menuItem);
        return toDto(saved);
    }

    private MenuItemDto toDto(MenuItem menuItem) {
        return new MenuItemDto(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), menuItem.isAvailable());
    }
}
