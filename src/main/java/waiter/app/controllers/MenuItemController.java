package waiter.app.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import waiter.app.dto.MenuItemDto;
import waiter.app.services.MenuItemService;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<List<MenuItemDto>> getAll() {
        return ResponseEntity.ok(menuItemService.getAllMenuItems());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemDto> create(@Valid @RequestBody MenuItemDto dto) {
        return ResponseEntity.ok(menuItemService.createMenuItem(dto));
    }
}
