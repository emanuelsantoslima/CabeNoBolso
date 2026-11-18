package model;

import java.util.*;

public class DespesaPorPessoa extends Despesa {
    private static final long serialVersionUID = 1L;

    public DespesaPorPessoa(String id, String descricao, double valorUnitario, List<Participante> participantes, boolean essencial, int prioridade) {
        super(id, descricao, valorUnitario, participantes, essencial, prioridade);
    }

    @Override
    public String getTipo() {
        return "Por Pessoa";
    }

    @Override
    public Map<Participante, Double> calcularRateio() {
        Map<Participante, Double> rateio = new HashMap<>();
        for (Participante p : getParticipantesInvolucrados()) {
            rateio.put(p, getValorInformado());
        }
        return rateio;
    }
}