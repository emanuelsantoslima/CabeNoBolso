package model;

import java.math.BigDecimal;
import java.util.*;

public class DespesaFixa extends Despesa {
    private static final long serialVersionUID = 1L;

    public DespesaFixa(String id, String descricao, BigDecimal valorTotal, List<Participante> participantes, boolean essencial, int prioridade) throws ValidacaoException {
        super(id, descricao, valorTotal, participantes, essencial, prioridade);
    }

    @Override
    public String getTipo() {
        return "Fixa";
    }

    @Override
    public Map<Participante, BigDecimal> calcularRateio() {
        Map<Participante, BigDecimal> rateio = new HashMap<>();
        List<Participante> ordenados = new ArrayList<>(getParticipantesInvolucrados());
        Collections.sort(ordenados);

        long valorTotalEmCentavos = getValorInformado().movePointRight(2).longValueExact();
        int quantidade = ordenados.size();

        long quotaBaseCentavos = valorTotalEmCentavos / quantidade;
        long sobraCentavos = valorTotalEmCentavos % quantidade;

        for (int i = 0; i < quantidade; i++) {
            long centavosPessoa = quotaBaseCentavos + (i < sobraCentavos ? 1 : 0);
            rateio.put(ordenados.get(i), BigDecimal.valueOf(centavosPessoa, 2));
        }

        return rateio;
    }
}