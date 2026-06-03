package com.vetsentinel.ui;

import com.vetsentinel.repository.*;
import com.vetsentinel.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Supplier;

public class VentanaSelector extends VetBaseFrame {

    private JPanel root;
    private JButton btnThemeToggle;

    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;
    private final ParasitoRepository parasitoRepository;
    private final DiagnosticoRepository diagnosticoRepository;
    private final AuthenticationService authenticationService;
    private final RiskAssessmentService riskAssessmentService;

    public VentanaSelector(PropietarioRepository propietarioRepository,
                           MascotaRepository mascotaRepository,
                           ParasitoRepository parasitoRepository,
                           DiagnosticoRepository diagnosticoRepository,
                           AuthenticationService authenticationService,
                           RiskAssessmentService riskAssessmentService) {
        this(propietarioRepository, mascotaRepository, parasitoRepository, diagnosticoRepository, authenticationService, riskAssessmentService, true);
    }

    public VentanaSelector(PropietarioRepository propietarioRepository,
                           MascotaRepository mascotaRepository,
                           ParasitoRepository parasitoRepository,
                           DiagnosticoRepository diagnosticoRepository,
                           AuthenticationService authenticationService,
                           RiskAssessmentService riskAssessmentService,
                           boolean conAnimacion) {
        super("VetSentinel — Seleccionar Módulo");
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.parasitoRepository = parasitoRepository;
        this.diagnosticoRepository = diagnosticoRepository;
        this.authenticationService = authenticationService;
        this.riskAssessmentService = riskAssessmentService;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(bgDark);

        root.add(crearHeaderPanel(), BorderLayout.NORTH); // Utilizar el método de VetBaseFrame
        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);
        setSize(1100, 700);
        setResizable(false);
        setLocationRelativeTo(null);

        if (conAnimacion) {
            realizarFadeIn();
        }
    }

    private JPanel buildCenter() {
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);

        // Header Text Panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(10, 50, 10, 50));

        JLabel lblWelcome = new JLabel("¡Bienvenido!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        updaters.add(() -> lblWelcome.setForeground(textPrimary));
        lblWelcome.setForeground(textPrimary);
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblWelcome.setBorder(new EmptyBorder(15, 0, 5, 0));

        JLabel lblInstruction = new JLabel("Seleccione el perfil que mejor describa su función");
        lblInstruction.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        updaters.add(() -> lblInstruction.setForeground(textMuted));
        lblInstruction.setForeground(textMuted);
        lblInstruction.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(lblWelcome);
        textPanel.add(lblInstruction);

        centerWrapper.add(textPanel, BorderLayout.NORTH);

        // Cards Panel
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        updaters.add(center::repaint);

        JPanel container = new JPanel(new GridLayout(1, 3, 20, 0));
        container.setOpaque(false);
        
        Runnable onVolver = () -> {
            // No es necesario hacer nada aquí, ya que la ventana de destino se encargará de cerrar la actual.
            // La ventana de destino (VentanaSelector) se crea en el ActionListener del botón de volver.
        };

        JButton btnClinica = createModuleCard("👨‍⚕️", "Veterinario", "Registro clínico integral,\ndiagnósticos asistidos por IA\ny gestión completa de pacientes.", () -> accentBlue);
        btnClinica.addActionListener(e -> { 
            this.dispose(); 
            new VentanaLogin("CLINICA", propietarioRepository, mascotaRepository, parasitoRepository, diagnosticoRepository, authenticationService, riskAssessmentService, () -> {
                new VentanaSelector(propietarioRepository, mascotaRepository, parasitoRepository, diagnosticoRepository, authenticationService, riskAssessmentService, false).setVisible(true);
            }).setVisible(true); 
        });

        JButton btnCiudadano = createModuleCard("👨‍👩‍👧", "Ciudadano", "Consulta rápida de alertas\nsanitarias regionales y estado\nepidemiológico de tu comunidad.", () -> okGreen);
        btnCiudadano.addActionListener(e -> { 
            this.dispose(); 
            new VentanaCiudadana(() -> {
                new VentanaSelector(propietarioRepository, mascotaRepository, parasitoRepository, diagnosticoRepository, authenticationService, riskAssessmentService, false).setVisible(true);
            }).setVisible(true); 
        });

        JButton btnEstado = createModuleCard("🏛️", "Entidad Estatal", "Análisis de salud pública,\nmapas de calor biogeográficos\ny reportes de riesgo en tiempo real.", () -> accentTeal);
        btnEstado.addActionListener(e -> { 
            this.dispose(); 
            new VentanaLogin("ESTADO", propietarioRepository, mascotaRepository, parasitoRepository, diagnosticoRepository, authenticationService, riskAssessmentService, () -> {
                new VentanaSelector(propietarioRepository, mascotaRepository, parasitoRepository, diagnosticoRepository, authenticationService, riskAssessmentService, false).setVisible(true);
            }).setVisible(true);
        });

        container.add(btnClinica);
        container.add(btnCiudadano);
        container.add(btnEstado);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 50, 30, 50);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        center.add(container, gbc);

        centerWrapper.add(center, BorderLayout.CENTER);

        // Theme toggle button en la parte inferior derecha
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        bottomPanel.setOpaque(false);
        btnThemeToggle = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDarkMode ? bgCard : new Color(220, 228, 235)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                g2.setColor(textPrimary);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String text = isDarkMode ? "☀️" : "🌙";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        updaters.add(btnThemeToggle::repaint);
        btnThemeToggle.setFocusPainted(false);
        btnThemeToggle.setContentAreaFilled(false);
        btnThemeToggle.setBorderPainted(false);
        btnThemeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThemeToggle.setPreferredSize(new Dimension(36, 36));
        btnThemeToggle.addActionListener(e -> alternarTema());
        bottomPanel.add(btnThemeToggle);
        
        centerWrapper.add(bottomPanel, BorderLayout.SOUTH);

        return centerWrapper;
    }

    private JButton createModuleCard(String icon, String title, String subtitle, Supplier<Color> colorSupp) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color baseColor = colorSupp.get();
                Color bg = getModel().isRollover() ? new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 30) : bgCard;
                if (getModel().isPressed()) bg = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 60);
                
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                
                g2.setColor(getModel().isRollover() ? baseColor : borderColor);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                
                // Icon
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
                g2.drawString(icon, (getWidth() - g2.getFontMetrics().stringWidth(icon)) / 2, 80);
                
                // Title
                g2.setColor(textPrimary);
                g2.setFont(new Font("SansSerif", Font.BOLD, 20));
                g2.drawString(title, (getWidth() - g2.getFontMetrics().stringWidth(title)) / 2, 130);
                
                // Subtitle (Multiline)
                g2.setColor(textMuted);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                String[] lines = subtitle.split("\n");
                int startY = 170;
                for (String line : lines) {
                    g2.drawString(line, (getWidth() - g2.getFontMetrics().stringWidth(line)) / 2, startY);
                    startY += g2.getFontMetrics().getHeight() + 2;
                }
                
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(260, 280));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updaters.add(btn::repaint);
        return btn;
    }
}
