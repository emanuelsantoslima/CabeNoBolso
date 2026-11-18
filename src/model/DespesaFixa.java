package model;

import java.util.*;

public class DespesaFixa extends Despesa {
    private static final long serialVersionUID = 1L;

    public DespesaFixa(String id, String descricao, double valorTotal, List<Participante> participantes, boolean essencial, int prioridade) {
        super(id, descricao, valorTotal, participantes, essencial, prioridade);
    }

    @Override
    public String getTipo() {
        return "Fixa";
    }

    @Override
    public Map<Participante, Double> calcularRateio() {
        Map<Participante, Double> rateio = new HashMap<>();
        List<Participante> ordenados = new ArrayList<>(getParticipantesInvolucrados());
        Collections.sort(ordenados);

        long valorTotalEmCentavos = Math.round(getValorInformado() * 100.0);
        int quantidade = ordenados.size();

        long quotaBaseCentavos = valorTotalEmCentavos / quantidade;
        long sobraCentavos = valorTotalEmCentavos % quantidade;

        for (int i = 0; i < quantidade; i++) {
            long centavosPessoa = quotaBaseCentavos + (i < sobraCentavos ? 1 : 0);
            rateio.put(ordenados.get(i), centavosPessoa / 100.0);
        }

        return rateio;
    }
}