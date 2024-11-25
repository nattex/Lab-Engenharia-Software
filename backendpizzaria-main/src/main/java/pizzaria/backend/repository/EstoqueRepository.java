package pizzaria.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pizzaria.backend.entities.Estoque;

import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    Optional<Estoque> findByNome(String ingrediente);

    // Método para encontrar os três ingredientes com quantidade maior que 0
    @Query("SELECT e FROM Estoque e WHERE e.unidade > 0 ORDER BY e.unidade ASC")
    List<Estoque> findTop3ByQuantidadeGreaterThanOrderByQuantidadeAsc(int quantidade);
}
