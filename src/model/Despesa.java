package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Despesa implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String descricao;
    private double valorInformado;
    private List<Participante> participantesInvolucrados;
    private boolean essencial;
    private int prioridade;

    public Despesa(String id, String descricao, double valorInformado, List<Participante> participantes, boolean essencial, int prioridade) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da despesa inválido.");
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da despesa é obrigatória.");
        }
        if (valorInformado <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }
        if (participantes == null || participantes.isEmpty()) {
            throw new IllegalArgumentException("A despesa deve ter pelo menos um participante cadastrado.");
        }
        this.id = id;
        this.descricao = descricao.trim();
        this.valorInformado = valorInformado;

        List<Participante> semDuplicados = new ArrayList<>();
        for (Participante p : participantes) {
            if (!semDuplicados.contains(p)) {
                semDuplicados.add(p);
            }
        }
        this.participantesInvolucrados = semDuplicados;
        this.essencial = essencial;
        this.prioridade = essencial ? 0 : prioridade;
    }

    public String getId() { return id; }
    public String getDescricao() { return descricao; }
    public double getValorInformado() { return valorInformado; }
    public List<Participante> getParticipantesInvolucrados() { return Collections.unmodifiableList(participantesInvolucrados); }
    public boolean isEssencial() { return essencial; }
    public int getPrioridade() { return prioridade; }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da despesa é obrigatória.");
        }
        this.descricao = descricao.trim();
    }

    public void setValorInformado(double valorInformado) {
        if (valorInformado <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }
        this.valorInformado = valorInformado;
    }

    public void setParticipantesInvolucrados(List<Participante> participantes) {
        if (participantes == null || participantes.isEmpty()) {
            throw new IllegalArgumentException("A despesa deve ter pelo menos um participante.");
        }
        List<Participante> semDuplicados = new ArrayList<>();
        for (Participante p : participantes) {
            if (!semDuplicados.contains(p)) {
                semDuplicados.add(p);
            }
        }
        this.participantesInvolucrados = semDuplicados;
    }

    public void setEssencial(boolean essencial) { this.essencial = essencial; }
    public void setPrioridade(int prioridade) { this.prioridade = prioridade; }

    public abstract String getTipo();
    public abstract Map<Participante, Double> calcularRateio();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Despesa despesa = (Despesa) o;
        return Objects.equals(id, despesa.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}