package model;

import java.io.Serializable;
import java.util.*;

public class Planejamento implements Serializable {
    private static final long serialVersionUID = 1L;

    private String titulo;
    private final Map<Integer, Participante> participantesMap;
    private final List<Despesa> despesas;

    public Planejamento(String titulo) {
        this.titulo = (titulo != null && !titulo.trim().isEmpty()) ? titulo.trim() : "Planejamento Grupo";
        this.participantesMap = new HashMap<>();
        this.despesas = new ArrayList<>();
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public void adicionarParticipante(Participante p) {
        if (participantesMap.containsKey(p.getCodigo())) {
            throw new IllegalArgumentException("Código de participante " + p.getCodigo() + " já cadastrado.");
        }
        participantesMap.put(p.getCodigo(), p);
    }

    public void atualizarParticipante(int codigo, String novoNome, double novoOrcamento) {
        Participante p = participantesMap.get(codigo);
        if (p == null) {
            throw new IllegalArgumentException("Participante não encontrado para atualização.");
        }
        p.setNome(novoNome);
        p.setOrcamentoDisponivel(novoOrcamento);
    }

    public void removerParticipante(int codigo) {
        Participante p = participantesMap.get(codigo);
        if (p == null) return;
        
        for (Despesa d : despesas) {
            if (d.getParticipantesInvolucrados().contains(p)) {
                throw new IllegalStateException("Não é possível remover o participante " + p.getNome() + 
                        " pois ele possui a despesa '" + d.getDescricao() + "' vinculada.");
            }
        }
        participantesMap.remove(codigo);
    }

    public List<Participante> getParticipantes() {
        List<Participante> list = new ArrayList<>(participantesMap.values());
        Collections.sort(list);
        return list;
    }

    public Participante getParticipantePorCodigo(int codigo) {
        return participantesMap.get(codigo);
    }

    public void adicionarDespesa(Despesa d) {
        for (Despesa existente : despesas) {
            if (existente.getId().equalsIgnoreCase(d.getId())) {
                throw new IllegalArgumentException("Já existe uma despesa com o ID " + d.getId());
            }
        }
        despesas.add(d);
    }

    public void removerDespesa(String id) {
        despesas.removeIf(d -> d.getId().equalsIgnoreCase(id));
    }

    public List<Despesa> getDespesas() {
        return Collections.unmodifiableList(despesas);
    }

    public double calcularCustoTotalGeral() {
        double total = 0;
        for (Despesa d : despesas) {
            if (d instanceof DespesaFixa) {
                total += d.getValorInformado();
            } else if (d instanceof DespesaPorPessoa) {
                total += d.getValorInformado() * d.getParticipantesInvolucrados().size();
            }
        }
        return total;
    }

    public Map<Participante, Double> calcularTotaisIndividuais() {
        Map<Participante, Double> totais = new HashMap<>();
        for (Participante p : participantesMap.values()) {
            totais.put(p, 0.0);
        }

        for (Despesa d : despesas) {
            Map<Participante, Double> rateioDespesa = d.calcularRateio();
            for (Map.Entry<Participante, Double> entry : rateioDespesa.entrySet()) {
                Participante p = entry.getKey();
                double valor = entry.getValue();
                totais.put(p, totais.getOrDefault(p, 0.0) + valor);
            }
        }
        return totais;
    }
}