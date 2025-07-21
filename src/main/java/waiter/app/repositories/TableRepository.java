package waiter.app.repositories;

import waiter.app.entities.Tables;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableRepository extends JpaRepository<Tables, Long> {
}