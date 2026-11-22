# Cabe no Bolso - Planejador de Despesas Compartilhadas

Aplicação desktop em Java Swing desenvolvida como Atividade Final da disciplina de Programação Orientada a Objetos (UNIFOR EAD).

## Requisitos
- Java Development Kit (JDK) 17 ou superior.
- Visual Studio Code com *Extension Pack for Java* (opcional).

## Organização dos Pacotes
- `src/Main.java`: Ponto de entrada da aplicação.
- `src/model/`: Entidades de domínio (`Participante`, `Despesa`, `DespesaFixa`, `DespesaPorPessoa`, `Planejamento`) e algoritmo do Simulador de Alternativas (`SimuladorAlternativas`).
- `src/repository/`: Contrato e implementação de persistência via serialização (`IPersistenciaPlanejamento`, `PersistenciaArquivoLocal`).
- `src/controller/`: Regras de orquestração MVC (`PlanejamentoController`).
- `src/view/`: Interface gráfica Swing (`MainFrame`).
- `test/`: Testes automatizados (`TestesCabeNoBolso`).
- `dados/`: Arquivo de exemplo com o caso de referência (`caso_referencia.dat`).
- `docs/`: Diagrama de classes, simulador, testes e correções.

## Compilação e Execução via Terminal
Na raiz da pasta `CabeNoBolso`, execute:

```bash
# Compilar
javac -encoding UTF-8 -d bin -sourcepath src src/Main.java

# Executar
java -cp bin Main
```

## Reprodução dos Testes
1. Ao iniciar, clique em **Resetar p/ Caso de Referência** para carregar o **Caso de Referência (Passeio da Turma)**.
2. Verifique na aba **3. Resumo do Planejamento** o custo total de `R$ 210,00`, com Ana e Carla excedendo em `R$ 5,00` e Bruno dentro do limite (`R$ 60,00`).
3. Na aba **4. Simulador (Bônus Extra)**, clique em **Executar Otimização de Opcionais** para validar a remoção da despesa *Sobremesa* e ajuste do total para `R$ 180,00`.
4. Para salvar e recuperar, use os botões **Salvar Planejamento** e **Carregar Planejamento**. O arquivo `dados/caso_referencia.dat` já traz o caso de referência salvo.
5. Para rodar os testes automatizados, também na raiz do projeto:

```bash
javac -encoding UTF-8 -d bin -sourcepath src test/TestesCabeNoBolso.java
java -cp bin TestesCabeNoBolso
```

Os resultados dos testes e o roteiro de testes da tela estão em `docs/testes.md`.
