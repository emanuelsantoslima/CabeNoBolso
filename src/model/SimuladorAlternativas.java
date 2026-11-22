package model;

import java.math.BigDecimal;
import java.util.*;

public class SimuladorAlternativas {
    public static final int MAXIMO_OPCIONAIS = 10;

    public static class ResultadoSimulacao {
        public boolean viavel;
        public List<Despesa> despesasMantidas = new ArrayList<>();
        public List<Despesa> despesasRetiradas = new ArrayList<>();
        public Map<Participante, BigDecimal> custosPorPessoa = new HashMap<>();
        public BigDecimal custoTotal;
        public long somaPrioridades;
        public String mensagemErro;
        public Map<Participante, BigDecimal> custosAtuais = new HashMap<>();
        public BigDecimal custoTotalAtual;
        public Map<Participante, BigDecimal> excessos = new TreeMap<>();
    }

    public static ResultadoSimulacao simularAlternativasOtimas(Planejamento planejamento) throws ValidacaoException {
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

        if (opcionais.size() > MAXIMO_OPCIONAIS) {
            throw new ValidacaoException("O simulador aceita no máximo " + MAXIMO_OPCIONAIS +
                    " despesas opcionais, e este planejamento tem " + opcionais.size() + ".");
        }

        resultado.custosAtuais = planejamento.calcularTotaisIndividuais();
        resultado.custoTotalAtual = planejamento.calcularCustoTotalGeral();

        Map<Participante, BigDecimal> custosEssenciais = calcularCustosParaConjunto(planejamento, essenciais);
        for (Participante p : planejamento.getParticipantes()) {
            BigDecimal gasto = custosEssenciais.getOrDefault(p, BigDecimal.ZERO);
            if (gasto.compareTo(p.getOrcamentoDisponivel()) > 0) {
                resultado.excessos.put(p, p.calcularExcesso(gasto));
            }
        }
        if (!resultado.excessos.isEmpty()) {
            resultado.viavel = false;
            resultado.mensagemErro = "Sem solução viável: As despesas essenciais já ultrapassam o orçamento.";
            resultado.despesasMantidas.addAll(essenciais);
            resultado.despesasRetiradas.addAll(opcionais);
            resultado.custosPorPessoa = custosEssenciais;
            return resultado;
        }

        int n = opcionais.size();
        long melhorSomaPrioridade = -1;
        List<Despesa> melhorCombinacaoOpcional = null;
        Map<Participante, BigDecimal> melhoresCustos = null;

        int totalCombinacoes = 1 << n;
        for (int i = 0; i < totalCombinacoes; i++) {
            List<Despesa> candidatasOpcionais = new ArrayList<>();
            long somaPrioridadeAtual = 0;

            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    candidatasOpcionais.add(opcionais.get(j));
                    somaPrioridadeAtual += opcionais.get(j).getPrioridade();
                }
            }

            List<Despesa> conjuntoTeste = new ArrayList<>(essenciais);
            conjuntoTeste.addAll(candidatasOpcionais);

            Map<Participante, BigDecimal> custosTeste = calcularCustosParaConjunto(planejamento, conjuntoTeste);

            boolean atendeOrcamento = true;
            for (Participante p : planejamento.getParticipantes()) {
                if (custosTeste.getOrDefault(p, BigDecimal.ZERO).compareTo(p.getOrcamentoDisponivel()) > 0) {
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

            BigDecimal total = BigDecimal.ZERO.setScale(2);
            for (BigDecimal custo : melhoresCustos.values()) {
                total = total.add(custo);
            }
            resultado.custoTotal = total;
        }

        return resultado;
    }

    private static Map<Participante, BigDecimal> calcularCustosParaConjunto(Planejamento planejamento, List<Despesa> lista) {
        Map<Participante, BigDecimal> totais = new TreeMap<>();
        for (Participante p : planejamento.getParticipantes()) {
            totais.put(p, BigDecimal.ZERO.setScale(2));
        }
        for (Despesa d : lista) {
            Map<Participante, BigDecimal> rateio = d.calcularRateio();
            for (Map.Entry<Participante, BigDecimal> e : rateio.entrySet()) {
                totais.put(e.getKey(), totais.getOrDefault(e.getKey(), BigDecimal.ZERO).add(e.getValue()));
            }
        }
        return totais;
    }
}