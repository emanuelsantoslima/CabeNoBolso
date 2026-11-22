package model;

import java.math.BigDecimal;
import java.util.*;

public class DespesaPorPessoa extends Despesa {
    private static final long serialVersionUID = 1L;

    public DespesaPorPessoa(String id, String descricao, BigDecimal valorUnitario, List<Participante> participantes, boolean essencial, int prioridade) throws ValidacaoException {
        super(id, descricao, valorUnitario, participantes, essencial, prioridade);
    }

    @Override
    public String getTipo() {
        return "Por Pessoa";
    }

    @Override
    public Map<Participante, BigDecimal> calcularRateio() {
        Map<Participante, BigDecimal> rateio = new HashMap<>();
        for (Participante p : getParticipantesInvolucrados()) {
            rateio.put(p, getValorInformado());
        }
        return rateio;
    }
}