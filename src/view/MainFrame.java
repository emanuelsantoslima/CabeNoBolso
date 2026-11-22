package view;

import controller.PlanejamentoController;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    private final PlanejamentoController controller;

    private DefaultTableModel modelParticipantes;
    private DefaultTableModel modelDespesas;
    private DefaultTableModel modelResumo;
    private JTable tableParticipantes;
    private JTable tableDespesas;
    private JTable tableResumo;
    private JLabel lblCustoTotal;
    private JTextArea txtResultadoSimulacao;

    private JTextField txtCodPart, txtNomePart, txtOrcPart;
    private JTextField txtIdDesp, txtDescDesp, txtValorDesp, txtPrioridadeDesp;
    private JComboBox<String> cbTipoDesp;
    private JCheckBox chkEssencial;
    private JList<Participante> listParticipantesSelecao;

    public MainFrame() {
        this.controller = new PlanejamentoController();
        initComponents();
        atualizarTabelas();
    }

    private void initComponents() {
        setTitle("Cabe no Bolso - Planejador de Despesas Compartilhadas");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("1. Participantes", createPanelParticipantes());
        tabbedPane.addTab("2. Despesas", createPanelDespesas());
        tabbedPane.addTab("3. Resumo do Planejamento", createPanelResumo());
        tabbedPane.addTab("4. Simulador (Bônus Extra)", createPanelSimulador());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSalvar = new JButton("Salvar Planejamento");
        JButton btnCarregar = new JButton("Carregar Planejamento");
        JButton btnCarregarCasoRef = new JButton("Resetar p/ Caso de Referência");
        JButton btnNovo = new JButton("Novo Planejamento");

        btnSalvar.addActionListener(e -> salvarArquivo());
        btnCarregar.addActionListener(e -> carregarArquivo());
        btnCarregarCasoRef.addActionListener(e -> {
            try {
                carregarCasoDeReferencia();
                JOptionPane.showMessageDialog(this, "Caso de referência carregado com sucesso!");
            } catch (ValidacaoException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnNovo.addActionListener(e -> {
            controller.novoPlanejamento("Novo Planejamento");
            atualizarTabelas();
        });

        topPanel.add(btnSalvar);
        topPanel.add(btnCarregar);
        topPanel.add(btnCarregarCasoRef);
        topPanel.add(btnNovo);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPanelParticipantes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Cadastrar / Editar Participante"));

        txtCodPart = new JTextField();
        txtNomePart = new JTextField();
        txtOrcPart = new JTextField();

        form.add(new JLabel("Código Único (Inteiro > 0):"));
        form.add(txtCodPart);
        form.add(new JLabel("Nome do Participante:"));
        form.add(txtNomePart);
        form.add(new JLabel("Orçamento Disponível (R$):"));
        form.add(txtOrcPart);

        JButton btnAdicionar = new JButton("Salvar Participante");
        JButton btnRemover = new JButton("Remover Selecionado");
        JButton btnAlterar = new JButton("Alterar Selecionado");
        JButton btnLimpar = new JButton("Limpar");

        btnAdicionar.addActionListener(e -> acaoSalvarParticipante());
        btnRemover.addActionListener(e -> acaoRemoverParticipante());
        btnAlterar.addActionListener(e -> acaoAlterarParticipante());
        btnLimpar.addActionListener(e -> limparFormularioParticipante());

        form.add(btnAdicionar);
        form.add(btnRemover);
        form.add(btnAlterar);
        form.add(btnLimpar);

        modelParticipantes = criarModelo(new Object[]{"Código", "Nome", "Orçamento (R$)"});
        tableParticipantes = new JTable(modelParticipantes);
        tableParticipantes.getSelectionModel().addListSelectionListener(e -> preencherFormularioParticipante());

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableParticipantes), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPanelDespesas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(8, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Cadastrar / Editar Despesa"));

        txtIdDesp = new JTextField();
        txtDescDesp = new JTextField();
        txtValorDesp = new JTextField();
        cbTipoDesp = new JComboBox<>(new String[]{"Fixa", "Por Pessoa"});
        chkEssencial = new JCheckBox("Despesa Essencial (Marca para Simulador)");
        txtPrioridadeDesp = new JTextField("1");

        form.add(new JLabel("ID Único:"));
        form.add(txtIdDesp);
        form.add(new JLabel("Descrição:"));
        form.add(txtDescDesp);
        form.add(new JLabel("Valor (R$):"));
        form.add(txtValorDesp);
        form.add(new JLabel("Tipo de Despesa:"));
        form.add(cbTipoDesp);
        form.add(new JLabel("Caráter:"));
        form.add(chkEssencial);
        form.add(new JLabel("Prioridade Opcional (a partir de 1):"));
        form.add(txtPrioridadeDesp);

        JButton btnAddDesp = new JButton("Adicionar Despesa");
        JButton btnRemDesp = new JButton("Excluir Despesa Selecionada");
        JButton btnEditDesp = new JButton("Salvar Alteração");
        JButton btnLimparDesp = new JButton("Limpar");

        btnAddDesp.addActionListener(e -> acaoSalvarDespesa());
        btnRemDesp.addActionListener(e -> acaoRemoverDespesa());
        btnEditDesp.addActionListener(e -> acaoEditarDespesa());
        btnLimparDesp.addActionListener(e -> limparFormularioDespesa());

        form.add(btnAddDesp);
        form.add(btnRemDesp);
        form.add(btnEditDesp);
        form.add(btnLimparDesp);

        listParticipantesSelecao = new JList<>();
        listParticipantesSelecao.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollList = new JScrollPane(listParticipantesSelecao);
        scrollList.setBorder(BorderFactory.createTitledBorder("Selecione os Participantes da Despesa"));

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(form);
        centerPanel.add(scrollList);

        modelDespesas = criarModelo(new Object[]{"ID", "Descrição", "Tipo", "Valor (R$)", "Essencial", "Prioridade", "Participantes"});
        tableDespesas = new JTable(modelDespesas);
        tableDespesas.getSelectionModel().addListSelectionListener(e -> preencherFormularioDespesa());

        panel.add(centerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableDespesas), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPanelResumo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modelResumo = criarModelo(new Object[]{"Código", "Nome", "Orçamento (R$)", "Total Individual (R$)", "Situação / Excesso"});
        tableResumo = new JTable(modelResumo);

        lblCustoTotal = new JLabel("Custo Total Geral do Passeio: R$ 0,00");
        lblCustoTotal.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton btnAtualizar = new JButton("Recalcular / Atualizar Resumo");
        btnAtualizar.addActionListener(e -> atualizarTabelas());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(lblCustoTotal);
        bottomPanel.add(btnAtualizar);

        panel.add(new JScrollPane(tableResumo), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPanelSimulador() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtResultadoSimulacao = new JTextArea();
        txtResultadoSimulacao.setEditable(false);
        txtResultadoSimulacao.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JButton btnExecutar = new JButton("Executar Otimização de Opcionais");
        btnExecutar.addActionListener(e -> {
            SimuladorAlternativas.ResultadoSimulacao res;
            try {
                res = controller.simularAlternativasOtimas();
            } catch (ValidacaoException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("=== RESULTADO DA SIMULAÇÃO DE ALTERNATIVAS ===\n\n");

            if (!res.viavel) {
                sb.append(" STATUS: INVIÁVEL\n");
                sb.append(" Mensagem: ").append(res.mensagemErro).append("\n");
                for (Map.Entry<Participante, BigDecimal> entry : res.excessos.entrySet()) {
                    sb.append(String.format(" Excesso de %s: R$ %.2f\n", entry.getKey().getNome(), entry.getValue()));
                }
            } else {
                sb.append(" STATUS: SOLUÇÃO OTIMIZADA ENCONTRADA!\n");
                sb.append(" Soma Máxima de Prioridades Opcionais Mantidas: ").append(res.somaPrioridades).append("\n");
                sb.append(" Custo Total Atual do Passeio: R$ ").append(String.format("%.2f", res.custoTotalAtual)).append("\n");
                sb.append(" Custo Total Ajustado do Passeio: R$ ").append(String.format("%.2f", res.custoTotal)).append("\n\n");

                sb.append("--- DESPESAS MANTIDAS ---\n");
                for (Despesa d : res.despesasMantidas) {
                    sb.append(" [MANTIDA] ").append(d.getDescricao())
                            .append(" (").append(d.getTipo()).append(", R$ ").append(String.format("%.2f", d.getValorInformado()))
                            .append(d.isEssencial() ? ", ESSENCIAL" : ", Prioridade: " + d.getPrioridade()).append(")\n");
                }

                sb.append("\n--- DESPESAS RETIRADAS ---\n");
                if (res.despesasRetiradas.isEmpty()) {
                    sb.append(" Nenhuma despesa precisou ser retirada.\n");
                } else {
                    for (Despesa d : res.despesasRetiradas) {
                        sb.append(" [RETIRADA] ").append(d.getDescricao())
                                .append(" (Prioridade: ").append(d.getPrioridade()).append(")\n");
                    }
                }

                sb.append("\n--- IMPACTO NOS PARTICIPANTES ---\n");
                for (Map.Entry<Participante, BigDecimal> entry : res.custosPorPessoa.entrySet()) {
                    Participante p = entry.getKey();
                    BigDecimal novoCusto = entry.getValue();
                    sb.append(String.format(" %-15s | Orçamento: R$ %6.2f | Custo Atual: R$ %6.2f | Novo Custo: R$ %6.2f\n",
                            p.getNome(), p.getOrcamentoDisponivel(), res.custosAtuais.get(p), novoCusto));
                }
            }
            txtResultadoSimulacao.setText(sb.toString());
        });

        panel.add(btnExecutar, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtResultadoSimulacao), BorderLayout.CENTER);
        return panel;
    }

    private void acaoSalvarParticipante() {
        try {
            int cod = Integer.parseInt(txtCodPart.getText().trim());
            String nome = txtNomePart.getText().trim();
            BigDecimal orc = new BigDecimal(txtOrcPart.getText().trim().replace(",", "."));

            controller.cadastrarParticipante(cod, nome, orc);

            txtCodPart.setText("");
            txtNomePart.setText("");
            txtOrcPart.setText("");
            atualizarTabelas();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Código e Orçamento devem ser valores numéricos válidos.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoAlterarParticipante() {
        int idx = tableParticipantes.getSelectedRow();
        if (idx >= 0) {
            try {
                int cod = (int) modelParticipantes.getValueAt(idx, 0);
                String nome = txtNomePart.getText().trim();
                BigDecimal orc = new BigDecimal(txtOrcPart.getText().trim().replace(",", "."));

                controller.atualizarParticipante(cod, nome, orc);
                atualizarTabelas();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Orçamento deve ser um valor numérico válido.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um participante na tabela.");
        }
    }

    private void preencherFormularioParticipante() {
        int idx = tableParticipantes.getSelectedRow();
        if (idx < 0) return;
        txtCodPart.setText(String.valueOf(modelParticipantes.getValueAt(idx, 0)));
        txtCodPart.setEditable(false);
        txtNomePart.setText((String) modelParticipantes.getValueAt(idx, 1));
        txtOrcPart.setText((String) modelParticipantes.getValueAt(idx, 2));
    }

    private void limparFormularioParticipante() {
        tableParticipantes.clearSelection();
        txtCodPart.setEditable(true);
        txtCodPart.setText("");
        txtNomePart.setText("");
        txtOrcPart.setText("");
    }

    private void acaoRemoverParticipante() {
        int idx = tableParticipantes.getSelectedRow();
        if (idx >= 0) {
            int cod = (int) modelParticipantes.getValueAt(idx, 0);
            try {
                controller.removerParticipante(cod);
                atualizarTabelas();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Impossível Remover", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um participante na tabela.");
        }
    }

    private void acaoSalvarDespesa() {
        try {
            String id = txtIdDesp.getText().trim();
            String desc = txtDescDesp.getText().trim();
            BigDecimal val = new BigDecimal(txtValorDesp.getText().trim().replace(",", "."));
            String tipo = (String) cbTipoDesp.getSelectedItem();
            boolean ess = chkEssencial.isSelected();
            int prio = ess ? 0 : Integer.parseInt(txtPrioridadeDesp.getText().trim());

            List<Participante> selecionados = listParticipantesSelecao.getSelectedValuesList();

            if (tipo.equalsIgnoreCase("Fixa")) {
                controller.cadastrarDespesaFixa(id, desc, val, selecionados, ess, prio);
            } else {
                controller.cadastrarDespesaPorPessoa(id, desc, val, selecionados, ess, prio);
            }

            txtIdDesp.setText("");
            txtDescDesp.setText("");
            txtValorDesp.setText("");
            atualizarTabelas();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifique os valores numéricos de valor e prioridade.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoEditarDespesa() {
        int idx = tableDespesas.getSelectedRow();
        if (idx >= 0) {
            try {
                String id = (String) modelDespesas.getValueAt(idx, 0);
                String desc = txtDescDesp.getText().trim();
                BigDecimal val = new BigDecimal(txtValorDesp.getText().trim().replace(",", "."));
                String tipo = (String) cbTipoDesp.getSelectedItem();
                boolean ess = chkEssencial.isSelected();
                int prio = ess ? 0 : Integer.parseInt(txtPrioridadeDesp.getText().trim());

                List<Participante> selecionados = listParticipantesSelecao.getSelectedValuesList();

                controller.editarDespesa(id, desc, val, tipo, selecionados, ess, prio);
                atualizarTabelas();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Verifique os valores numéricos de valor e prioridade.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma despesa na tabela.");
        }
    }

    private void preencherFormularioDespesa() {
        int idx = tableDespesas.getSelectedRow();
        if (idx < 0) return;
        Despesa d = controller.getPlanejamento().getDespesas().get(idx);
        txtIdDesp.setText(d.getId());
        txtIdDesp.setEditable(false);
        txtDescDesp.setText(d.getDescricao());
        txtValorDesp.setText(String.format("%.2f", d.getValorInformado()));
        cbTipoDesp.setSelectedItem(d.getTipo());
        chkEssencial.setSelected(d.isEssencial());
        txtPrioridadeDesp.setText(String.valueOf(d.getPrioridade()));

        List<Participante> todos = controller.getPlanejamento().getParticipantes();
        int[] indices = new int[d.getParticipantesInvolucrados().size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = todos.indexOf(d.getParticipantesInvolucrados().get(i));
        }
        listParticipantesSelecao.setSelectedIndices(indices);
    }

    private void limparFormularioDespesa() {
        tableDespesas.clearSelection();
        txtIdDesp.setEditable(true);
        txtIdDesp.setText("");
        txtDescDesp.setText("");
        txtValorDesp.setText("");
        chkEssencial.setSelected(false);
        txtPrioridadeDesp.setText("1");
        listParticipantesSelecao.clearSelection();
    }

    private void acaoRemoverDespesa() {
        int idx = tableDespesas.getSelectedRow();
        if (idx >= 0) {
            String id = (String) modelDespesas.getValueAt(idx, 0);
            controller.removerDespesa(id);
            atualizarTabelas();
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma despesa na tabela.");
        }
    }

    private void atualizarTabelas() {
        // as tabelas são refeitas e perdem a seleção, então os formulários voltam ao estado inicial
        limparFormularioParticipante();
        limparFormularioDespesa();

        Planejamento p = controller.getPlanejamento();

        modelParticipantes.setRowCount(0);
        DefaultListModel<Participante> listModel = new DefaultListModel<>();

        for (Participante part : p.getParticipantes()) {
            modelParticipantes.addRow(new Object[]{part.getCodigo(), part.getNome(), String.format("%.2f", part.getOrcamentoDisponivel())});
            listModel.addElement(part);
        }
        listParticipantesSelecao.setModel(listModel);

        modelDespesas.setRowCount(0);
        for (Despesa d : p.getDespesas()) {
            StringBuilder partsStr = new StringBuilder();
            for (Participante part : d.getParticipantesInvolucrados()) {
                partsStr.append(part.getNome()).append(", ");
            }
            if (partsStr.length() > 2) partsStr.setLength(partsStr.length() - 2);

            modelDespesas.addRow(new Object[]{
                    d.getId(), d.getDescricao(), d.getTipo(), String.format("%.2f", d.getValorInformado()),
                    d.isEssencial() ? "Sim" : "Não", d.getPrioridade(), partsStr.toString()
            });
        }

        List<Object> colunas = new ArrayList<>(List.of("Código", "Nome", "Orçamento (R$)"));
        for (Despesa d : p.getDespesas()) {
            colunas.add(d.getDescricao() + " (R$)");
        }
        colunas.add("Total Individual (R$)");
        colunas.add("Situação / Excesso");
        modelResumo.setRowCount(0);
        modelResumo.setColumnIdentifiers(colunas.toArray());
        Map<Participante, BigDecimal> totaisIndividuais = p.calcularTotaisIndividuais();

        for (Participante part : p.getParticipantes()) {
            BigDecimal gasto = totaisIndividuais.getOrDefault(part, BigDecimal.ZERO);
            BigDecimal orc = part.getOrcamentoDisponivel();
            String status;

            BigDecimal ex = part.calcularExcesso(gasto);
            if (ex.signum() == 0) {
                status = "Dentro do limite";
            } else {
                status = String.format("EXCEDE em R$ %.2f", ex);
            }

            List<Object> linha = new ArrayList<>(List.of(part.getCodigo(), part.getNome(), String.format("%.2f", orc)));
            for (Despesa d : p.getDespesas()) {
                linha.add(String.format("%.2f", d.calcularRateio().getOrDefault(part, BigDecimal.ZERO)));
            }
            linha.add(String.format("%.2f", gasto));
            linha.add(status);
            modelResumo.addRow(linha.toArray());
        }

        lblCustoTotal.setText(String.format("Custo Total Geral do Passeio: R$ %.2f", p.calcularCustoTotalGeral()));
        txtResultadoSimulacao.setText("");
    }

    private void carregarCasoDeReferencia() throws ValidacaoException {
        controller.novoPlanejamento("Caso de Referência - Passeio da Turma");

        controller.cadastrarParticipante(1, "Ana", new BigDecimal("70.00"));
        controller.cadastrarParticipante(2, "Bruno", new BigDecimal("60.00"));
        controller.cadastrarParticipante(3, "Carla", new BigDecimal("70.00"));

        Participante ana = controller.getPlanejamento().getParticipantePorCodigo(1);
        Participante bruno = controller.getPlanejamento().getParticipantePorCodigo(2);
        Participante carla = controller.getPlanejamento().getParticipantePorCodigo(3);

        List<Participante> todos = List.of(ana, bruno, carla);
        List<Participante> anaCarla = List.of(ana, carla);

        controller.cadastrarDespesaFixa("D1", "Transporte", new BigDecimal("90.00"), todos, true, 0);
        controller.cadastrarDespesaPorPessoa("D2", "Almoço", new BigDecimal("20.00"), todos, true, 0);
        controller.cadastrarDespesaFixa("D3", "Atividade recreativa", new BigDecimal("30.00"), anaCarla, false, 3);
        controller.cadastrarDespesaPorPessoa("D4", "Sobremesa", new BigDecimal("10.00"), todos, false, 1);

        atualizarTabelas();
    }

    private void salvarArquivo() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();
                controller.salvarEmArquivo(f.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Planejamento salvo com sucesso em:\n" + f.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro de Persistência", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarArquivo() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();
                controller.carregarDeArquivo(f.getAbsolutePath());
                atualizarTabelas();
                JOptionPane.showMessageDialog(this, "Planejamento recarregado com sucesso!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar arquivo local: " + ex.getMessage(), "Falha de Leitura", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private DefaultTableModel criarModelo(Object[] colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}