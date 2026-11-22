package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

public class Planejamento implements Serializable {
    private static final long serialVersionUID = 1L;

    private String titulo;
    private final Map<Integer, Participante> participantesMap;
    private final List<Despesa> despesas;

    public Planejamento(String titulo) {
        this.titulo = (titulo != null && !titulo.trim().isEmpty()) ? titulo.trim() : "Planejamento Grupo";
        this.participantesMap = new TreeMap<>();
        this.despesas = new ArrayList<>();
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public void adicionarParticipante(Participante p) throws ValidacaoException {
        if (participantesMap.containsKey(p.getCodigo())) {
            throw new ValidacaoException("Código de participante " + p.getCodigo() + " já cadastrado.");
        }
        participantesMap.put(p.getCodigo(), p);
    }

    public void atualizarParticipante(int codigo, String novoNome, BigDecimal novoOrcamento) throws ValidacaoException {
        Participante p = participantesMap.get(codigo);
        if (p == null) {
            throw new ValidacaoException("Participante não encontrado para atualização.");
        }
        // valida os dois valores antes, para um erro no orçamento não deixar o nome já alterado
        new Participante(codigo, novoNome, novoOrcamento);
        p.setNome(novoNome);
        p.setOrcamentoDisponivel(novoOrcamento);
    }

    public void removerParticipante(int codigo) throws ValidacaoException {
        Participante p = participantesMap.get(codigo);
        if (p == null) return;
        
        for (Despesa d : despesas) {
            if (d.getParticipantesInvolucrados().contains(p)) {
                throw new ValidacaoException("Não é possível remover o participante " + p.getNome() + 
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

    public void adicionarDespesa(Despesa d) throws ValidacaoException {
        for (Despesa existente : despesas) {
            if (existente.getId().equalsIgnoreCase(d.getId())) {
                throw new ValidacaoException("Já existe uma despesa com o ID " + d.getId());
            }
        }
        verificarParticipantesCadastrados(d);
        despesas.add(d);
    }

    public void substituirDespesa(String id, Despesa nova) throws ValidacaoException {
        verificarParticipantesCadastrados(nova);
        for (int i = 0; i < despesas.size(); i++) {
            if (despesas.get(i).getId().equalsIgnoreCase(id)) {
                despesas.set(i, nova);
                return;
            }
        }
        throw new ValidacaoException("Despesa não encontrada para alteração.");
    }

    // != de propósito: uma cópia com o mesmo código não pode entrar na despesa
    private void verificarParticipantesCadastrados(Despesa d) throws ValidacaoException {
        for (Participante p : d.getParticipantesInvolucrados()) {
            if (participantesMap.get(p.getCodigo()) != p) {
                throw new ValidacaoException("O participante " + p.getNome() + " não está cadastrado neste planejamento.");
            }
        }
    }

    public void removerDespesa(String id) {
        despesas.removeIf(d -> d.getId().equalsIgnoreCase(id));
    }

    public List<Despesa> getDespesas() {
        return Collections.unmodifiableList(despesas);
    }

    public BigDecimal calcularCustoTotalGeral() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Despesa d : despesas) {
            for (BigDecimal parte : d.calcularRateio().values()) {
                total = total.add(parte);
            }
        }
        return total;
    }

    public Map<Participante, BigDecimal> calcularTotaisIndividuais() {
        Map<Participante, BigDecimal> totais = new HashMap<>();
        for (Participante p : participantesMap.values()) {
            totais.put(p, BigDecimal.ZERO.setScale(2));
        }

        for (Despesa d : despesas) {
            Map<Participante, BigDecimal> rateioDespesa = d.calcularRateio();
            for (Map.Entry<Participante, BigDecimal> entry : rateioDespesa.entrySet()) {
                Participante p = entry.getKey();
                BigDecimal valor = entry.getValue();
                totais.put(p, totais.getOrDefault(p, BigDecimal.ZERO).add(valor));
            }
        }
        return totais;
    }

    // usado ao carregar: um arquivo estragado pode abrir com dados que quebram as regras ou as contas
    public boolean estaConsistente() {
        try {
            for (Participante p : participantesMap.values()) {
                BigDecimal orcamento = p.getOrcamentoDisponivel();
                if (participantesMap.get(p.getCodigo()) != p || p.getNome().isEmpty()
                        || orcamento.scale() != 2 || orcamento.signum() < 0) {
                    return false;
                }
            }
            for (Despesa d : despesas) {
                BigDecimal valor = d.getValorInformado();
                if (d.getId().isEmpty() || d.getDescricao().isEmpty() || valor.scale() != 2 || valor.signum() <= 0) {
                    return false;
                }
                verificarParticipantesCadastrados(d);
            }
            calcularTotaisIndividuais();
            return true;
        } catch (ValidacaoException | RuntimeException e) {
            return false;
        }
    }
}