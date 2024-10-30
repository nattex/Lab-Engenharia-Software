package pizzaria.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pizzaria.backend.entities.Receita;

import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    @Query("SELECT r FROM Receita r JOIN r.ingredientes i WHERE i IN :ingredientes")
    List<Receita> findByIngredientesIn(List<String> ingredientes);
}
