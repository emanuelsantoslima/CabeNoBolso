# Correções

Quando a primeira versão ficou pronta, revisamos o código comparando com a proposta e com a rubrica. Esta é a lista do que encontramos e do que fizemos. Cada correção tem teste na classe `TestesCabeNoBolso` ou um passo no roteiro da tela (`testes.md`).

## Principais

- Não dava para editar despesa, só incluir e excluir. Criamos a edição: clicar na despesa carrega os dados no formulário e o botão "Salvar Alteração" grava. Por dentro, a despesa antiga é trocada por uma nova, que passa pela validação do construtor.

- O dinheiro era guardado em `double`. Em algumas somas sobrava um resto (22,10 + 15,30 dava 37,400000000000006) e quem gastava exatamente o orçamento aparecia como "excede em R$ 0,00". Passamos tudo para `BigDecimal` com duas casas e as comparações para `compareTo`.

- Cadastrar um participante com código que já existia apagava o participante antigo sem avisar. O botão "Salvar Participante" passou a só cadastrar, recusando código repetido, e criamos o botão "Alterar Selecionado" para as alterações.

- Alterar um participante com orçamento negativo dava erro, mas o nome já tinha sido trocado. Agora nome e orçamento são validados antes de qualquer mudança.

- O programa sempre abria com o caso de referência e não tinha como começar do zero. Ele passou a abrir vazio e ganhou o botão "Novo Planejamento". O caso de referência continua no botão "Resetar p/ Caso de Referência".

- As células das tabelas podiam ser editadas com duplo clique, mas a mudança não ia para o planejamento e ainda podia causar erro ao remover. Bloqueamos a edição nas três tabelas.

- As mensagens de erro ao carregar arquivo eram as do Java, em inglês (para arquivo vazio aparecia "null"). Escrevemos uma mensagem para cada situação: arquivo não encontrado, vazio, incompleto, de outro tipo ou corrompido. Em todas o planejamento aberto é mantido.

- A conta de quem estava dentro do orçamento era feita na tela, e o simulador estava no controlador. Levamos os dois para o modelo (`Participante.calcularExcesso` e a classe `SimuladorAlternativas`).

- O custo total usava `instanceof` para saber o tipo da despesa. Mudamos para somar o `calcularRateio()` de cada uma.

- No simulador, a mensagem de "sem solução" não dizia o valor do excesso, a prioridade aceitava zero e negativo, e não havia limite de opcionais. Corrigimos os três pontos, e o resultado passou a mostrar os custos de antes e de depois.

- O resumo mostrava apenas o total de cada pessoa. Colocamos uma coluna por despesa, como na tabela da seção 4.

## Menores

- Texto como "NaN" era aceito no campo de valor. Com o `BigDecimal` esse tipo de texto não é mais convertido, e a tela mostra o aviso de número inválido.
- O arquivo ficava aberto quando a leitura falhava. Os dois fluxos passaram a ser fechados pelo try-with-resources.
- O modelo aceitava despesa com participante que não estava cadastrado.
- Um valor de despesa muito grande era aceito e depois quebrava a conta do rateio. Colocamos um valor máximo.
- Um arquivo salvo com algum byte estragado podia abrir com dados errados. O programa passou a conferir o planejamento lido antes de usar.
- A prioridade era exigida até para despesa essencial.
- O resultado do simulador continuava na tela depois que os dados mudavam.
- Os valores eram formatados conforme o idioma do computador. O `Main` passou a fixar pt-BR.
- Selecionar um participante na tabela não preenchia o formulário.
- Faltavam no repositório o diagrama de classes, os testes e as instruções do README.
