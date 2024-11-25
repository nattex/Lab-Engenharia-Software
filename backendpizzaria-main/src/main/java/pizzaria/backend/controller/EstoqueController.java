package pizzaria.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pizzaria.backend.entities.Estoque;
import pizzaria.backend.repository.EstoqueRepository;

import java.util.List;

@RestController
@RequestMapping("/estoque")
@CrossOrigin(origins = "*") // Permite acesso de qualquer origem
public class EstoqueController {
    private final EstoqueRepository estoqueRepository;

    public EstoqueController(EstoqueRepository estoqueRepository)
    {
        this.estoqueRepository = estoqueRepository;
    }

    // Listar todos os itens do estoque
    @GetMapping
    public List<Estoque> listar() {
        return estoqueRepository.findAll();
    }

    // Criar um novo item no estoque
    @PostMapping
    public Estoque criar(@RequestBody Estoque estoque) {
        return estoqueRepository.save(estoque);
    }

    // Editar um item existente
    @PutMapping("/{id}")
    public ResponseEntity<Estoque> editar(@PathVariable Long id, @RequestBody Estoque estoqueDetails) {
        return estoqueRepository.findById(id)
                .map(estoque -> {
                    estoque.setNome(estoqueDetails.getNome());
                    estoque.setUnidade(estoqueDetails.getUnidade());
                    estoque.setQuilos(estoqueDetails.getQuilos());
                    Estoque estoqueAtualizado = estoqueRepository.save(estoque);
                    return ResponseEntity.ok(estoqueAtualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Deletar um item do estoque
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return estoqueRepository.findById(id)
                .map(estoque -> {
                    estoqueRepository.delete(estoque);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
