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

        JLabel title = makeLabel("Cepas y Alertas por Ubicación", FONT_SUBTITLE, () -> accentTeal);
        panelDatos.add(title, BorderLayout.NORTH);

        actualizarTablaCepas();

        return panelDatos;
    }

    private void actualizarTablaCepas() {
        if (panelDatos.getComponentCount() > 1) {
            panelDatos.remove(1);
        }
        
        List<String[]> cepas = diagnosticoRepository.obtenerCepasPorUbicacion();
        String[] cols = {"Departamento", "Cepa / Parásito", "Casos", "Riesgo Max"};
        Object[][] data = cepas.toArray(new Object[0][]);

        JTable table = new JTable(data, cols);
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
