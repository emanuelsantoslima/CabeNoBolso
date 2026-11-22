# Testes

## Testes automatizados

Ficam em `test/TestesCabeNoBolso.java`, uma classe com `main`, sem JUnit. Para rodar, na raiz do projeto:

```
javac -encoding UTF-8 -d bin -sourcepath src test/TestesCabeNoBolso.java
java -cp bin TestesCabeNoBolso
```

Cada verificação imprime uma linha com PASSOU ou FALHOU, o valor esperado e o obtido. Na última execução (22/11/2026, OpenJDK 21) o resultado foi:

```
Resultado: 96 verificações aprovadas, 0 reprovadas.
```

No terminal os valores saem com ponto (75.00). Na tela aparecem com vírgula.

### Caso de referência e testes da seção 4

| Teste | Entrada | Esperado | Obtido | Passou |
|---|---|---|---|---|
| R1 Caso de referência | Ana 70, Bruno 60, Carla 70 e as quatro despesas da proposta | Ana 30/20/15/10 = 75,00; Bruno 30/20/0/10 = 60,00; Carla igual a Ana; total 210,00; Ana e Carla excedem 5,00 | Ana 75.00, Bruno 60.00, Carla 75.00; total 210.00; excessos 5.00, 0.00 e 5.00 | Sim |
| T1 Centavos | Fixa de 100,00 para os códigos 1, 2 e 3 | 33,34 / 33,33 / 33,33 | 33.34, 33.33, 33.33; soma 100.00 | Sim |
| T2 Entrada inválida | Despesa sem participantes, valor -10, descrição ou ID vazio | Recusa com mensagem, sem mudar os dados | Recusou todas, por exemplo "A despesa deve ter pelo menos um participante cadastrado." e "O valor da despesa deve ser maior que zero."; planejamento igual ao de antes | Sim |
| T3 Orçamento | Ana passa a ter 80,00 | Continua pagando 75,00, agora dentro do limite | Custo 75.00, excesso 0.00 | Sim |
| T4 Exclusão | Excluir a Atividade recreativa | 60,00 por pessoa, total 180,00 | 60.00 para os três; total 180.00 | Sim |
| T5 Salvar e reiniciar | Salvar, abrir em outro controlador, carregar duas vezes | Tudo de volta, sem duplicar | Dados idênticos; 3 participantes e 4 despesas; os participantes das despesas são os mesmos objetos da lista | Sim |
| T6 Arquivo com problema | Arquivo inexistente, vazio, cortado, de outro tipo, de texto e com um byte estragado | Mensagem e planejamento preservado | Uma mensagem para cada caso, como "O arquivo está vazio."; planejamento aberto não mudou | Sim |

### Simulador

| Teste | Entrada | Esperado | Obtido | Passou |
|---|---|---|---|---|
| S1 | Caso de referência | Retira a Sobremesa; Ana e Carla 65,00, Bruno 50,00; total 180,00 | Igual ao esperado, soma de prioridades 3 | Sim |
| S2 | Ana com 40,00 | Sem solução, com o excesso | Sem solução; Ana excede 10.00 | Sim |
| S3 | Passeio ao parque (caso nosso) | Mantém Sorvete e Fliperama, retira a Pizza; 50,00 cada | Igual ao esperado; total de 195.00 para 150.00 | Sim |

Os detalhes dos três casos estão em `simulador.md`.

### Outros testes

Depois desses, a classe testa as correções que fizemos (lista em `correcoes.md`): edição de despesa, soma com centavos, código repetido, limite de opcionais do simulador, entre outras. Na saída elas vêm depois da linha "Testes das correções".

## Testes na tela

O que depende da janela nós conferimos com este roteiro. Na versão final todos os passos deram o resultado esperado.

1. Abrir o programa. As tabelas começam vazias.
2. Clicar em "Resetar p/ Caso de Referência". O resumo mostra Ana 75,00 (EXCEDE em R$ 5,00), Bruno 60,00 (Dentro do limite), Carla 75,00 e custo total de R$ 210,00.
3. Dar duplo clique numa célula de qualquer tabela. Nada entra em edição.
4. Clicar na linha da Ana. O formulário é preenchido e o código fica travado. Trocar o orçamento para 80 e clicar em "Alterar Selecionado": ela passa a "Dentro do limite".
5. Clicar em "Limpar" e tentar cadastrar outro participante com o código 1 pelo botão "Salvar Participante". Aparece "Código de participante 1 já cadastrado.". Com um texto como "abc" no orçamento, aparece o aviso de valor numérico inválido.
6. Na aba de despesas, clicar na Sobremesa, mudar o valor para 12 e clicar em "Salvar Alteração". O resumo é recalculado.
7. Carregar o caso de referência de novo, selecionar a Atividade recreativa e clicar em "Excluir Despesa Selecionada". Fica 60,00 para cada um e total de R$ 180,00.
8. Tentar adicionar uma despesa sem marcar participantes. Aparece a mensagem e nada é cadastrado.
9. Salvar o planejamento, clicar em "Novo Planejamento" e carregar o arquivo salvo. Os dados voltam sem duplicar.
10. Carregar um arquivo de texto qualquer. Aparece a mensagem de arquivo corrompido e o planejamento aberto continua igual.
11. Carregar o caso de referência mais uma vez e, na aba do simulador, clicar em "Executar Otimização de Opcionais". A Sobremesa aparece como retirada, e o total vai de 210,00 para 180,00.
