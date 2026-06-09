package com.vetsentinel.ui;

import com.vetsentinel.repository.*;
import com.vetsentinel.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class VentanaLogin extends VetBaseFrame {

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  24);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_INPUT   = new Font("SansSerif", Font.PLAIN, 14);

    private JPanel root;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnThemeToggle;
    private final String modo;

    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;
    private final ParasitoRepository parasitoRepository;
    private final DiagnosticoRepository diagnosticoRepository;
    private final AuthenticationService authenticationService;
    private final RiskAssessmentService riskAssessmentService;
    private final Runnable onVolver;

    public VentanaLogin(String modo,
                        PropietarioRepository propietarioRepository,
                        MascotaRepository mascotaRepository,
                        ParasitoRepository parasitoRepository,
                        DiagnosticoRepository diagnosticoRepository,
                        AuthenticationService authenticationService,
                        RiskAssessmentService riskAssessmentService,
                        Runnable onVolver) {
        super("VetSentinel — Login " + (modo.equals("ESTADO") ? "Salud Pública" : "Clínica"));
        this.modo = modo;
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.parasitoRepository = parasitoRepository;
        this.diagnosticoRepository = diagnosticoRepository;
        this.authenticationService = authenticationService;
        this.riskAssessmentService = riskAssessmentService;
        this.onVolver = onVolver;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(bgDark);

        root.add(crearHeaderPanel(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);
        setSize(480, 650);
        setResizable(false);
        setLocationRelativeTo(null);
        realizarFadeIn();
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(bgDark);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        updaters.add(center::repaint);
        
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgCard);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 16, 16));
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        updaters.add(card::repaint);

        JLabel title = new JLabel("Bienvenido a VetSentinel");
        title.setFont(FONT_TITLE);
        updaters.add(() -> title.setForeground(accentTeal));
        title.setForeground(accentTeal);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(modo.equals("ESTADO") ? "Acceso exclusivo Estado / INS." : "Por favor, inicia sesión para continuar.");
        subtitle.setFont(FONT_LABEL);
        updaters.add(() -> subtitle.setForeground(textMuted));
        subtitle.setForeground(textMuted);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtUsername = createTextField("Usuario");
        txtPassword = createPasswordField("Contraseña");

        btnLogin = createButton("Iniciar Sesión", () -> accentTeal);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(300, 45));
        btnLogin.addActionListener(e -> intentarLogin());

        txtPassword.addActionListener(e -> intentarLogin());
        txtUsername.addActionListener(e -> intentarLogin());

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(35));
        card.add(fieldRow("Usuario", txtUsername));
        card.add(Box.createVerticalStrut(15));
        card.add(fieldRow("Contraseña", txtPassword));
        card.add(Box.createVerticalStrut(35));
        card.add(btnLogin);
        
        card.add(Box.createVerticalStrut(15));
        
        JButton btnVolver = new JButton("← Volver") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color baseBg = isDarkMode ? new Color(255, 255, 255, 30) : new Color(0, 0, 0, 20);
                if (getModel().isPressed()) {
                    baseBg = isDarkMode ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 40);
                } else if (getModel().isRollover()) {
                    baseBg = isDarkMode ? new Color(255, 255, 255, 50) : new Color(0, 0, 0, 10);
                }
                
                g2.setColor(baseBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                
                g2.setColor(isDarkMode ? new Color(226, 232, 240) : new Color(71, 85, 105));
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setPreferredSize(new Dimension(85, 28));
        btnVolver.setMinimumSize(new Dimension(85, 28));
        btnVolver.setMaximumSize(new Dimension(85, 28));
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> {
            if (onVolver != null) {
                onVolver.run();
            }
            dispose();
        });
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(btnVolver);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 30, 20, 30);
        
        center.add(card, gbc);

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

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(center, BorderLayout.CENTER);
        centerWrapper.add(bottomPanel, BorderLayout.SOUTH);

        return centerWrapper;
    }

    private void intentarLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa tu usuario y contraseña.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (authenticationService.login(user, pass)) {
                if (modo.equals("ESTADO") && !user.equalsIgnoreCase("estado")) {
                    JOptionPane.showMessageDialog(this, "Acceso denegado. Este módulo es exclusivo para el Estado.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (modo.equals("CLINICA") && user.equalsIgnoreCase("estado")) {
                    JOptionPane.showMessageDialog(this, "El usuario 'estado' no puede acceder a la vista clínica.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    if (modo.equals("ESTADO")) {
                        new VentanaEstado(propietarioRepository, mascotaRepository, parasitoRepository, diagnosticoRepository, authenticationService, riskAssessmentService, onVolver).setVisible(true);
                    } else {
                        new VentanaVeterinaria(propietarioRepository, mascotaRepository, parasitoRepository, diagnosticoRepository, authenticationService, riskAssessmentService, onVolver).setVisible(true);
                    }
                });
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Error", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Acceso Bloqueado", JOptionPane.WARNING_MESSAGE);
            txtPassword.setText("");
        }
    }

    private JPanel fieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 8));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(300, 65));
        
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        updaters.add(() -> lbl.setForeground(textPrimary));
        lbl.setForeground(textPrimary);
        
        row.add(lbl, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(textMuted);
                    g2.setFont(FONT_INPUT.deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        styleInput(pf);
        updaters.add(pf::repaint);
        return pf;
    }
}
