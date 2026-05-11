import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;

public class VentanaLogin extends VetBaseFrame {

    // ── Tipografía ─────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  24);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_INPUT   = new Font("SansSerif", Font.PLAIN, 14);

    // ── Componentes de UI ──────────────────────────────────────────────────────
    private JPanel root;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnThemeToggle;

    public VentanaLogin() {
        super("VetSentinel — Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(bgDark);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);
        setSize(480, 650);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 160));
        
        JPanel imageContainer = new JPanel() {
            private Image banner;
            {
                try {
                    ImageIcon icon = new ImageIcon("img/bannerProyecto.png");
                    banner = icon.getImage();
                } catch (Exception e) { System.out.println("Imagen no encontrada."); }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (banner != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(banner, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(accentTeal);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        imageContainer.setLayout(new BorderLayout());
        imageContainer.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        togglePanel.setOpaque(false);
        
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

        togglePanel.add(btnThemeToggle);
        imageContainer.add(togglePanel, BorderLayout.NORTH);

        headerPanel.add(imageContainer, BorderLayout.CENTER);
        return headerPanel;
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

        JLabel subtitle = new JLabel("Por favor, inicia sesión para continuar.");
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

        // Permitir enter para iniciar sesión
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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 30, 20, 30);
        
        center.add(card, gbc);
        return center;
    }

    private void intentarLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa tu usuario y contraseña.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (VeterinariaDAO.validarUsuario(user, pass)) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                VentanaVeterinaria v = new VentanaVeterinaria();
                v.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Error", JOptionPane.ERROR_MESSAGE);
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
