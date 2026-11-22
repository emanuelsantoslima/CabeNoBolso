# Simulador de alternativas

Quando o passeio não cabe no orçamento de alguém, o simulador procura quais despesas opcionais retirar, mantendo a maior soma de prioridades possível. As essenciais nunca saem.

O código fica em `src/model/SimuladorAlternativas.java`, no método `simularAlternativasOtimas`. Ele estava no controlador e foi movido para o modelo.

## Como funciona

1. Separa as despesas em essenciais e opcionais. Se houver mais de 10 opcionais, recusa com uma mensagem.
2. Calcula o custo de cada pessoa só com as essenciais. Se alguém já passa do orçamento, não existe solução, e o resultado mostra de quanto é o excesso de cada um.
3. Testa todas as combinações de opcionais (força bruta). Com n opcionais são 2^n combinações. Cada número de 0 até 2^n - 1 é uma combinação: se o bit j está ligado, a opcional j fica.
4. Para cada combinação, soma as prioridades, calcula o custo de cada pessoa e confere se todos ficam dentro do orçamento. Entre as que cabem, fica a de maior soma.

Em caso de empate fica a primeira combinação encontrada. O simulador não altera o planejamento, ele apenas lê as despesas e devolve um `ResultadoSimulacao` com as despesas mantidas e retiradas e os custos de antes e de depois.

O limite de 10 opcionais vem da proposta e também do tamanho da busca: com 10 são 1.024 combinações, com 20 já passa de um milhão.

## Casos testados

Os três casos estão em `test/TestesCabeNoBolso.java` (S1, S2 e S3).

### Caso 1: passeio da turma (seção 7)

Transporte e Almoço essenciais, Atividade recreativa com prioridade 3 e Sobremesa com prioridade 1.

| Opcionais mantidas | Soma | Ana | Bruno | Carla | Cabe? |
|---|---|---|---|---|---|
| nenhuma | 0 | 50,00 | 50,00 | 50,00 | Sim |
| Atividade | 3 | 65,00 | 50,00 | 65,00 | Sim |
| Sobremesa | 1 | 60,00 | 60,00 | 60,00 | Sim |
| as duas | 4 | 75,00 | 60,00 | 75,00 | Não |

Resultado: mantém a Atividade e retira a Sobremesa, como a proposta espera. Antes, Ana e Carla pagavam 75,00 e Bruno 60,00 (total 210,00). Depois, Ana e Carla pagam 65,00 e Bruno 50,00 (total 180,00).

### Caso 2: Ana com R$ 40,00 (seção 7)

Só com Transporte e Almoço a Ana já gasta 50,00. O simulador responde que não há solução e que a Ana passa R$ 10,00 do orçamento.

### Caso 3: passeio ao parque (criado por nós)

Davi tem R$ 60,00, Eva R$ 55,00 e Fábio R$ 50,00. Todas as despesas são para os três.

| Despesa | Tipo | Valor | Essencial | Prioridade |
|---|---|---|---|---|
| Ônibus | Fixa | 36,00 | Sim | |
| Ingresso | Por pessoa | 20,00 | Sim | |
| Pizza | Fixa | 45,00 | Não | 3 |
| Sorvete | Por pessoa | 10,00 | Não | 2 |
| Fliperama | Por pessoa | 8,00 | Não | 2 |

Com tudo, cada um pagaria 65,00 (total 195,00) e os três estourariam. As essenciais custam 32,00 por pessoa. Com a Pizza vai para 47,00 e cabe, mas a soma de prioridades fica em 3. Com Sorvete e Fliperama vai para 50,00, que é exatamente o limite do Fábio, e a soma é 4. Sorvete ou Fliperama sozinhos também cabem, mas somam 2. Qualquer combinação da Pizza com outra opcional passa do orçamento dele.

O simulador ficou com Sorvete e Fliperama e retirou a Pizza: 50,00 para cada um, total 150,00. Escolhemos esse caso porque quem olhasse só para a maior prioridade ficaria com a Pizza, e também para testar um total igual ao orçamento.
