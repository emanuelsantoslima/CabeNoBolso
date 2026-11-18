package view;

import controller.PlanejamentoController;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
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

    private JTextField txtCodPart, txtNomePart, txtOrcPart;
    private JTextField txtIdDesp, txtDescDesp, txtValorDesp, txtPrioridadeDesp;
    private JComboBox<String> cbTipoDesp;
    private JCheckBox chkEssencial;
    private JList<Participante> listParticipantesSelecao;

    public MainFrame() {
        this.controller = new PlanejamentoController();
        initComponents();
        carregarCasoDeReferencia();
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

        btnSalvar.addActionListener(e -> salvarArquivo());
        btnCarregar.addActionListener(e -> carregarArquivo());
        btnCarregarCasoRef.addActionListener(e -> {
            carregarCasoDeReferencia();
            JOptionPane.showMessageDialog(this, "Caso de referência carregado com sucesso!");
        });

        topPanel.add(btnSalvar);
        topPanel.add(btnCarregar);
        topPanel.add(btnCarregarCasoRef);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPanelParticipantes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
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

        btnAdicionar.addActionListener(e -> acaoSalvarParticipante());
        btnRemover.addActionListener(e -> acaoRemoverParticipante());

        form.add(btnAdicionar);
        form.add(btnRemover);

        modelParticipantes = new DefaultTableModel(new Object[]{"Código", "Nome", "Orçamento (R$)"}, 0);
        tableParticipantes = new JTable(modelParticipantes);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableParticipantes), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPanelDespesas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(7, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Cadastrar Nova Despesa"));

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
        form.add(new JLabel("Prioridade Opcional (1 a 10):"));
        form.add(txtPrioridadeDesp);

        JButton btnAddDesp = new JButton("Adicionar Despesa");
        JButton btnRemDesp = new JButton("Excluir Despesa Selecionada");

        btnAddDesp.addActionListener(e -> acaoSalvarDespesa());
        btnRemDesp.addActionListener(e -> acaoRemoverDespesa());

        form.add(btnAddDesp);
        form.add(btnRemDesp);

        listParticipantesSelecao = new JList<>();
        listParticipantesSelecao.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollList = new JScrollPane(listParticipantesSelecao);
        scrollList.setBorder(BorderFactory.createTitledBorder("Selecione os Participantes da Despesa"));

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(form);
        centerPanel.add(scrollList);

        modelDespesas = new DefaultTableModel(new Object[]{"ID", "Descrição", "Tipo", "Valor (R$)", "Essencial", "Prioridade", "Participantes"}, 0);
        tableDespesas = new JTable(modelDespesas);

        panel.add(centerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableDespesas), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPanelResumo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modelResumo = new DefaultTableModel(new Object[]{"Código", "Nome", "Orçamento (R$)", "Total Individual (R$)", "Situação / Excesso"}, 0);
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

        JTextArea txtResultadoSimulacao = new JTextArea();
        txtResultadoSimulacao.setEditable(false);
        txtResultadoSimulacao.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JButton btnExecutar = new JButton("Executar Otimização de Opcionais");
        btnExecutar.addActionListener(e -> {
            PlanejamentoController.ResultadoSimulacao res = controller.simularAlternativasOtimas();
            StringBuilder sb = new StringBuilder();
            sb.append("=== RESULTADO DA SIMULAÇÃO DE ALTERNATIVAS ===\n\n");

            if (!res.viavel) {
                sb.append(" STATUS: INVIÁVEL\n");
                sb.append(" Mensagem: ").append(res.mensagemErro).append("\n");
            } else {
                sb.append(" STATUS: SOLUÇÃO OTIMIZADA ENCONTRADA!\n");
                sb.append(" Soma Máxima de Prioridades Opcionais Mantidas: ").append(res.somaPrioridades).append("\n");
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
                for (Map.Entry<Participante, Double> entry : res.custosPorPessoa.entrySet()) {
                    Participante p = entry.getKey();
                    double novoCusto = entry.getValue();
                    sb.append(String.format(" %-15s | Orçamento: R$ %6.2f | Novo Custo: R$ %6.2f | Folga: R$ %6.2f\n",
                            p.getNome(), p.getOrcamentoDisponivel(), novoCusto, (p.getOrcamentoDisponivel() - novoCusto)));
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
            double orc = Double.parseDouble(txtOrcPart.getText().trim().replace(",", "."));

            if (controller.getPlanejamento().getParticipantePorCodigo(cod) != null) {
                controller.atualizarParticipante(cod, nome, orc);
            } else {
                controller.cadastrarParticipante(cod, nome, orc);
            }

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
            double val = Double.parseDouble(txtValorDesp.getText().trim().replace(",", "."));
            String tipo = (String) cbTipoDesp.getSelectedItem();
            boolean ess = chkEssencial.isSelected();
            int prio = Integer.parseInt(txtPrioridadeDesp.getText().trim());

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

        modelResumo.setRowCount(0);
        Map<Participante, Double> totaisIndividuais = p.calcularTotaisIndividuais();

        for (Participante part : p.getParticipantes()) {
            double gasto = totaisIndividuais.getOrDefault(part, 0.0);
            double orc = part.getOrcamentoDisponivel();
            String status;

            if (gasto <= orc) {
                status = "Dentro do limite";
            } else {
                double ex = gasto - orc;
                status = String.format("EXCEDE em R$ %.2f", ex);
            }

            modelResumo.addRow(new Object[]{
                    part.getCodigo(), part.getNome(), String.format("%.2f", orc),
                    String.format("%.2f", gasto), status
            });
        }

        lblCustoTotal.setText(String.format("Custo Total Geral do Passeio: R$ %.2f", p.calcularCustoTotalGeral()));
    }

    private void carregarCasoDeReferencia() {
        controller.novoPlanejamento("Caso de Referência - Passeio da Turma");

        controller.cadastrarParticipante(1, "Ana", 70.0);
        controller.cadastrarParticipante(2, "Bruno", 60.0);
        controller.cadastrarParticipante(3, "Carla", 70.0);

        Participante ana = controller.getPlanejamento().getParticipantePorCodigo(1);
        Participante bruno = controller.getPlanejamento().getParticipantePorCodigo(2);
        Participante carla = controller.getPlanejamento().getParticipantePorCodigo(3);

        List<Participante> todos = List.of(ana, bruno, carla);
        List<Participante> anaCarla = List.of(ana, carla);

        controller.cadastrarDespesaFixa("D1", "Transporte", 90.0, todos, true, 0);
        controller.cadastrarDespesaPorPessoa("D2", "Almoço", 20.0, todos, true, 0);
        controller.cadastrarDespesaFixa("D3", "Atividade recreativa", 30.0, anaCarla, false, 3);
        controller.cadastrarDespesaPorPessoa("D4", "Sobremesa", 10.0, todos, false, 1);

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
}