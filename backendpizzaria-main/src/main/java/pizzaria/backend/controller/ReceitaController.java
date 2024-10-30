package pizzaria.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pizzaria.backend.entities.Estoque;
import pizzaria.backend.entities.Receita;
import pizzaria.backend.repository.EstoqueRepository;
import pizzaria.backend.repository.ReceitaRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/receita")
@CrossOrigin(origins = "*") // Permite acesso de qualquer origem
public class ReceitaController {

    private final ReceitaRepository receitaRepository;
    private final EstoqueRepository estoqueRepository;

    public ReceitaController(ReceitaRepository receitaRepository, EstoqueRepository estoqueRepository) {
        this.receitaRepository = receitaRepository;
        this.estoqueRepository = estoqueRepository;
    }

    @GetMapping("/verificar-ingredientes")
    public ResponseEntity<String> verificarIngredientes(@RequestParam Long receitaId) {
        Optional<Receita> receitaOptional = receitaRepository.findById(receitaId);

        if (receitaOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receita não encontrada.");
        }

        Receita receita = receitaOptional.get();
        List<String> ingredientes = receita.getIngredientes(); // Supondo que a receita tenha um método para retornar os ingredientes

        for (String ingrediente : ingredientes) {
            Optional<Estoque> estoqueOptional = estoqueRepository.findByNome(ingrediente);
            if (estoqueOptional.isEmpty() || estoqueOptional.get().getQuantidade() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ingrediente " + ingrediente + " não disponível no estoque.");
            }
        }

        return ResponseEntity.ok("Todos os ingredientes da estão disponíveis.");
    }

    @PostMapping("/criar")
    public ResponseEntity<Receita> criarReceita(@RequestBody Receita novaReceita) {
        // Aqui você pode adicionar qualquer validação necessária

        Receita receitaSalva = receitaRepository.save(novaReceita);
        return ResponseEntity.status(HttpStatus.CREATED).body(receitaSalva);
    }

    @GetMapping
    public List<Receita> listar() {
        return receitaRepository.findAll();
    }

    @GetMapping("/receitas-sugeridas")
    public ResponseEntity<List<Receita>> obterReceitasSugeridas() {
        // Obter os três ingredientes com menor quantidade, maiores que 0
        List<Estoque> ingredientesBaixaQuantidade = estoqueRepository.findTop3ByQuantidadeGreaterThanOrderByQuantidadeAsc(0);

        // Listar nomes dos ingredientes
        List<String> nomesIngredientes = ingredientesBaixaQuantidade.stream()
                .map(Estoque::getNome)
                .toList();

        // Buscar receitas que contenham pelo menos um dos ingredientes
        List<Receita> receitasSugeridas = receitaRepository.findByIngredientesIn(nomesIngredientes);

        // Verificar se alguma receita foi encontrada
        if (receitasSugeridas.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content se não houver receitas
        }

        return ResponseEntity.ok(receitasSugeridas);
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<Receita> editarReceita(@PathVariable Long id, @RequestBody Receita receitaAtualizada) {
        return receitaRepository.findById(id)
                .map(receita -> {
                    receita.setNomeReceita(receitaAtualizada.getNomeReceita());
                    receita.setIngredientes(receitaAtualizada.getIngredientes());
                    return ResponseEntity.ok(receitaRepository.save(receita));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletarReceita(@PathVariable Long id) {
        if (receitaRepository.existsById(id)) {
            receitaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


}
