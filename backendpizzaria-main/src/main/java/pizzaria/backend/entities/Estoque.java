package pizzaria.backend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_estoque")
public class Estoque {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "estoque_id")
    private Long estoqueId;

    private String nome;
    private Integer unidade;
    private Double  quilos;

    public Integer getQuantidade() {
        return this.unidade;
    }
}
