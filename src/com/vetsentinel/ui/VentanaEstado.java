package com.vetsentinel.ui;

import com.vetsentinel.repository.*;
import com.vetsentinel.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

public class VentanaEstado extends VetBaseFrame {

    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 14);

    private JPanel root;
    private JButton btnThemeToggle;
    private JButton btnActualizar;
    private PanelMapaColombia panelMapaVector;
    private JPanel panelDatos;
    private String selectedDepartamento = null;

    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;
    private final ParasitoRepository parasitoRepository;
    private final DiagnosticoRepository diagnosticoRepository;
    private final AuthenticationService authenticationService;
    private final RiskAssessmentService riskAssessmentService;

    public VentanaEstado(PropietarioRepository propietarioRepository,
                         MascotaRepository mascotaRepository,
                         ParasitoRepository parasitoRepository,
                         DiagnosticoRepository diagnosticoRepository,
                         AuthenticationService authenticationService,
                         RiskAssessmentService riskAssessmentService) {
        super("VetSentinel — Módulo de Salud Pública (Estado)");
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.parasitoRepository = parasitoRepository;
        this.diagnosticoRepository = diagnosticoRepository;
        this.authenticationService = authenticationService;
        this.riskAssessmentService = riskAssessmentService;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(bgDark);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);
        setPreferredSize(new Dimension(1200, 800));
        setMinimumSize(new Dimension(1000, 700));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(getWidth(), 120));
        headerPanel.setBackground(bgPanel);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = makeLabel("Panel de Vigilancia Epidemiológica Nacional", FONT_TITLE, () -> textPrimary);
        JLabel subtitle = makeLabel("Instituto Nacional de Salud - VetSentinel", FONT_SUBTITLE, () -> textMuted);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 5));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);

        JPanel leftPanel = new JPanel(new BorderLayout(15, 0));
        leftPanel.setOpaque(false);

        JButton btnVolverTop = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDarkMode ? new Color(30, 41, 55, 220) : new Color(255, 255, 255, 220));
                if (getModel().isRollover()) g2.setColor(isDarkMode ? new Color(45, 60, 80, 255) : new Color(235, 240, 245, 255));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(textPrimary);
                g2.setFont(FONT_BTN);
                FontMetrics fm = g2.getFontMetrics();
                String text = "⬅ Volver";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        updaters.add(btnVolverTop::repaint);
        btnVolverTop.setPreferredSize(new Dimension(85, 30));
        btnVolverTop.setContentAreaFilled(false);
        btnVolverTop.setBorderPainted(false);
        btnVolverTop.setFocusPainted(false);
        btnVolverTop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolverTop.addActionListener(e -> { 
            this.dispose(); 
            new VentanaSelector(propietarioRepository, mascotaRepository, parasitoRepository, diagnosticoRepository, authenticationService, riskAssessmentService).setVisible(true); 
        });

        JPanel btnWrapper = new JPanel(new GridBagLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnVolverTop);

        leftPanel.add(btnWrapper, BorderLayout.WEST);
        leftPanel.add(titles, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        btnActualizar = createButton("🔄 Actualizar", () -> accentBlue);
        btnActualizar.setPreferredSize(new Dimension(120, 36));
        btnActualizar.addActionListener(e -> actualizarDatos());

        btnThemeToggle = createButton(isDarkMode ? "☀️ Claro" : "🌙 Oscuro", () -> textMuted);
        btnThemeToggle.setPreferredSize(new Dimension(100, 36));
        btnThemeToggle.addActionListener(e -> {
            alternarTema();
            btnThemeToggle.setText(isDarkMode ? "☀️ Claro" : "🌙 Oscuro");
            actualizarDatos();
        });

        rightPanel.add(btnActualizar);
        rightPanel.add(btnThemeToggle);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        updaters.add(center::repaint);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.55;
        gbc.insets = new Insets(0, 0, 0, 10);
        center.add(buildMapaPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.45;
        gbc.insets = new Insets(0, 10, 0, 0);
        center.add(buildDatosPanel(), gbc);

        return center;
    }

    private JPanel buildMapaPanel() {
        JPanel container = createCard();
        container.setLayout(new BorderLayout(0, 15));

        JLabel title = makeLabel("Mapa de Riesgo Zoonótico por Departamento", FONT_SUBTITLE, () -> accentTeal);
        container.add(title, BorderLayout.NORTH);

        panelMapaVector = new PanelMapaColombia();
        panelMapaVector.setSelectionListener(depto -> {
            selectedDepartamento = depto;
            actualizarTablaCepas();
        });
        container.add(panelMapaVector, BorderLayout.CENTER);

        actualizarMapaVector();

        return container;
    }

    private void actualizarMapaVector() {
        if (panelMapaVector != null) {
            Map<String, String> riesgos = diagnosticoRepository.obtenerRiesgoPorDepartamento();
            panelMapaVector.actualizarRiesgos(riesgos);
        }
    }

    private JPanel buildDatosPanel() {
        panelDatos = createCard();
        panelDatos.setLayout(new BorderLayout(0, 15));

        actualizarTablaCepas();

        return panelDatos;
    }

    private JPanel buildDatosHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = makeLabel(
            selectedDepartamento == null ? "Cepas y Alertas por Ubicación" : "Casos en " + selectedDepartamento,
            FONT_SUBTITLE,
            () -> accentTeal
        );
        header.add(title, BorderLayout.WEST);

        if (selectedDepartamento != null) {
            JButton btnVolver = new JButton("⬅ Volver") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isDarkMode ? new Color(30, 41, 55, 220) : new Color(255, 255, 255, 220));
                    if (getModel().isRollover()) g2.setColor(isDarkMode ? new Color(45, 60, 80, 255) : new Color(235, 240, 245, 255));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    g2.setColor(borderColor);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    g2.setColor(textPrimary);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(getText(), x, y);
                    g2.dispose();
                }
            };
            btnVolver.setPreferredSize(new Dimension(80, 26));
            btnVolver.setContentAreaFilled(false);
            btnVolver.setBorderPainted(false);
            btnVolver.setFocusPainted(false);
            btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnVolver.addActionListener(e -> {
                selectedDepartamento = null;
                actualizarTablaCepas();
            });
            header.add(btnVolver, BorderLayout.EAST);
        }

        return header;
    }

    private void actualizarTablaCepas() {
        panelDatos.removeAll();
        panelDatos.add(buildDatosHeader(), BorderLayout.NORTH);

        if (selectedDepartamento == null) {
            List<String[]> cepas = diagnosticoRepository.obtenerCepasPorUbicacion();
            String[] cols = {"Departamento", "Cepa / Parásito", "Casos", "Riesgo Max"};
            Object[][] data = cepas.toArray(new Object[0][]);

            JTable table = new JTable(data, cols) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table.setBackground(bgCard);
            table.setForeground(textPrimary);
            table.setFont(FONT_INPUT);
            table.setRowHeight(30);
            table.getTableHeader().setBackground(bgDark);
            table.getTableHeader().setForeground(accentTeal);
            
            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(bgCard);
            scroll.setBorder(BorderFactory.createLineBorder(borderColor));
            panelDatos.add(scroll, BorderLayout.CENTER);
        } else {
            Object[][] data = diagnosticoRepository.obtenerHistorialPorDepartamento(selectedDepartamento);
            if (data.length == 0) {
                JPanel emptyPanel = new JPanel(new GridBagLayout());
                emptyPanel.setOpaque(false);
                JLabel emptyLabel = makeLabel("Sin datos", FONT_SUBTITLE.deriveFont(Font.ITALIC), () -> textMuted);
                emptyPanel.add(emptyLabel);
                panelDatos.add(emptyPanel, BorderLayout.CENTER);
            } else {
                String[] cols = {"Fecha", "Propietario", "Dirección / Municipio", "Mascota", "Especie", "Parásito", "Riesgo"};
                JTable table = new JTable(data, cols) {
                    @Override public boolean isCellEditable(int r, int c) { return false; }
                    @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                        Component c = super.prepareRenderer(r, row, col);
                        c.setBackground(row % 2 == 0 ? bgCard : bgPanel);
                        c.setForeground(textPrimary);
                        
                        if (col == 6) {
                            String v = getValueAt(row, col).toString();
                            if ("EMERGENCIA CRÍTICA".equals(v) || "EMERGENCIA CRITICA".equals(v) || "CRITICO".equals(v) || "CRÍTICO".equals(v)) c.setForeground(dangerRed);
                            else if ("ALTO".equals(v) || "MODERADO".equals(v)) c.setForeground(warnOrange);
                            else if ("MEDIO".equals(v)) c.setForeground(new Color(230, 180, 50));
                            else if ("BAJO".equals(v)) c.setForeground(okGreen);
                        }
                        return c;
                    }
                };
                table.setBackground(bgCard);
                table.setForeground(textPrimary);
                table.setFont(FONT_INPUT);
                table.setRowHeight(32);
                table.getTableHeader().setBackground(bgDark);
                table.getTableHeader().setForeground(accentTeal);
                
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                table.getColumnModel().getColumn(0).setPreferredWidth(95);  // Fecha
                table.getColumnModel().getColumn(1).setPreferredWidth(140); // Propietario
                table.getColumnModel().getColumn(2).setPreferredWidth(180); // Dirección / Municipio
                table.getColumnModel().getColumn(3).setPreferredWidth(90);  // Mascota
                table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Especie
                table.getColumnModel().getColumn(5).setPreferredWidth(120); // Parásito
                table.getColumnModel().getColumn(6).setPreferredWidth(130); // Riesgo

                JScrollPane scroll = new JScrollPane(table);
                scroll.getViewport().setBackground(bgCard);
                scroll.setBorder(BorderFactory.createLineBorder(borderColor));
                scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                panelDatos.add(scroll, BorderLayout.CENTER);
            }
        }

        panelDatos.revalidate();
        panelDatos.repaint();
    }

    private void actualizarDatos() {
        actualizarMapaVector();
        actualizarTablaCepas();
    }

    private JPanel createCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgCard);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2.setColor(borderColor);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        updaters.add(p::repaint);
        return p;
    }
}
