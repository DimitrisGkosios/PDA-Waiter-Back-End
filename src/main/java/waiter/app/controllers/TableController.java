package waiter.app.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import waiter.app.dto.TableDto;
import waiter.app.Enums.TableStatus;
import waiter.app.services.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<TableDto>> getAll() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<TableDto> updateStatus(
            @PathVariable Long id,
            @RequestParam TableStatus status,
            Principal principal
    ) {
        return ResponseEntity.ok(
                tableService.updateStatus(id, status, principal.getName())
        );
    }
}
