package controller;

import model.*;
import repository.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class PlanejamentoController {

    private Planejamento planejamento;
    private final IPersistenciaPlanejamento repositorio;

    public PlanejamentoController() {
        this.planejamento = new Planejamento("Passeio em Grupo");
        this.repositorio = new PersistenciaArquivoLocal();
    }

    public Planejamento getPlanejamento() { return planejamento; }

    public void novoPlanejamento(String titulo) {
        this.planejamento = new Planejamento(titulo);
    }

    public void cadastrarParticipante(int codigo, String nome, BigDecimal orcamento) throws ValidacaoException {
        Participante p = new Participante(codigo, nome, orcamento);
        planejamento.adicionarParticipante(p);
    }

    public void atualizarParticipante(int codigo, String nome, BigDecimal orcamento) throws ValidacaoException {
        planejamento.atualizarParticipante(codigo, nome, orcamento);
    }

    public void removerParticipante(int codigo) throws ValidacaoException {
        planejamento.removerParticipante(codigo);
    }

    public void cadastrarDespesaFixa(String id, String desc, BigDecimal valorTotal, List<Participante> participantes, boolean essencial, int prioridade) throws ValidacaoException {
        DespesaFixa d = new DespesaFixa(id, desc, valorTotal, participantes, essencial, prioridade);
        planejamento.adicionarDespesa(d);
    }

    public void cadastrarDespesaPorPessoa(String id, String desc, BigDecimal valorUnitario, List<Participante> participantes, boolean essencial, int prioridade) throws ValidacaoException {
        DespesaPorPessoa d = new DespesaPorPessoa(id, desc, valorUnitario, participantes, essencial, prioridade);
        planejamento.adicionarDespesa(d);
    }

    public void editarDespesa(String id, String desc, BigDecimal valor, String tipo, List<Participante> participantes, boolean essencial, int prioridade) throws ValidacaoException {
        Despesa nova;
        if (tipo.equalsIgnoreCase("Fixa")) {
            nova = new DespesaFixa(id, desc, valor, participantes, essencial, prioridade);
        } else {
            nova = new DespesaPorPessoa(id, desc, valor, participantes, essencial, prioridade);
        }
        planejamento.substituirDespesa(id, nova);
    }

    public void removerDespesa(String id) {
        planejamento.removerDespesa(id);
    }

    public void salvarEmArquivo(String caminho) throws IOException {
        repositorio.salvar(planejamento, caminho);
    }

    public void carregarDeArquivo(String caminho) throws IOException, ClassNotFoundException {
        this.planejamento = repositorio.carregar(caminho);
    }

    public SimuladorAlternativas.ResultadoSimulacao simularAlternativasOtimas() throws ValidacaoException {
        return SimuladorAlternativas.simularAlternativasOtimas(planejamento);
    }
}