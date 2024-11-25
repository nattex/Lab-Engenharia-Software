package pizzaria.backend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_item_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receita_id")
    private Receita receita;

    private Integer quantidade;
    private Double precoUnitario;
    private Double precoTotal;

    @Column(name = "pedido_id")
    private Long pedidoId;
}
