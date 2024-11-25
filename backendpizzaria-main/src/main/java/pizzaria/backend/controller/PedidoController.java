package pizzaria.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pizzaria.backend.entities.ItemPedido;
import pizzaria.backend.entities.Pedido;
import pizzaria.backend.entities.Receita;
import pizzaria.backend.repository.ItemPedidoRepository;
import pizzaria.backend.repository.PedidoRepository;
import pizzaria.backend.repository.ReceitaRepository;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private ReceitaRepository receitaRepository;

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedido) {
        Double valorTotal = 0.0;

        for (ItemPedido item : pedido.getItens()) {
            Receita receita = receitaRepository.findById(item.getReceita().getReceitaId())
                    .orElseThrow(() -> new RuntimeException("Receita não encontrada"));

            item.setPrecoUnitario(receita.getPreco());
            item.setPrecoTotal(item.getPrecoUnitario() * item.getQuantidade());

            valorTotal += item.getPrecoTotal();

            item.setReceita(receita);
        }

        pedido.setValorTotal(valorTotal);

        Pedido novoPedido = pedidoRepository.save(pedido);

        for (ItemPedido item : novoPedido.getItens()) {
            item.setPedidoId(novoPedido.getPedidoId());
        }

        itemPedidoRepository.saveAll(novoPedido.getItens());

        return ResponseEntity.ok(novoPedido);
    }








    // Listar todos os pedidos
    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodosPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        return ResponseEntity.ok(pedidos);
    }

    // Obter pedido por ID
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obterPedidoPorId(@PathVariable Long id) {
        Optional<Pedido> pedidoOptional = pedidoRepository.findById(id);
        return pedidoOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Atualizar pedido por ID
    @PutMapping("/{id}")
    public ResponseEntity<Pedido> atualizarPedido(@PathVariable Long id, @RequestBody Pedido pedidoAtualizado) {
        Optional<Pedido> pedidoOptional = pedidoRepository.findById(id);

        if (pedidoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Pedido pedidoExistente = pedidoOptional.get();
        pedidoExistente.setDataPedido(pedidoAtualizado.getDataPedido());
        pedidoExistente.setItens(pedidoAtualizado.getItens());
        pedidoExistente.setValorTotal(pedidoAtualizado.getValorTotal());
        Pedido pedidoSalvo = pedidoRepository.save(pedidoExistente);

        return ResponseEntity.ok(pedidoSalvo);
    }


    // Excluir pedido por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPedido(@PathVariable Long id) {
        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        pedidoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}