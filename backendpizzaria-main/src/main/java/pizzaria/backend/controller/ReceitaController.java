package pizzaria.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pizzaria.backend.entities.Estoque;
import pizzaria.backend.entities.Receita;
import pizzaria.backend.repository.EstoqueRepository;
import pizzaria.backend.repository.ReceitaRepository;

import java.util.*;

@RestController
@RequestMapping("/receita")
@CrossOrigin(origins = "*")
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
        List<String> ingredientes = receita.getIngredientes();
        List<String> ingredientesIndisponiveis = new ArrayList<>();
        Map<String, String> sugestoesSubstituto = new HashMap<>();

        for (String ingrediente : ingredientes) {
            Optional<Estoque> estoqueOptional = estoqueRepository.findByNome(ingrediente);
            if (estoqueOptional.isEmpty() || estoqueOptional.get().getQuantidade() <= 0) {
                ingredientesIndisponiveis.add(ingrediente);

                // Busca um substituto
                Optional<String> substituto = buscarSubstituto(ingrediente);
                if (substituto.isPresent()) {
                    sugestoesSubstituto.put(ingrediente, substituto.get());
                }
            }
        }

        if (!ingredientesIndisponiveis.isEmpty()) {
            String mensagem = formatarMensagemIngredientes(ingredientesIndisponiveis, sugestoesSubstituto);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensagem);
        }

        return ResponseEntity.ok("Todos os ingredientes estão disponíveis.");
    }

    private Optional<String> buscarSubstituto(String ingrediente) {
        // Substituições simples com base no nome do ingrediente
        switch (ingrediente.toLowerCase()) {
            case "queijo mussarela":
                return Optional.of("Queijo Prato");
            case "queijo":
                return Optional.of("Queijo Cheddar");
            case "calabresa":
                return Optional.of("Peperone");
            case "molho de tomate":
                return Optional.of("Molho de Tomate Caseiro");
            case "catupiry":
                return Optional.of("Creme de Leite");
            case "atum":
                return Optional.of("Frango Desfiado");
            case "farinha":
                return Optional.of("Amido de Milho");
            default:
                break;
        }

        // Substituições por categoria
        if (ingrediente.equalsIgnoreCase("Queijo")) {
            return Optional.of("Queijo Gouda");  // Outro tipo de queijo
        } else if (ingrediente.equalsIgnoreCase("Carne")) {
            return Optional.of("Frango Grelhado");  // Substituto para carnes
        } else if (ingrediente.equalsIgnoreCase("Molho de Tomate")) {
            return Optional.of("Molho Pesto");  // Substituto de molho, dependendo da receita
        } else if (ingrediente.equalsIgnoreCase("Farinha")) {
            return Optional.of("Farinha de Trigo Integral");  // Alternativa de farinha
        } else if (ingrediente.equalsIgnoreCase("Frango")) {
            return Optional.of("Carne Moída");  // Carne substituta para o frango
        }

        // Se não encontrar substituto direto ou por categoria, retornar a mensagem padrão
        return Optional.of("Substituto não encontrado");
    }



    private String formatarMensagemIngredientes(List<String> ingredientesIndisponiveis, Map<String, String> sugestoesSubstituto) {
        StringBuilder mensagem = new StringBuilder();

        int total = ingredientesIndisponiveis.size();
        for (int i = 0; i < total; i++) {
            String ingrediente = ingredientesIndisponiveis.get(i);

            // Adiciona o ingrediente faltante
            mensagem.append(ingrediente);

            // Verifica se há sugestão de substituto
            if (sugestoesSubstituto.containsKey(ingrediente)) {
                String substituto = sugestoesSubstituto.get(ingrediente);
                if ("Substituto não encontrado".equals(substituto)) {
                    mensagem.append(" / Substituto não encontrado");
                } else {
                    mensagem.append(" / ").append(substituto);
                }
            } else {
                mensagem.append(" / Substituto não encontrado");
            }

            if (i < total - 1) {
                mensagem.append("| ");
            }
        }

        return mensagem.toString();
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

        // Lista para armazenar as receitas sugeridas
        List<Receita> receitasSugeridas = new ArrayList<>();

        // Passo 1: Buscar receitas que utilizam os 3 ingredientes
        receitasSugeridas.addAll(receitaRepository.findByIngredientesIn(nomesIngredientes));


        if (receitasSugeridas.size() >= 4) {
            return ResponseEntity.ok(receitasSugeridas.subList(0, 4));
        }

        // Passo 2: Buscar receitas que utilizam 2 dos 3 ingredientes
        List<Receita> receitasComDoisIngredientes = new ArrayList<>();
        for (int i = 0; i < nomesIngredientes.size(); i++) {
            for (int j = i + 1; j < nomesIngredientes.size(); j++) {
                List<String> doisIngredientes = List.of(nomesIngredientes.get(i), nomesIngredientes.get(j));
                receitasComDoisIngredientes.addAll(receitaRepository.findByIngredientesIn(doisIngredientes));
            }
        }

        // Adiciona receitas com 2 ingredientes
        receitasSugeridas.addAll(receitasComDoisIngredientes);

        // Se já encontramos 4 receitas, retorna
        if (receitasSugeridas.size() >= 4) {
            return ResponseEntity.ok(receitasSugeridas.subList(0, 4));
        }

        // Passo 3: Buscar receitas que utilizam 1 dos 3 ingredientes
        for (String ingrediente : nomesIngredientes) {
            List<String> umIngrediente = List.of(ingrediente);
            receitasSugeridas.addAll(receitaRepository.findByIngredientesIn(umIngrediente));

            // Se já encontramos 4 receitas, retorna
            if (receitasSugeridas.size() >= 4) {
                return ResponseEntity.ok(receitasSugeridas.subList(0, 4));
            }
        }

        // Se não encontrar 4 receitas, retorna as que foram encontradas até o momento
        return ResponseEntity.ok(receitasSugeridas.subList(0, Math.min(4, receitasSugeridas.size())));
    }



    @PutMapping("/editar/{id}")
    public ResponseEntity<Receita> editarReceita(@PathVariable Long id, @RequestBody Receita receitaAtualizada) {
        return receitaRepository.findById(id)
                .map(receita -> {
                    receita.setNomeReceita(receitaAtualizada.getNomeReceita());
                    receita.setIngredientes(receitaAtualizada.getIngredientes());
                    receita.setPreco(receitaAtualizada.getPreco());
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
