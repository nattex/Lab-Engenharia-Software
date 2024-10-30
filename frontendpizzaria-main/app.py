from flask import Flask, render_template, request, redirect, url_for
import requests
import os

app = Flask(__name__)


@app.route('/')
def home():
    return render_template('home.html')  # Renderiza a nova página inicial

@app.route('/adicionar', methods=['GET', 'POST'])
def index():
    if request.method == 'POST':
        # Captura os dados do formulário
        nome_produto = request.form.get('produto')
        quantidade_unidades = request.form.get('quantidade')
        quantidade_kg = request.form.get('quantidade_kg')

        # Monta o payload para enviar ao backend em Spring
        payload = {
            "nome": nome_produto,
            "unidade": int(quantidade_unidades),
            "quilos": float(quantidade_kg)
        }

        # Faz a requisição POST para o backend Spring
        try:
            response = requests.post('https://backendpizzaria-jw9a.onrender.com/estoque', json=payload)
            response.raise_for_status()  # Garante que a resposta seja válida
            return redirect(url_for('estoque'))
        except requests.exceptions.RequestException as e:
            print(f"Erro ao enviar dados para o backend Spring: {e}")
            return "Erro ao enviar dados para o backend.", 500

    return render_template('index.html')

@app.route('/estoque')
def estoque():
    try:
        response = requests.get('https://backendpizzaria-jw9a.onrender.com/estoque')
        response.raise_for_status()
        estoque_data = response.json()  # Processa o JSON recebido
    except requests.exceptions.RequestException as e:
        print(f"Erro ao obter dados de estoque do backend Spring: {e}")
        estoque_data = []

    return render_template('estoque.html', estoque=estoque_data)

@app.route('/receitas')
def receitas():
    try:
        # Faz a requisição para obter as receitas sugeridas
        response = requests.get('https://backendpizzaria-jw9a.onrender.com/receita/receitas-sugeridas')
        response.raise_for_status()
        receitas_sugeridas = response.json()  # Processa o JSON recebido
    except requests.exceptions.RequestException as e:
        print(f"Erro ao obter dados de receitas sugeridas do backend Spring: {e}")
        receitas_sugeridas = []  # Caso haja erro, retorna uma lista vazia

    # Agora, vamos buscar os ingredientes do estoque
    try:
        response = requests.get('https://backendpizzaria-jw9a.onrender.com/estoque')  # Endpoint para obter o estoque
        response.raise_for_status()
        estoque_data = response.json()  # Processa o JSON recebido
    except requests.exceptions.RequestException as e:
        print(f"Erro ao obter dados de estoque do backend Spring: {e}")
        estoque_data = []

    # Filtra os ingredientes com quantidade maior que zero
    ingredientes_validos = [item for item in estoque_data if item['quantidade'] > 0]

    # Ordena por quantidade e pega os três primeiros
    sugestoes = sorted(ingredientes_validos, key=lambda x: x['quantidade'])[:3]

    return render_template('receitas.html', receitas=receitas_sugeridas, sugestoes=sugestoes)



@app.route('/relatorio')
def relatorio():
    receita_id = request.args.get('receitaId')  # Obtém o ID da receita da URL
    print(f"ID da receita recebido: {receita_id}")

    if receita_id:
        try:
            # Faz a requisição para o backend com o ID da receita
            response = requests.get(f'https://backendpizzaria-jw9a.onrender.com/receita/verificar-ingredientes?receitaId={receita_id}')
            
            # Verifica o status da resposta
            if response.status_code == 200:
                # Se a resposta for 200, captura a mensagem de sucesso e define a cor verde
                mensagem = response.text
                cor_mensagem = "green"
            elif response.status_code == 400:
                # Se a resposta for 400, captura a mensagem de erro e define a cor vermelha
                mensagem = response.text
                cor_mensagem = "red"
            else:
                mensagem = "Erro inesperado. Tente novamente mais tarde."
                cor_mensagem = "red"  # Define vermelho para outros erros
        except requests.exceptions.RequestException as e:
            print(f"Erro ao verificar ingredientes da receita: {e}")
            mensagem = "Erro ao verificar ingredientes. Tente novamente mais tarde."
            cor_mensagem = "red"  # Define vermelho para erros de requisição
    else:
        mensagem = "ID da receita não fornecido."
        cor_mensagem = "red"  # Define vermelho se não houver ID

    # Renderiza o template com a mensagem e a cor correspondente
    return render_template('relatorio.html', mensagem=mensagem, cor_mensagem=cor_mensagem, receita_id=receita_id)

if __name__ == '__main__':
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port)