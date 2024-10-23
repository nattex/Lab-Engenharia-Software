from flask import Flask, render_template, request, redirect, url_for
import requests

app = Flask(__name__)

# Lista para armazenar os itens do estoque
estoque = []

@app.route('/')
def home():
    return render_template('home.html')  # Renderiza a nova página inicial

@app.route('/adicionar', methods=['GET', 'POST'])
def index():
    if request.method == 'POST':
        produto = request.form.get('produto')
        quantidade = request.form.get('quantidade')
        quantidade_kg = request.form.get('quantidade_kg')
        if produto and quantidade and quantidade_kg:
            estoque.append(f"{produto} - {quantidade} unidades, {quantidade_kg} kg")
            return redirect(url_for('index'))  # Redireciona para evitar reenvios de formulário
    return render_template('index.html')

@app.route('/estoque')
def conferir_estoque():
    return render_template('estoque.html', estoque=estoque)

@app.route('/conferir_estoque')
def conferir_pedidos():
    return render_template('conferir_estoque.html')

@app.route('/pesquisa_operacional/<pizza>')
def pesquisa_operacional(pizza):
    # Chamada para a API de pizzas
    try:
        response = requests.get('https://pizza-api.com/api/pizzas')
        response.raise_for_status()  # Verifica se a requisição foi bem-sucedida
        receitas = response.json()

        # Procura pela receita da pizza especificada
        receita_encontrada = next((item for item in receitas if item['name'].lower() == pizza.lower()), None)

        if receita_encontrada:
            ingredientes = receita_encontrada['ingredients']
            itens_faltando = [ingrediente for ingrediente in ingredientes if ingrediente not in estoque]
            return render_template('pesquisa_operacional.html', receita=receita_encontrada, itens_faltando=itens_faltando)  # Corrigido aqui
        else:
            return render_template('pesquisa_operacional.html', receita=None)

    except requests.exceptions.RequestException as e:
        return render_template('pesquisa_operacional.html', receita=None)



if __name__ == '__main__':
    app.run(debug=True)
