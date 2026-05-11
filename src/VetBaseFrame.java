import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class VetBaseFrame extends JFrame {

    protected boolean isDarkMode = false;
    protected List<Runnable> updaters = new ArrayList<>();

    // ── Paleta de colores Dinámica ─────────────────────────────────────────────
    protected Color bgDark;
    protected Color bgPanel;
    protected Color bgCard;
    protected Color bgInput;
    protected Color accentTeal;
    protected Color accentBlue;
    protected Color dangerRed;
    protected Color warnOrange;
    protected Color okGreen;
    protected Color textPrimary;
    protected Color textMuted;
    protected Color borderColor;

    // ── Tipografía (base) ─────────────────────────────────────────────────────────────
    protected static final Font FONT_LABEL   = new Font("SansSerif", Font.PLAIN, 12);
    protected static final Font FONT_INPUT   = new Font("SansSerif", Font.PLAIN, 13);
    protected static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  12);

    public VetBaseFrame(String title) {
        super(title);
        aplicarColores(isDarkMode);
    }

    protected void aplicarColores(boolean oscuro) {
        if (oscuro) {
            bgDark       = new Color(13,  17,  23);
            bgPanel      = new Color(22,  30,  40);
            bgCard       = new Color(30,  41,  55);
            bgInput      = new Color(18,  24,  33);
            accentTeal   = new Color(20, 184, 166);
            accentBlue   = new Color(56, 139, 253);
            dangerRed    = new Color(220,  53,  69);
            warnOrange   = new Color(255, 152,   0);
            okGreen      = new Color( 40, 167,  69);
            textPrimary  = new Color(245, 250, 255);
            textMuted    = new Color(175, 195, 215);
            borderColor  = new Color( 48,  62,  78);
        } else {
            bgDark       = new Color(244, 247, 249);
            bgPanel      = new Color(255, 255, 255);
            bgCard       = new Color(255, 255, 255);
            bgInput      = new Color(250, 250, 250);
            accentTeal   = new Color(0,   168, 181);
            accentBlue   = new Color(10,  100, 220);
            dangerRed    = new Color(224, 122,  95);
            warnOrange   = new Color(245, 130,   0);
            okGreen      = new Color( 30, 140,  50);
            textPrimary  = new Color(0,   61,  91);
            textMuted    = new Color(85,  102, 119);
            borderColor  = new Color(221, 228, 233);
        }
    }

    protected void alternarTema() {
        isDarkMode = !isDarkMode;
        aplicarColores(isDarkMode);
        
        getContentPane().setBackground(bgDark);
        
        for (Runnable r : updaters) {
            r.run();
        }
        
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    protected JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(textMuted);
                    g2.setFont(FONT_INPUT.deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, getHeight()/2 + 5);
                    g2.dispose();
                }
            }
        };
        styleInput(tf);
        updaters.add(tf::repaint);
        return tf;
    }

    protected void styleInput(JTextField tf) {
        Runnable updater = () -> {
            tf.setBackground(bgInput);
            tf.setForeground(textPrimary);
            tf.setCaretColor(accentTeal);
            tf.setFont(FONT_INPUT);
            if (!tf.isFocusOwner()) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor),
                    new EmptyBorder(8, 12, 8, 12)));
            } else {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentTeal),
                    new EmptyBorder(8, 12, 8, 12)));
            }
        };
        updater.run();
        updaters.add(updater);
        
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { updater.run(); tf.repaint(); }
            @Override public void focusLost(FocusEvent e) { updater.run(); tf.repaint(); }
        });
    }

    protected JButton createButton(String text, Supplier<Color> bgSupplier) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = bgSupplier.get();
                Color fill = getModel().isPressed()
                        ? bg.darker()
                        : getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_BTN);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updaters.add(btn::repaint);
        return btn;
    }

    protected JLabel makeLabel(String text, Font font, Supplier<Color> colorSupplier) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(colorSupplier.get());
        updaters.add(() -> l.setForeground(colorSupplier.get()));
        return l;
    }
}
