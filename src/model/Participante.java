package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Participante implements Serializable, Comparable<Participante> {
    private static final long serialVersionUID = 1L;

    private int codigo;
    private String nome;
    private BigDecimal orcamentoDisponivel;

    public Participante(int codigo, String nome, BigDecimal orcamentoDisponivel) throws ValidacaoException {
        if (codigo <= 0) {
            throw new ValidacaoException("O código do participante deve ser maior que zero.");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidacaoException("O nome do participante não pode ser vazio.");
        }
        if (orcamentoDisponivel == null || orcamentoDisponivel.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacaoException("O orçamento disponível deve ser maior ou igual a zero.");
        }
        if (orcamentoDisponivel.stripTrailingZeros().scale() > 2) {
            throw new ValidacaoException("O orçamento deve ter no máximo duas casas decimais.");
        }
        this.codigo = codigo;
        this.nome = nome.trim();
        this.orcamentoDisponivel = orcamentoDisponivel.setScale(2);
    }

    public int getCodigo() { return codigo; }
    public String getNome() { return nome; }

    public void setNome(String nome) throws ValidacaoException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidacaoException("O nome do participante não pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public BigDecimal getOrcamentoDisponivel() { return orcamentoDisponivel; }

    public void setOrcamentoDisponivel(BigDecimal orcamentoDisponivel) throws ValidacaoException {
        if (orcamentoDisponivel == null || orcamentoDisponivel.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacaoException("O orçamento disponível não pode ser negativo.");
        }
        if (orcamentoDisponivel.stripTrailingZeros().scale() > 2) {
            throw new ValidacaoException("O orçamento deve ter no máximo duas casas decimais.");
        }
        this.orcamentoDisponivel = orcamentoDisponivel.setScale(2);
    }

    public BigDecimal calcularExcesso(BigDecimal gasto) {
        BigDecimal excesso = gasto.subtract(orcamentoDisponivel);
        return excesso.compareTo(BigDecimal.ZERO) > 0 ? excesso : BigDecimal.ZERO.setScale(2);
    }

    @Override
    public int compareTo(Participante o) {
        return Integer.compare(this.codigo, o.getCodigo());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participante that = (Participante) o;
        return codigo == that.codigo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "[" + codigo + "] " + nome + " (R$ " + String.format("%.2f", orcamentoDisponivel) + ")";
    }
}