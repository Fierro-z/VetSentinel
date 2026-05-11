import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Supplier;

public class VentanaSelector extends VetBaseFrame {

    private JPanel root;
    private JButton btnThemeToggle;

    public VentanaSelector() {
        super("VetSentinel — Seleccionar Módulo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(bgDark);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);
        setSize(750, 520);
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
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        updaters.add(center::repaint);

        JPanel container = new JPanel(new GridLayout(1, 2, 30, 0));
        container.setOpaque(false);
        
        JButton btnClinica = createModuleButton("🏥", "Módulo Clínico", "Registro de diagnósticos", () -> accentBlue);
        btnClinica.addActionListener(e -> { this.dispose(); new VentanaLogin("CLINICA").setVisible(true); });

        JButton btnEstado = createModuleButton("📊", "Módulo Estado", "Vigilancia Epidemiológica", () -> accentTeal);
        btnEstado.addActionListener(e -> { this.dispose(); new VentanaLogin("ESTADO").setVisible(true); });

        container.add(btnClinica);
        container.add(btnEstado);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 50, 40, 50);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        center.add(container, gbc);

        return center;
    }

    private JButton createModuleButton(String icon, String title, String subtitle, Supplier<Color> colorSupp) {
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
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
                g2.drawString(icon, (getWidth() - g2.getFontMetrics().stringWidth(icon)) / 2, 110);
                g2.setColor(textPrimary);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                g2.drawString(title, (getWidth() - g2.getFontMetrics().stringWidth(title)) / 2, 170);
                g2.setColor(textMuted);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString(subtitle, (getWidth() - g2.getFontMetrics().stringWidth(subtitle)) / 2, 200);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(250, 260));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updaters.add(btn::repaint);
        return btn;
    }
}