package model;

import java.io.Serializable;
import java.util.Objects;

public class Participante implements Serializable, Comparable<Participante> {
    private static final long serialVersionUID = 1L;

    private int codigo;
    private String nome;
    private double orcamentoDisponivel;

    public Participante(int codigo, String nome, double orcamentoDisponivel) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("O código do participante deve ser maior que zero.");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do participante não pode ser vazio.");
        }
        if (orcamentoDisponivel < 0) {
            throw new IllegalArgumentException("O orçamento disponível deve ser maior ou igual a zero.");
        }
        this.codigo = codigo;
        this.nome = nome.trim();
        this.orcamentoDisponivel = orcamentoDisponivel;
    }

    public int getCodigo() { return codigo; }
    public String getNome() { return nome; }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do participante não pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public double getOrcamentoDisponivel() { return orcamentoDisponivel; }

    public void setOrcamentoDisponivel(double orcamentoDisponivel) {
        if (orcamentoDisponivel < 0) {
            throw new IllegalArgumentException("O orçamento disponível não pode ser negativo.");
        }
        this.orcamentoDisponivel = orcamentoDisponivel;
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