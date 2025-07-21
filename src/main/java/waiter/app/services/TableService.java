package waiter.app.services;

import waiter.app.dto.TableDto;
import waiter.app.entities.Tables;
import waiter.app.Enums.TableStatus;
import waiter.app.repositories.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;

    public List<TableDto> getAllTables() {
        return tableRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TableDto updateStatus(Long tableId, TableStatus status, String waiter) {
        Tables table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        table.setStatus(status);
        table.setAssignedWaiter(waiter);
        return toDto(tableRepository.save(table));
    }

    private TableDto toDto(Tables table) {
        return new TableDto(table.getId(), table.getNumber(), table.getStatus(), table.getAssignedWaiter());
    }
}
