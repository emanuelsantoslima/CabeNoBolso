package controller;

import model.*;
import repository.*;

import java.io.IOException;
import java.util.*;

public class PlanejamentoController {

    private Planejamento planejamento;
    private final IPersistenciaPlanejamento repositorio;

    public PlanejamentoController() {
        this.planejamento = new Planejamento("Passeio em Grupo");
        this.repositorio = new PersistenciaArquivoLocal();
    }

    public Planejamento getPlanejamento() { return planejamento; }

    public void novoPlanejamento(String titulo) {
        this.planejamento = new Planejamento(titulo);
    }

    public void cadastrarParticipante(int codigo, String nome, double orcamento) {
        Participante p = new Participante(codigo, nome, orcamento);
        planejamento.adicionarParticipante(p);
    }

    public void atualizarParticipante(int codigo, String nome, double orcamento) {
        planejamento.atualizarParticipante(codigo, nome, orcamento);
    }

    public void removerParticipante(int codigo) {
        planejamento.removerParticipante(codigo);
    }

    public void cadastrarDespesaFixa(String id, String desc, double valorTotal, List<Participante> participantes, boolean essencial, int prioridade) {
        DespesaFixa d = new DespesaFixa(id, desc, valorTotal, participantes, essencial, prioridade);
        planejamento.adicionarDespesa(d);
    }

    public void cadastrarDespesaPorPessoa(String id, String desc, double valorUnitario, List<Participante> participantes, boolean essencial, int prioridade) {
        DespesaPorPessoa d = new DespesaPorPessoa(id, desc, valorUnitario, participantes, essencial, prioridade);
        planejamento.adicionarDespesa(d);
    }

    public void removerDespesa(String id) {
        planejamento.removerDespesa(id);
    }

    public void salvarEmArquivo(String caminho) throws IOException {
        repositorio.salvar(planejamento, caminho);
    }

    public void carregarDeArquivo(String caminho) throws IOException, ClassNotFoundException {
        this.planejamento = repositorio.carregar(caminho);
    }

    public static class ResultadoSimulacao {
        public boolean viavel;
        public List<Despesa> despesasMantidas = new ArrayList<>();
        public List<Despesa> despesasRetiradas = new ArrayList<>();
        public Map<Participante, Double> custosPorPessoa = new HashMap<>();
        public double custoTotal;
        public int somaPrioridades;
        public String mensagemErro;
    }

    public ResultadoSimulacao simularAlternativasOtimas() {
        ResultadoSimulacao resultado = new ResultadoSimulacao();
        List<Despesa> essenciais = new ArrayList<>();
        List<Despesa> opcionais = new ArrayList<>();

        for (Despesa d : planejamento.getDespesas()) {
            if (d.isEssencial()) {
                essenciais.add(d);
            } else {
                opcionais.add(d);
            }
        }

        Map<Participante, Double> custosEssenciais = calcularCustosParaConjunto(essenciais);
        for (Participante p : planejamento.getParticipantes()) {
            double gasto = custosEssenciais.getOrDefault(p, 0.0);
            if (gasto > p.getOrcamentoDisponivel()) {
                resultado.viavel = false;
                resultado.mensagemErro = "Sem solução viável: As despesas essenciais já ultrapassam o orçamento de " + p.getNome();
                resultado.despesasRetiradas.addAll(planejamento.getDespesas());
                return resultado;
            }
        }

        int n = opcionais.size();
        int melhorSomaPrioridade = -1;
        List<Despesa> melhorCombinacaoOpcional = null;
        Map<Participante, Double> melhoresCustos = null;

        int totalCombinacoes = 1 << n;
        for (int i = 0; i < totalCombinacoes; i++) {
            List<Despesa> candidatasOpcionais = new ArrayList<>();
            int somaPrioridadeAtual = 0;

            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    candidatasOpcionais.add(opcionais.get(j));
                    somaPrioridadeAtual += opcionais.get(j).getPrioridade();
                }
            }

            List<Despesa> conjuntoTeste = new ArrayList<>(essenciais);
            conjuntoTeste.addAll(candidatasOpcionais);

            Map<Participante, Double> custosTeste = calcularCustosParaConjunto(conjuntoTeste);

            boolean atendeOrcamento = true;
            for (Participante p : planejamento.getParticipantes()) {
                if (custosTeste.getOrDefault(p, 0.0) > p.getOrcamentoDisponivel()) {
                    atendeOrcamento = false;
                    break;
                }
            }

            if (atendeOrcamento) {
                if (somaPrioridadeAtual > melhorSomaPrioridade) {
                    melhorSomaPrioridade = somaPrioridadeAtual;
                    melhorCombinacaoOpcional = candidatasOpcionais;
                    melhoresCustos = custosTeste;
                }
            }
        }

        if (melhorCombinacaoOpcional != null) {
            resultado.viavel = true;
            resultado.despesasMantidas.addAll(essenciais);
            resultado.despesasMantidas.addAll(melhorCombinacaoOpcional);
            for (Despesa o : opcionais) {
                if (!melhorCombinacaoOpcional.contains(o)) {
                    resultado.despesasRetiradas.add(o);
                }
            }
            resultado.custosPorPessoa = melhoresCustos;
            resultado.somaPrioridades = melhorSomaPrioridade;

            double total = 0;
            for (Despesa d : resultado.despesasMantidas) {
                if (d instanceof DespesaFixa) total += d.getValorInformado();
                else if (d instanceof DespesaPorPessoa) total += d.getValorInformado() * d.getParticipantesInvolucrados().size();
            }
            resultado.custoTotal = total;
        }

        return resultado;
    }

    private Map<Participante, Double> calcularCustosParaConjunto(List<Despesa> lista) {
        Map<Participante, Double> totais = new HashMap<>();
        for (Participante p : planejamento.getParticipantes()) {
            totais.put(p, 0.0);
        }
        for (Despesa d : lista) {
            Map<Participante, Double> rateio = d.calcularRateio();
            for (Map.Entry<Participante, Double> e : rateio.entrySet()) {
                totais.put(e.getKey(), totais.getOrDefault(e.getKey(), 0.0) + e.getValue());
            }
        }
        return totais;
    }
}