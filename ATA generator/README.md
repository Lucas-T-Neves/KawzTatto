# Automação de Geração de Atas

## 1. Visão geral

Esta automação tem como objetivo gerar automaticamente uma ata de reunião em formato `.docx` a partir de informações definidas no arquivo `config.py`.

O programa utiliza Python para:

1. receber as informações da reunião;
2. identificar os membros presentes e ausentes;
3. montar os parágrafos da ata;
4. utilizar um modelo do Microsoft Word (`.dotx`);
5. abrir e manipular o Microsoft Word automaticamente;
6. aplicar formatação ao documento;
7. salvar a ata final em formato `.docx`.

A execução principal é iniciada pelo `main.py`, que coordena os demais módulos.

---

# 2. Objetivo

O objetivo da automação é reduzir o trabalho manual necessário para criar atas de reuniões.

Em vez de criar um documento, copiar o modelo, preencher os participantes e formatar os campos manualmente, o usuário informa os dados da reunião no `config.py` e executa o programa.

A automação então gera o documento seguindo uma estrutura previamente definida.

---

# 3. Arquitetura

A aplicação possui uma arquitetura modular simples:

```text
                    ┌─────────────────┐
                    │    config.py    │
                    │                 │
                    │ Dados da reunião│
                    │ Configurações   │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │     main.py     │
                    │                 │
                    │ Orquestrador    │
                    └───────┬─────────┘
                            │
                ┌───────────┴───────────┐
                ▼                       ▼
       ┌─────────────────┐     ┌─────────────────┐
       │    ataform.py   │     │     pywin.py    │
       │                 │     │                 │
       │ Monta conteúdo  │     │ Controla Word   │
       │ da ata          │     │ e formatação    │
       └────────┬────────┘     └────────┬────────┘
                │                       │
                │                       ▼
                │              ┌─────────────────┐
                │              │   modelo.dotx   │
                │              │ Template Word   │
                │              └────────┬────────┘
                │                       │
                └───────────┬───────────┘
                            ▼
                    ┌─────────────────┐
                    │    arquivo.docx │
                    │    Ata final    │
                    └─────────────────┘
```

## Responsabilidade de cada componente

### `main.py`

É o ponto de entrada e responsável por coordenar a execução.

Ele:

- cria uma instância de `ATA_Formatter`;
- gera os textos da ata;
- cria uma instância de `WordInstance`;
- insere os textos no documento;
- aplica a formatação;
- salva o documento.

O fluxo principal é:

```text
config
   ↓
ATA_Formatter
   ↓
format_paragraphs()
   ↓
WordInstance
   ↓
create_and_populate_paragraphs()
   ↓
format_text()
   ↓
save()
```

O uso de:

```python
if __name__ == "__main__":
    main()
```

faz com que `main()` seja executado quando `main.py` for executado diretamente.

---

# 4. `config.py`

O `config.py` funciona como camada de configuração da automação.

É nele que são definidos os dados específicos da reunião, enquanto a lógica de geração permanece nos outros módulos.

Entre as informações configuradas estão:

- título;
- subtítulo;
- presentes;
- ausentes;
- assunto;
- próximos passos;
- objetivo;
- caminho para salvar o documento;
- caminho do template;
- textos que devem ser destacados em negrito.

O título utiliza a data atual automaticamente:

```python
titulo = f"ATA de reunião - {datetime.now().strftime('%d.%m.%Y')}"
```

Isso evita que o usuário precise alterar manualmente a data da ata.

O caminho utilizado para salvar o documento também é construído a partir da localização do próprio `config.py`:

```python
pasta_atual = Path(__file__).resolve().parent
```

e:

```python
pathSave = pasta_atual / f"{titulo}.docx"
```

Dessa forma, o documento é direcionado para a pasta da automação. 
---

# 5. `ataform.py`

Esse módulo é responsável por transformar as configurações em conteúdo estruturado para a ata.

A classe principal é:

```python
class ATA_Formatter:
```

Ela recebe os dados da reunião através do construtor.

## Identificação dos participantes

Os participantes são armazenados em uma lista fixa:

```python
membros = [
    ...
]
```

O programa compara os nomes informados no `config.py` com os nomes dessa lista utilizando os quatro primeiros caracteres.

Por exemplo:

```text
config.py
Lucas

        ↓

comparação dos 4 primeiros caracteres

        ↓

Lucas Tertuliano das Neves
```

Essa lógica permite informar somente o primeiro nome no `config.py`, enquanto o documento recebe o nome completo cadastrado.

## Geração dos parágrafos

Depois de identificar os participantes, o método `format_paragraphs()` retorna uma lista contendo:

1. título;
2. subtítulo;
3. presentes;
4. ausentes;
5. assunto;
6. próximos passos;
7. objetivo.

Essa lista é posteriormente utilizada pelo módulo responsável pelo Word.

---

# 6. `pywin.py`

Esse módulo é responsável pela comunicação entre Python e Microsoft Word.

A comunicação é feita utilizando:

```python
win32com.client
```

e:

```python
win32com.client.Dispatch("Word.Application")
```

O Word é aberto através da automação COM do Windows.

## Template

Ao iniciar, o programa cria um documento utilizando o template definido em `config.pathTemplate`:

```python
doc = openWord.Documents.Add(
    Template=str(config.pathTemplate)
)
```

Portanto, o arquivo `modelo.dotx` faz parte do funcionamento da automação.

## Inserção dos textos

O método:

```python
create_and_populate_paragraphs()
```

verifica a quantidade de parágrafos existente no documento e adiciona novos parágrafos quando necessário.

Depois, cada texto gerado pelo `ATA_Formatter` é colocado em um parágrafo correspondente.

## Formatação

O método:

```python
format_text()
```

define inicialmente:

- fonte Arial;
- tamanho 13.

O primeiro parágrafo recebe tamanho 22 e negrito.

O segundo recebe tamanho 16 e negrito.

Nos demais parágrafos, o programa procura os textos configurados em `textsBold` e aplica negrito quando encontra esses textos.

## Salvamento

Por fim, o documento é alinhado verticalmente ao centro e salvo utilizando:

```python
doc.SaveAs2(...)
```

com `FileFormat=16`, correspondente ao formato `.docx`.

Depois do salvamento, o documento é fechado e a instância do Word é encerrada.

---

# 7. Fluxo completo da automação

O funcionamento pode ser representado da seguinte maneira:

```text
1. Usuário altera config.py
             ↓
2. main.py é executado
             ↓
3. ATA_Formatter recebe os dados
             ↓
4. Participantes são identificados
             ↓
5. Os textos da ata são montados
             ↓
6. WordInstance é inicializado
             ↓
7. modelo.dotx é aberto como template
             ↓
8. Os textos são inseridos nos parágrafos
             ↓
9. Fonte e tamanhos são configurados
             ↓
10. Campos específicos recebem negrito
             ↓
11. Documento é salvo como .docx
             ↓
12. Word é fechado
```

---

# 8. Como utilizar

## 8.1. Configurar a reunião

Antes de executar a automação, deve-se alterar as informações no `config.py`.

Exemplo:

```python
sub = "Grupo 10"

pres = "Lucas Arthur Eric"

aus = "Clara"

ass = "Definição de beneficiário"

prox = "Definir as próximas atividades"

obj = "Definir possíveis soluções"
```

Os nomes utilizados em `pres` e `aus` devem corresponder aos membros cadastrados em `ataform.py`.

---

## 8.2. Verificar o template

O arquivo:

```text
modelo.dotx
```

deve estar disponível no caminho configurado em:

```python
pathTemplate
```

O programa utiliza esse arquivo como base para criar o documento.

---

## 8.3. Executar

A execução deve ser realizada através do:

```text
main.py
```

O programa então gera o documento automaticamente.

O arquivo final recebe o nome baseado no título configurado, que atualmente inclui a data da reunião.

---

# 9. Dependências

## Python

A automação é desenvolvida em Python.

Alguns módulos utilizados são da biblioteca padrão:

```python
datetime
pathlib
sys
```

Não é necessário instalar esses módulos separadamente.

## PyWin32

O projeto utiliza:

```python
win32com.client
```

para controlar o Microsoft Word.

Portanto, é necessário possuir o pacote que disponibiliza `win32com`.

## Microsoft Word

A automação depende do Microsoft Word instalado no computador.

Isso ocorre porque o código abre uma instância de:

```text
Word.Application
```

e manipula documentos através da interface COM do Windows.

## Template

Também é necessária a existência do:

```text
modelo.dotx
```

utilizado como modelo do documento.

---

# 10. Possíveis erros

## 10.1. Microsoft Word não instalado

### Sintoma

Erro ao executar:

```python
win32com.client.Dispatch("Word.Application")
```

### Causa

A automação não consegue criar uma instância do Microsoft Word.

### Solução

Instalar o Microsoft Word no computador que executará a automação.

---

## 10.2. PyWin32 não instalado

### Sintoma

Erro semelhante a:

```text
ModuleNotFoundError: No module named 'win32com'
```

### Causa

A biblioteca necessária para a comunicação com o Word não está instalada.

### Solução

Instalar o pacote correspondente ao `win32com`.

---

## 10.3. Template não encontrado

### Sintoma

Erro ao criar o documento a partir de:

```python
Template=str(config.pathTemplate)
```

### Causa

O arquivo `modelo.dotx` não está no caminho esperado.

### Solução

Verificar:

```python
pathTemplate
```

e garantir que o template esteja disponível nesse local.

---

## 10.4. Nome de participante não reconhecido

### Sintoma

Um participante informado em `config.py` não aparece corretamente na ata.

### Causa

A identificação depende dos quatro primeiros caracteres do nome informado e dos quatro primeiros caracteres do nome cadastrado.

### Exemplo

Se o cadastro possuir:

```text
Leonardo
```

e for informado:

```text
Leo
```

a comparação pode não encontrar o participante, pois a lógica utiliza quatro caracteres.

---

## 10.5. Participante duplicado

Como a identificação percorre os nomes informados e compara cada um com a lista de membros, uma configuração inadequada pode fazer com que um mesmo participante seja incluído mais de uma vez.

A documentação deve recomendar que cada participante seja informado uma única vez.

---

## 10.6. Caminho inválido

O `pathSave` utiliza a pasta onde `config.py` está localizado, o que reduz problemas de caminhos absolutos para o arquivo final. 
Porém, o template depende de `pathTemplate`. Se o caminho ou arquivo estiver incorreto, a geração pode falhar.

---

## 10.7. Arquivo já existente

Como o nome do arquivo é baseado na data:

```text
ATA de reunião - DD.MM.AAAA.docx
```

duas execuções para a mesma data podem resultar na tentativa de salvar utilizando o mesmo nome.

O comportamento de sobrescrita ou eventual erro depende do comportamento do Microsoft Word nesse cenário.

---

## 10.8. Word aberto ou processo interrompido

A automação controla uma instância do Word e, ao final, executa:

```python
doc.Close()
openWord.Quit()
```

Se ocorrer uma exceção antes dessas instruções, existe a possibilidade de a instância do Word permanecer aberta.

Uma melhoria futura seria utilizar tratamento de exceções e garantir o encerramento dos recursos mesmo quando ocorrer um erro.

---

# 11. Limitações atuais

A automação possui algumas características que devem ser conhecidas antes de sua manutenção.

### Lista de membros fixa

Os membros estão definidos diretamente no `ataform.py`.

Para adicionar ou remover uma pessoa, atualmente é necessário alterar o código.

### Dados da reunião ficam no código

As informações da reunião são inseridas diretamente no `config.py`.

Não existe atualmente uma interface ou formulário para preenchimento.

### Dependência do Windows

A utilização de `win32com.client` e do Microsoft Word torna a solução dependente do ambiente Windows.

### Dependência do Microsoft Word

O documento não é gerado por uma biblioteca Python independente de editor de texto. A automação utiliza o próprio Microsoft Word para criação e formatação.

---

# 12. Arquivo `modelCreate.py`

O projeto também possui um arquivo chamado `modelCreate.py`.

Esse arquivo está relacionado à criação/salvamento de um template `.dotx`, utilizando o Word.

Entretanto, o código atualmente apresentado possui referências que precisam ser verificadas antes de considerá-lo parte do fluxo principal da automação.

Por exemplo, existem referências a `word` e `path` que não aparecem definidas no trecho fornecido, além de:

```python
nome = ""
```

Portanto, esse arquivo deve ser considerado **script auxiliar para criação do modelo**, e não uma etapa necessária da execução principal, até que seu fluxo seja revisado. 

---

# 13. Segurança e versionamento

O arquivo `.gitignore` faz parte da pasta da automação.

Seu objetivo deve ser evitar que arquivos que não precisam fazer parte do versionamento sejam enviados para o Git.

Como o conteúdo do `.gitignore` não está disponível no material analisado, não é possível afirmar quais arquivos ele atualmente ignora.

É recomendável verificar se arquivos gerados automaticamente, como os `.docx`, não estão sendo versionados desnecessariamente.

Também devem ser evitados no repositório dados pessoais ou informações sensíveis inseridas nas atas, caso a automação passe a ser utilizada com informações reais.

---

# 14. Manutenção

Para modificar a automação, deve-se identificar primeiro qual camada é responsável pela alteração.

| Necessidade | Arquivo |
|---|---|
| Alterar dados da reunião | `config.py` |
| Alterar participantes cadastrados | `ataform.py` |
| Alterar estrutura do texto da ata | `ataform.py` |
| Alterar fonte/tamanho/negrito | `pywin.py` |
| Alterar comportamento do Word | `pywin.py` |
| Alterar fluxo principal | `main.py` |
| Alterar aparência estrutural do documento | `modelo.dotx` |
| Criar/alterar o template | `modelCreate.py` |

---

# 15. Resumo da arquitetura

A automação segue uma separação simples de responsabilidades:

```text
CONFIGURAÇÃO
config.py
     │
     ▼
PROCESSAMENTO
ataform.py
     │
     ▼
ORQUESTRAÇÃO
main.py
     │
     ▼
AUTOMAÇÃO DO WORD
pywin.py
     │
     ├──────────────► modelo.dotx
     │
     ▼
SAÍDA
ATA de reunião - DD.MM.AAAA.docx
```

Essa divisão permite que o conteúdo da reunião, a lógica de montagem da ata e a manipulação do Word permaneçam relativamente separados.

---

# 16. Melhorias futuras recomendadas

Algumas melhorias podem tornar a automação mais robusta:

1. adicionar tratamento de exceções (`try/except/finally`);
2. garantir que o Word seja encerrado mesmo quando ocorrer um erro;
3. separar a lista de membros do código;
4. validar os participantes antes de gerar a ata;
5. impedir participantes duplicados;
6. validar se o template existe antes de abrir o Word;
7. validar os dados obrigatórios da reunião;
8. evitar sobrescrever automaticamente uma ata existente;
9. criar um arquivo de dependências, como `requirements.txt`;
10. adicionar mensagens de erro mais claras para o usuário;
11. criar testes para a parte de formatação dos dados;
12. futuramente substituir a configuração manual por uma interface de preenchimento.

---

# 17. Dependências resumidas

```text
Python
 │
 ├── datetime       → data automática
 ├── pathlib        → manipulação de caminhos
 ├── sys            → recursos do sistema
 │
 └── pywin32
       │
       └── win32com.client
               │
               ▼
        Microsoft Word
               │
               ▼
          modelo.dotx
               │
               ▼
             .docx
```

## Requisitos mínimos conhecidos

- Python instalado;
- Windows;
- Microsoft Word instalado;
- biblioteca `pywin32`;
- arquivo `modelo.dotx`;
- arquivos Python da automação na estrutura esperada.

---

# 18. Resultado esperado

Após a configuração e execução, a automação deve produzir uma ata `.docx` contendo:

- título com a data;
- grupo/subtítulo;
- participantes presentes;
- participantes ausentes;
- assunto;
- próximos passos;
- objetivo;
- formatação aplicada automaticamente.

O documento é salvo na pasta da automação utilizando o nome definido pelo título configurado.