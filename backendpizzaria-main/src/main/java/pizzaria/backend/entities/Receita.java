package pizzaria.backend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "tb_receita")
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "receita_id")
    private Long receitaId;

    private String nomeReceita;

    @ElementCollection
    private List<String> ingredientes;

}
