package pizzaria.backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "tb_pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "pedido_id")
    private Long pedidoId;

    private String cliente;

    @OneToMany(cascade = CascadeType.ALL)
    private List<ItemPedido> itens; // Lista de itens do pedido

    private LocalDateTime dataPedido;
    private String status;
    private Double valorTotal;
}
