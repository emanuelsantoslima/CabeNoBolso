package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Despesa implements Serializable {
    private static final long serialVersionUID = 1L;
    // acima disso o rateio em centavos não cabe num long
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("999999999.99");

    private String id;
    private String descricao;
    private BigDecimal valorInformado;
    private List<Participante> participantesInvolucrados;
    private boolean essencial;
    private int prioridade;

    public Despesa(String id, String descricao, BigDecimal valorInformado, List<Participante> participantes, boolean essencial, int prioridade) throws ValidacaoException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidacaoException("ID da despesa inválido.");
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new ValidacaoException("Descrição da despesa é obrigatória.");
        }
        validarValor(valorInformado);
        if (participantes == null || participantes.isEmpty()) {
            throw new ValidacaoException("A despesa deve ter pelo menos um participante cadastrado.");
        }
        if (!essencial && prioridade < 1) {
            throw new ValidacaoException("A prioridade de uma despesa opcional deve ser um número inteiro maior ou igual a 1.");
        }
        this.id = id.trim();
        this.descricao = descricao.trim();
        this.valorInformado = valorInformado.setScale(2);

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
    public BigDecimal getValorInformado() { return valorInformado; }
    public List<Participante> getParticipantesInvolucrados() { return Collections.unmodifiableList(participantesInvolucrados); }
    public boolean isEssencial() { return essencial; }
    public int getPrioridade() { return prioridade; }

    public void setDescricao(String descricao) throws ValidacaoException {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new ValidacaoException("Descrição da despesa é obrigatória.");
        }
        this.descricao = descricao.trim();
    }

    public void setValorInformado(BigDecimal valorInformado) throws ValidacaoException {
        validarValor(valorInformado);
        this.valorInformado = valorInformado.setScale(2);
    }

    private static void validarValor(BigDecimal valor) throws ValidacaoException {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("O valor da despesa deve ser maior que zero.");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new ValidacaoException("O valor da despesa deve ter no máximo duas casas decimais.");
        }
        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            throw new ValidacaoException("O valor da despesa deve ser de no máximo R$ 999.999.999,99.");
        }
    }

    public void setParticipantesInvolucrados(List<Participante> participantes) throws ValidacaoException {
        if (participantes == null || participantes.isEmpty()) {
            throw new ValidacaoException("A despesa deve ter pelo menos um participante.");
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
    public abstract Map<Participante, BigDecimal> calcularRateio();

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