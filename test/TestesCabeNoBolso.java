import controller.PlanejamentoController;
import model.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TestesCabeNoBolso {
    private static int aprovados = 0;
    private static int reprovados = 0;
    private static Path pasta;

    private interface Acao {
        void executar() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        pasta = Files.createTempDirectory("cabe-no-bolso-testes");
        testarCasoDeReferencia();
        testarCentavos();
        testarEntradaInvalida();
        testarAlteracaoDeOrcamento();
        testarExclusaoDeDespesa();
        testarSalvarEReiniciar();
        testarProblemaNoArquivo();
        testarSimuladorCasoReferencia();
        testarSimuladorSemSolucao();
        testarSimuladorCasoExtra();
        testarCorrecoes();

        for (File f : pasta.toFile().listFiles()) f.delete();
        pasta.toFile().delete();
        System.out.println();
        System.out.println("Resultado: " + aprovados + " verificações aprovadas, " + reprovados + " reprovadas.");
        if (reprovados > 0) System.exit(1);
    }

    private static void verificar(String descricao, Object obtido, Object esperado) {
        boolean passou = String.valueOf(obtido).equals(String.valueOf(esperado));
        if (passou) aprovados++; else reprovados++;
        System.out.println((passou ? "PASSOU " : "FALHOU ") + descricao + " | esperado: " + esperado + " | obtido: " + obtido);
    }

    private static String erroDe(Acao acao) {
        try {
            acao.executar();
            return "nenhum erro";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static BigDecimal reais(String valor) {
        return new BigDecimal(valor);
    }

    private static PlanejamentoController casoDeReferencia() throws ValidacaoException {
        PlanejamentoController c = new PlanejamentoController();
        c.cadastrarParticipante(1, "Ana", reais("70.00"));
        c.cadastrarParticipante(2, "Bruno", reais("60.00"));
        c.cadastrarParticipante(3, "Carla", reais("70.00"));
        List<Participante> todos = c.getPlanejamento().getParticipantes();
        List<Participante> anaECarla = List.of(pessoa(c, 1), pessoa(c, 3));
        c.cadastrarDespesaFixa("D1", "Transporte", reais("90.00"), todos, true, 0);
        c.cadastrarDespesaPorPessoa("D2", "Almoço", reais("20.00"), todos, true, 0);
        c.cadastrarDespesaFixa("D3", "Atividade recreativa", reais("30.00"), anaECarla, false, 3);
        c.cadastrarDespesaPorPessoa("D4", "Sobremesa", reais("10.00"), todos, false, 1);
        return c;
    }

    private static Participante pessoa(PlanejamentoController c, int codigo) {
        return c.getPlanejamento().getParticipantePorCodigo(codigo);
    }

    private static BigDecimal total(PlanejamentoController c, int codigo) {
        return c.getPlanejamento().calcularTotaisIndividuais().get(pessoa(c, codigo));
    }

    private static BigDecimal excesso(PlanejamentoController c, int codigo) {
        return pessoa(c, codigo).calcularExcesso(total(c, codigo));
    }

    private static String valores(Map<Participante, BigDecimal> mapa) {
        StringJoiner texto = new StringJoiner(", ");
        for (Map.Entry<Participante, BigDecimal> entry : new TreeMap<>(mapa).entrySet()) {
            texto.add(entry.getKey().getNome() + " " + entry.getValue());
        }
        return texto.toString();
    }

    private static String nomes(List<Despesa> despesas) {
        StringJoiner texto = new StringJoiner(", ");
        for (Despesa d : despesas) texto.add(d.getDescricao());
        return texto.toString();
    }

    private static String descreverDespesas(Planejamento p) {
        StringJoiner texto = new StringJoiner("; ");
        for (Despesa d : p.getDespesas()) {
            StringJoiner codigos = new StringJoiner(",", "[", "]");
            for (Participante x : d.getParticipantesInvolucrados()) codigos.add(String.valueOf(x.getCodigo()));
            texto.add(d.getId() + " " + d.getDescricao() + " " + d.getTipo() + " " + d.getValorInformado() + " " + codigos
                    + (d.isEssencial() ? " essencial" : " prioridade " + d.getPrioridade()));
        }
        return texto.toString();
    }

    // fotografia do planejamento inteiro, usada para conferir que uma operação recusada não mudou nada
    private static String retrato(PlanejamentoController c) {
        Planejamento p = c.getPlanejamento();
        StringJoiner pessoas = new StringJoiner("; ");
        for (Participante x : p.getParticipantes()) pessoas.add(x.getCodigo() + " " + x.getNome() + " " + x.getOrcamentoDisponivel());
        return pessoas + " | " + descreverDespesas(p) + " | " + valores(p.calcularTotaisIndividuais()) + " | " + p.calcularCustoTotalGeral();
    }

    private static void testarCasoDeReferencia() throws Exception {
        System.out.println("== R1 Caso de referência (seção 4)");
        PlanejamentoController c = casoDeReferencia();
        Planejamento p = c.getPlanejamento();
        String[] esperado = {"30.00 20.00 15.00 10.00", "30.00 20.00 0.00 10.00", "30.00 20.00 15.00 10.00"};
        for (Participante x : p.getParticipantes()) {
            StringJoiner partes = new StringJoiner(" ");
            for (Despesa d : p.getDespesas()) {
                partes.add(String.valueOf(d.calcularRateio().getOrDefault(x, reais("0.00"))));
            }
            verificar("R1 valor de " + x.getNome() + " em cada despesa", partes, esperado[x.getCodigo() - 1]);
        }
        verificar("R1 totais individuais", valores(p.calcularTotaisIndividuais()), "Ana 75.00, Bruno 60.00, Carla 75.00");
        verificar("R1 custo total", p.calcularCustoTotalGeral(), "210.00");
        verificar("R1 excesso de Ana", excesso(c, 1), "5.00");
        verificar("R1 excesso de Bruno (total igual ao orçamento fica dentro)", excesso(c, 2), "0.00");
        verificar("R1 excesso de Carla", excesso(c, 3), "5.00");
    }

    private static void testarCentavos() throws Exception {
        System.out.println("== T1 Centavos no rateio");
        PlanejamentoController c = new PlanejamentoController();
        c.cadastrarParticipante(1, "Ana", reais("100"));
        c.cadastrarParticipante(2, "Bruno", reais("100"));
        c.cadastrarParticipante(3, "Carla", reais("100"));
        c.cadastrarDespesaFixa("C1", "Teste de centavos", reais("100.00"), c.getPlanejamento().getParticipantes(), true, 0);
        verificar("T1 fixa de 100,00 para os códigos 1, 2 e 3", valores(c.getPlanejamento().calcularTotaisIndividuais()), "Ana 33.34, Bruno 33.33, Carla 33.33");
        verificar("T1 soma das partes", c.getPlanejamento().calcularCustoTotalGeral(), "100.00");

        c.removerDespesa("C1");
        c.cadastrarDespesaFixa("C2", "Teste de centavos", reais("100.00"), List.of(pessoa(c, 3), pessoa(c, 1), pessoa(c, 2)), true, 0);
        verificar("T1 mesma despesa com a lista na ordem 3, 1, 2", valores(c.getPlanejamento().calcularTotaisIndividuais()), "Ana 33.34, Bruno 33.33, Carla 33.33");
    }

    private static void testarEntradaInvalida() throws Exception {
        System.out.println("== T2 Entrada ou vínculo inválido");
        PlanejamentoController c = casoDeReferencia();
        String antes = retrato(c);
        List<Participante> ana = List.of(pessoa(c, 1));
        verificar("T2 despesa sem participantes", erroDe(() -> c.cadastrarDespesaFixa("D9", "Teste", reais("50"), List.of(), true, 0)),
                "A despesa deve ter pelo menos um participante cadastrado.");
        verificar("T2 valor negativo", erroDe(() -> c.cadastrarDespesaFixa("D9", "Teste", reais("-10"), ana, true, 0)),
                "O valor da despesa deve ser maior que zero.");
        verificar("T2 valor zero", erroDe(() -> c.cadastrarDespesaPorPessoa("D9", "Teste", reais("0"), ana, true, 0)),
                "O valor da despesa deve ser maior que zero.");
        verificar("T2 descrição vazia", erroDe(() -> c.cadastrarDespesaFixa("D9", "  ", reais("50"), ana, true, 0)),
                "Descrição da despesa é obrigatória.");
        verificar("T2 ID vazio", erroDe(() -> c.cadastrarDespesaFixa("", "Teste", reais("50"), ana, true, 0)),
                "ID da despesa inválido.");
        verificar("T2 nome de participante vazio", erroDe(() -> c.cadastrarParticipante(4, "", reais("10"))),
                "O nome do participante não pode ser vazio.");
        verificar("T2 orçamento negativo", erroDe(() -> c.cadastrarParticipante(4, "Davi", reais("-1"))),
                "O orçamento disponível deve ser maior ou igual a zero.");
        verificar("T2 dados continuam iguais depois das recusas", retrato(c), antes);
    }

    private static void testarAlteracaoDeOrcamento() throws Exception {
        System.out.println("== T3 Alteração de orçamento");
        PlanejamentoController c = casoDeReferencia();
        c.atualizarParticipante(1, "Ana", reais("80.00"));
        verificar("T3 orçamento de Ana", pessoa(c, 1).getOrcamentoDisponivel(), "80.00");
        verificar("T3 custo de Ana continua", total(c, 1), "75.00");
        verificar("T3 Ana agora dentro do limite (excesso zero)", excesso(c, 1), "0.00");
    }

    private static void testarExclusaoDeDespesa() throws Exception {
        System.out.println("== T4 Exclusão de despesa");
        PlanejamentoController c = casoDeReferencia();
        c.removerDespesa("D3");
        verificar("T4 totais sem a atividade recreativa", valores(c.getPlanejamento().calcularTotaisIndividuais()), "Ana 60.00, Bruno 60.00, Carla 60.00");
        verificar("T4 custo total", c.getPlanejamento().calcularCustoTotalGeral(), "180.00");
        verificar("T4 todos dentro do limite", excesso(c, 1) + " " + excesso(c, 2) + " " + excesso(c, 3), "0.00 0.00 0.00");
    }

    private static void testarSalvarEReiniciar() throws Exception {
        System.out.println("== T5 Salvar e reiniciar");
        PlanejamentoController original = casoDeReferencia();
        String arquivo = pasta.resolve("passeio.dat").toString();
        original.salvarEmArquivo(arquivo);

        PlanejamentoController reaberto = new PlanejamentoController();
        reaberto.carregarDeArquivo(arquivo);
        verificar("T5 dados e resultados recuperados em um controlador novo", retrato(reaberto), retrato(original));
        reaberto.carregarDeArquivo(arquivo);
        Planejamento p = reaberto.getPlanejamento();
        verificar("T5 carregar duas vezes não duplica registros", p.getParticipantes().size() + " participantes, " + p.getDespesas().size() + " despesas", "3 participantes, 4 despesas");

        boolean mesmoObjeto = true;
        for (Despesa d : p.getDespesas()) {
            for (Participante x : d.getParticipantesInvolucrados()) {
                mesmoObjeto &= x == p.getParticipantePorCodigo(x.getCodigo());
            }
        }
        verificar("T5 vínculo preservado: participantes das despesas são os mesmos objetos da lista", mesmoObjeto, true);
        reaberto.atualizarParticipante(1, "Ana Maria", reais("70"));
        verificar("T5 renomear depois de carregar aparece na despesa", p.getDespesas().get(0).getParticipantesInvolucrados().get(0).getNome(), "Ana Maria");

        PlanejamentoController exemplo = new PlanejamentoController();
        verificar("T5 arquivo de exemplo dados/caso_referencia.dat carrega (rodar a partir da raiz do projeto)",
                erroDe(() -> exemplo.carregarDeArquivo("dados/caso_referencia.dat")), "nenhum erro");
        verificar("T5 arquivo de exemplo tem exatamente o caso de referência", retrato(exemplo), retrato(original));
    }

    private static void testarProblemaNoArquivo() throws Exception {
        System.out.println("== T6 Problema no arquivo");
        PlanejamentoController c = casoDeReferencia();
        Planejamento emUso = c.getPlanejamento();
        String antes = retrato(c);

        byte[] completo = Files.readAllBytes(pasta.resolve("passeio.dat"));
        String inexistente = pasta.resolve("nao_existe.dat").toString();
        Path vazio = Files.createFile(pasta.resolve("vazio.dat"));
        Path texto = Files.writeString(pasta.resolve("texto.dat"), "isto não é um planejamento");
        Path truncado = Files.write(pasta.resolve("truncado.dat"), Arrays.copyOf(completo, completo.length / 2));
        Path outroTipo = pasta.resolve("outro.dat");
        try (ObjectOutputStream saida = new ObjectOutputStream(Files.newOutputStream(outroTipo))) {
            saida.writeObject("um texto qualquer salvo com serialização");
        }

        verificar("T6 arquivo inexistente", erroDe(() -> c.carregarDeArquivo(inexistente)), "Arquivo não encontrado: " + inexistente);
        verificar("T6 arquivo vazio", erroDe(() -> c.carregarDeArquivo(vazio.toString())), "O arquivo está vazio.");
        verificar("T6 arquivo truncado", erroDe(() -> c.carregarDeArquivo(truncado.toString())),
                "O arquivo está incompleto. Ele pode ter sido cortado ao ser copiado ou gravado.");
        verificar("T6 arquivo com outro tipo de objeto", erroDe(() -> c.carregarDeArquivo(outroTipo.toString())),
                "O arquivo não contém um planejamento do Cabe no Bolso.");
        verificar("T6 arquivo corrompido (texto)", erroDe(() -> c.carregarDeArquivo(texto.toString())),
                "O arquivo está corrompido ou não foi gravado por esta versão do programa.");
        verificar("T6 planejamento em uso preservado após as falhas", c.getPlanejamento() == emUso && retrato(c).equals(antes), true);
        verificar("T6 o arquivo inválido pode ser apagado logo após a falha (fluxo fechado)", texto.toFile().delete(), true);

        // estraga um byte de cada vez: o arquivo tem que ser recusado com mensagem ou abrir com dados que dá para usar
        Path estragado = pasta.resolve("estragado.dat");
        int semTratamento = 0;
        for (int i = 0; i < completo.length; i++) {
            byte[] copia = completo.clone();
            copia[i] ^= 0xFF;
            Files.write(estragado, copia);
            PlanejamentoController outro = new PlanejamentoController();
            try {
                outro.carregarDeArquivo(estragado.toString());
                retrato(outro);
            } catch (IOException e) {
                if (e.getMessage() == null) semTratamento++;
            } catch (RuntimeException e) {
                semTratamento++;
            }
        }
        verificar("T6 arquivo com um byte estragado (testado em cada posição) nunca fica sem tratamento", semTratamento, 0);

        c.novoPlanejamento("Vazio");
        verificar("T6 é possível iniciar um planejamento vazio", c.getPlanejamento().getParticipantes().size() + " " + c.getPlanejamento().getDespesas().size(), "0 0");
    }

    private static void testarSimuladorCasoReferencia() throws Exception {
        System.out.println("== S1 Simulador, caso de referência (seção 7)");
        PlanejamentoController c = casoDeReferencia();
        String antes = retrato(c);
        SimuladorAlternativas.ResultadoSimulacao r = c.simularAlternativasOtimas();
        verificar("S1 há solução", r.viavel, true);
        verificar("S1 despesas mantidas", nomes(r.despesasMantidas), "Transporte, Almoço, Atividade recreativa");
        verificar("S1 despesas retiradas", nomes(r.despesasRetiradas), "Sobremesa");
        verificar("S1 soma das prioridades", r.somaPrioridades, 3);
        verificar("S1 custos atuais", valores(r.custosAtuais), "Ana 75.00, Bruno 60.00, Carla 75.00");
        verificar("S1 custos novos", valores(r.custosPorPessoa), "Ana 65.00, Bruno 50.00, Carla 65.00");
        verificar("S1 total atual e total novo", r.custoTotalAtual + " " + r.custoTotal, "210.00 180.00");
        verificar("S1 planejamento original não muda", retrato(c), antes);
    }

    private static void testarSimuladorSemSolucao() throws Exception {
        System.out.println("== S2 Simulador sem solução (Ana com R$ 40,00)");
        PlanejamentoController c = casoDeReferencia();
        c.atualizarParticipante(1, "Ana", reais("40"));
        String antes = retrato(c);
        SimuladorAlternativas.ResultadoSimulacao r = c.simularAlternativasOtimas();
        verificar("S2 não há solução", r.viavel, false);
        verificar("S2 mensagem", r.mensagemErro, "Sem solução viável: As despesas essenciais já ultrapassam o orçamento.");
        verificar("S2 excesso indicado", valores(r.excessos), "Ana 10.00");
        verificar("S2 custo só das essenciais", valores(r.custosPorPessoa), "Ana 50.00, Bruno 50.00, Carla 50.00");
        verificar("S2 essenciais continuam mantidas", nomes(r.despesasMantidas), "Transporte, Almoço");
        verificar("S2 retiradas são só as opcionais", nomes(r.despesasRetiradas), "Atividade recreativa, Sobremesa");
        verificar("S2 planejamento original não muda", retrato(c), antes);
    }

    private static PlanejamentoController casoExtraDoParque() throws Exception {
        PlanejamentoController c = new PlanejamentoController();
        c.cadastrarParticipante(1, "Davi", reais("60.00"));
        c.cadastrarParticipante(2, "Eva", reais("55.00"));
        c.cadastrarParticipante(3, "Fábio", reais("50.00"));
        List<Participante> todos = c.getPlanejamento().getParticipantes();
        c.cadastrarDespesaFixa("P1", "Ônibus", reais("36.00"), todos, true, 0);
        c.cadastrarDespesaPorPessoa("P2", "Ingresso", reais("20.00"), todos, true, 0);
        c.cadastrarDespesaFixa("P3", "Pizza", reais("45.00"), todos, false, 3);
        c.cadastrarDespesaPorPessoa("P4", "Sorvete", reais("10.00"), todos, false, 2);
        c.cadastrarDespesaPorPessoa("P5", "Fliperama", reais("8.00"), todos, false, 2);
        return c;
    }

    private static void testarSimuladorCasoExtra() throws Exception {
        System.out.println("== S3 Simulador, caso extra do grupo (passeio ao parque)");
        PlanejamentoController c = casoExtraDoParque();
        String antes = retrato(c);
        SimuladorAlternativas.ResultadoSimulacao r = c.simularAlternativasOtimas();
        verificar("S3 há solução", r.viavel, true);
        verificar("S3 despesas mantidas", nomes(r.despesasMantidas), "Ônibus, Ingresso, Sorvete, Fliperama");
        verificar("S3 despesas retiradas", nomes(r.despesasRetiradas), "Pizza");
        verificar("S3 soma das prioridades (2 + 2 vence 3)", r.somaPrioridades, 4);
        verificar("S3 custos atuais", valores(r.custosAtuais), "Davi 65.00, Eva 65.00, Fábio 65.00");
        verificar("S3 custos novos", valores(r.custosPorPessoa), "Davi 50.00, Eva 50.00, Fábio 50.00");
        verificar("S3 total atual e total novo", r.custoTotalAtual + " " + r.custoTotal, "195.00 150.00");
        verificar("S3 Fábio fica exatamente no limite", pessoa(c, 3).calcularExcesso(r.custosPorPessoa.get(pessoa(c, 3))), "0.00");
        verificar("S3 planejamento original não muda", retrato(c), antes);
    }

    private static void testarCorrecoes() throws Exception {
        System.out.println("== Testes das correções");

        PlanejamentoController c = casoDeReferencia();
        List<Participante> anaEBruno = List.of(pessoa(c, 1), pessoa(c, 2));
        c.editarDespesa("D4", "Sobremesa", reais("12.00"), "Fixa", anaEBruno, true, 0);
        verificar("despesa editada (tipo, valor, participantes e essencial)", descreverDespesas(c.getPlanejamento()).split("; ")[3],
                "D4 Sobremesa Fixa 12.00 [1,2] essencial");
        verificar("totais recalculados após a edição", valores(c.getPlanejamento().calcularTotaisIndividuais()), "Ana 71.00, Bruno 56.00, Carla 65.00");
        verificar("custo total após a edição", c.getPlanejamento().calcularCustoTotalGeral(), "192.00");
        String depoisDaEdicao = retrato(c);
        verificar("edição com valor inválido é recusada", erroDe(() -> c.editarDespesa("D2", "Almoço", reais("-5"), "Por Pessoa", anaEBruno, true, 0)),
                "O valor da despesa deve ser maior que zero.");
        verificar("edição com prioridade 0 em opcional é recusada", erroDe(() -> c.editarDespesa("D3", "Atividade", reais("30"), "Fixa", anaEBruno, false, 0)),
                "A prioridade de uma despesa opcional deve ser um número inteiro maior ou igual a 1.");
        verificar("edição de ID inexistente é recusada", erroDe(() -> c.editarDespesa("D9", "Nova", reais("5"), "Fixa", anaEBruno, true, 0)),
                "Despesa não encontrada para alteração.");
        verificar("edições recusadas não mudam nada", retrato(c), depoisDaEdicao);

        PlanejamentoController precisao = new PlanejamentoController();
        precisao.cadastrarParticipante(4, "Davi", reais("37.40"));
        precisao.cadastrarDespesaPorPessoa("X1", "Ingresso", reais("22.10"), precisao.getPlanejamento().getParticipantes(), true, 0);
        precisao.cadastrarDespesaPorPessoa("X2", "Lanche", reais("15.30"), precisao.getPlanejamento().getParticipantes(), true, 0);
        verificar("22,10 + 15,30 dá exatamente 37,40", total(precisao, 4), "37.40");
        verificar("total igual ao orçamento fica dentro", excesso(precisao, 4), "0.00");
        verificar("orçamento com três casas decimais é recusado", erroDe(() -> precisao.cadastrarParticipante(5, "Eva", reais("10.005"))),
                "O orçamento deve ter no máximo duas casas decimais.");
        verificar("despesa com três casas decimais é recusada", erroDe(() -> precisao.cadastrarDespesaFixa("Z", "Z", reais("10.005"), List.of(pessoa(precisao, 4)), true, 0)),
                "O valor da despesa deve ter no máximo duas casas decimais.");
        precisao.cadastrarParticipante(5, "Eva", reais("70.5"));
        verificar("valor com uma casa é completado para duas", pessoa(precisao, 5).getOrcamentoDisponivel(), "70.50");

        Planejamento soModelo = casoDeReferencia().getPlanejamento();
        verificar("simulador roda direto no modelo, sem controlador", valores(SimuladorAlternativas.simularAlternativasOtimas(soModelo).custosPorPessoa), "Ana 65.00, Bruno 50.00, Carla 65.00");
        verificar("excesso calculado pelo modelo", soModelo.getParticipantePorCodigo(1).calcularExcesso(reais("75.00")), "5.00");

        PlanejamentoController ref = casoDeReferencia();
        String antesRef = retrato(ref);
        verificar("código duplicado é recusado", erroDe(() -> ref.cadastrarParticipante(1, "Zeca", reais("10"))),
                "Código de participante 1 já cadastrado.");
        verificar("alteração com orçamento negativo é recusada", erroDe(() -> ref.atualizarParticipante(2, "Bruno Silva", reais("-5"))),
                "O orçamento disponível deve ser maior ou igual a zero.");
        verificar("alteração com nome vazio é recusada", erroDe(() -> ref.atualizarParticipante(2, "", reais("65"))),
                "O nome do participante não pode ser vazio.");
        verificar("nada mudou (Bruno continua Bruno com 60,00)", retrato(ref), antesRef);

        PlanejamentoController vazio = new PlanejamentoController();
        verificar("o controlador começa com planejamento vazio", vazio.getPlanejamento().getParticipantes().size() + " " + vazio.getPlanejamento().getDespesas().size()
                + " " + vazio.getPlanejamento().calcularCustoTotalGeral(), "0 0 0.00");

        PlanejamentoController terceiro = casoDeReferencia();
        Participante anaRef = pessoa(terceiro, 1);
        Despesa brinde = new Despesa("D5", "Brinde", reais("5.00"), List.of(anaRef), false, 1) {
            @Override
            public String getTipo() { return "Brinde"; }

            @Override
            public Map<Participante, BigDecimal> calcularRateio() { return Map.of(anaRef, getValorInformado()); }
        };
        terceiro.getPlanejamento().adicionarDespesa(brinde);
        verificar("custo total soma o rateio de qualquer subclasse", terceiro.getPlanejamento().calcularCustoTotalGeral(), "215.00");
        verificar("total de Ana inclui o brinde", total(terceiro, 1), "80.00");

        PlanejamentoController prioridade = casoDeReferencia();
        List<Participante> todos = prioridade.getPlanejamento().getParticipantes();
        verificar("prioridade 0 em opcional é recusada", erroDe(() -> prioridade.cadastrarDespesaFixa("O1", "Extra", reais("1"), todos, false, 0)),
                "A prioridade de uma despesa opcional deve ser um número inteiro maior ou igual a 1.");
        verificar("prioridade negativa é recusada", erroDe(() -> prioridade.cadastrarDespesaFixa("O1", "Extra", reais("1"), todos, false, -2)),
                "A prioridade de uma despesa opcional deve ser um número inteiro maior ou igual a 1.");
        for (int i = 1; i <= 8; i++) {
            prioridade.cadastrarDespesaFixa("O" + i, "Opcional " + i, reais("1"), todos, false, 1);
        }
        verificar("com 10 opcionais o simulador roda", prioridade.simularAlternativasOtimas().viavel, true);
        prioridade.cadastrarDespesaFixa("O9", "Opcional 9", reais("1"), todos, false, 1);
        verificar("com 11 opcionais o simulador recusa", erroDe(prioridade::simularAlternativasOtimas),
                "O simulador aceita no máximo 10 despesas opcionais, e este planejamento tem 11.");

        PlanejamentoController vinculo = casoDeReferencia();
        String antesVinculo = retrato(vinculo);
        verificar("participante fora do planejamento é recusado",
                erroDe(() -> vinculo.cadastrarDespesaFixa("F1", "Fantasma", reais("10"), List.of(new Participante(99, "Fantasma", reais("10"))), true, 0)),
                "O participante Fantasma não está cadastrado neste planejamento.");
        verificar("cópia de um participante cadastrado é recusada",
                erroDe(() -> vinculo.cadastrarDespesaFixa("F2", "Cópia", reais("10"), List.of(new Participante(1, "Ana", reais("70"))), true, 0)),
                "O participante Ana não está cadastrado neste planejamento.");
        verificar("valor acima do máximo é recusado", erroDe(() -> vinculo.cadastrarDespesaFixa("F3", "Iate", reais("1000000000"), List.of(pessoa(vinculo, 1)), true, 0)),
                "O valor da despesa deve ser de no máximo R$ 999.999.999,99.");
        verificar("ID em minúsculas conta como repetido", erroDe(() -> vinculo.cadastrarDespesaFixa("d1", "Outra", reais("5"), List.of(pessoa(vinculo, 1)), true, 0)),
                "Já existe uma despesa com o ID d1");
        verificar("ID com espaços conta como repetido", erroDe(() -> vinculo.cadastrarDespesaFixa(" D1 ", "Outra", reais("5"), List.of(pessoa(vinculo, 1)), true, 0)),
                "Já existe uma despesa com o ID D1");
        verificar("participante com despesa não pode ser removido", erroDe(() -> vinculo.removerParticipante(2)),
                "Não é possível remover o participante Bruno pois ele possui a despesa 'Transporte' vinculada.");
        verificar("nada mudou depois dessas recusas", retrato(vinculo), antesVinculo);
    }
}
