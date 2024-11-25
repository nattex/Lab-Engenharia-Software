package pizzaria.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pizzaria.backend.entities.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
