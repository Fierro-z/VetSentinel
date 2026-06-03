package com.vetsentinel.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Supplier;

public class VentanaCiudadana extends VetBaseFrame {

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  20);
    private static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);

    private JPanel root;
    private JButton btnThemeToggle;
    private JTextField txtAltitud;
    private JComboBox<String> cbDepartamento;
    private JComboBox<String> cbArea;
    private JCheckBox chkGatos;
    private JCheckBox chkGestantes;
    private JCheckBox chkNinos;
    private JButton btnEvaluar;

    private JPanel alertPanel;
    private JPanel alertCenterWrapper;
    private JLabel alertIconLabel;
    private JLabel alertNivelLabel;
    private JTextArea alertTextArea;
    private JLabel alertMascotaLabel;

    private final VentanaSelector selector;

    private static final String[] DEPARTAMENTOS = {
        "Amazonas", "Antioquia", "Arauca", "Atlántico", "Bolívar", "Boyacá", "Caldas", "Caquetá", "Casanare", "Cauca", 
        "Cesar", "Chocó", "Córdoba", "Cundinamarca", "Guainía", "Guaviare", "Huila", "La Guajira", "Magdalena", "Meta", 
        "Nariño", "Norte de Santander", "Putumayo", "Quindío", "Risaralda", "San Andrés y Providencia", "Santander", 
        "Sucre", "Tolima", "Valle del Cauca", "Vaupés", "Vichada"
    };

    public VentanaCiudadana(VentanaSelector selector) {
        super("BioGeo Risk — Módulo Ciudadano Preventivo");
        this.selector = selector;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(bgDark);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);
        setPreferredSize(new Dimension(1100, 750));
        setMinimumSize(new Dimension(950, 700));
        pack();
        setLocationRelativeTo(null);

        resetAlertPanel();
    }

    private JPanel buildHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(getWidth(), 180));
        
        JPanel imageContainer = new JPanel() {
            private Image banner;
            {
                try {
                    ImageIcon icon = new ImageIcon("resources/img/bannerProyecto.png");
                    banner = icon.getImage();
                } catch (Exception e) { System.out.println("Imagen no encontrada."); }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(bgDark);
                g.fillRect(0, 0, getWidth(), getHeight());
                if (banner != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    int iw = banner.getWidth(this);
                    int ih = banner.getHeight(this);
                    int pw = getWidth();
                    int ph = getHeight();
                    if (iw > 0 && ih > 0) {
                        double scale = Math.min((double) pw / iw, (double) ph / ih);
                        int nw = (int) (iw * scale);
                        int nh = (int) (ih * scale);
                        int x = (pw - nw) / 2;
                        int y = (ph - nh) / 2;
                        g2.drawImage(banner, x, y, nw, nh, this);
                    } else {
                        g2.drawImage(banner, 0, 0, pw, ph, this);
                    }
                }
            }
        };
        imageContainer.setLayout(new BorderLayout());
        imageContainer.setBorder(new EmptyBorder(15, 25, 15, 25));

        JPanel topLeftRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topLeftRow.setOpaque(false);

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
            selector.setVisible(true); 
        });

        topLeftRow.add(btnVolverTop);
        imageContainer.add(topLeftRow, BorderLayout.NORTH);

        headerPanel.add(imageContainer, BorderLayout.CENTER);
        return headerPanel;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(bgDark);
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        updaters.add(center::repaint);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.weightx = 0.65;
        gbc.weighty = 1.0;
        gbc.gridx = 0; gbc.gridy = 0;
        
        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.setOpaque(false);
        leftContainer.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JPanel formPanel = buildFormPanel();
        leftContainer.add(formPanel, BorderLayout.CENTER);

        center.add(leftContainer, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 0.35;
        
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);
        
        JPanel topRightRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        topRightRow.setOpaque(false);

        JPanel badge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badge.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        updaters.add(() -> dot.setForeground(okGreen));
        JLabel status = makeLabel("Ciudadano protegido", FONT_LABEL, () -> textMuted);
        badge.add(dot);
        badge.add(status);

        btnThemeToggle = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDarkMode ? bgCard : new Color(220, 228, 235)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
                
                g2.setColor(textPrimary);
                g2.setFont(FONT_BTN);
                FontMetrics fm = g2.getFontMetrics();
                String text = isDarkMode ? "☀️ Modo Claro" : "🌙 Modo Oscuro";
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
        btnThemeToggle.setPreferredSize(new Dimension(135, 36));
        btnThemeToggle.addActionListener(e -> alternarTema());

        topRightRow.add(badge);
        topRightRow.add(btnThemeToggle);

        rightContainer.add(topRightRow, BorderLayout.NORTH);
        
        JPanel paddingAlert = new JPanel(new BorderLayout());
        paddingAlert.setOpaque(false);
        paddingAlert.setBorder(new EmptyBorder(14, 0, 0, 0)); 
        paddingAlert.add(buildAlertPanel(), BorderLayout.CENTER);
        
        rightContainer.add(paddingAlert, BorderLayout.CENTER);

        center.add(rightContainer, gbc);

        return center;
    }

    private JPanel buildFormPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());

        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setOpaque(false);
        formContent.setBorder(new EmptyBorder(0, 0, 0, 8));

        formContent.add(sectionLabel("BIOGEO RISK - AUTOEVALUACIÓN DE ENTORNO"));
        formContent.add(Box.createVerticalStrut(15));

        formContent.add(sectionLabel("UBICACIÓN GEOGRÁFICA"));
        formContent.add(Box.createVerticalStrut(5));
        formContent.add(fieldRow("Departamento de Residencia", cbDepartamento = createCombo(DEPARTAMENTOS)));
        formContent.add(Box.createVerticalStrut(5));
        formContent.add(fieldRow("Altitud del Hogar (msnm)", txtAltitud = createTextField("Ej: 1100")));
        txtAltitud.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) && e.getKeyChar() != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });
        formContent.add(Box.createVerticalStrut(5));
        formContent.add(fieldRow("Tipo de Entorno / Zona", cbArea = createCombo(new String[]{"Urbana", "Rural"})));

        formContent.add(Box.createVerticalStrut(15));
        formContent.add(sectionLabel("CONDICIONES DE CONVIVENCIA Y HOGAR"));
        formContent.add(Box.createVerticalStrut(5));

        JPanel riskRow = new JPanel(new GridLayout(1, 3, 6, 0));
        riskRow.setOpaque(false);
        riskRow.setAlignmentX(Component.LEFT_ALIGNMENT); 

        chkGatos     = createCheckBox();
        chkGestantes = createCheckBox();
        chkNinos     = createCheckBox();

        riskRow.add(riskCard("🐱", "¿Tiene Gatos?", chkGatos));
        riskRow.add(riskCard("🤰", "¿Hay Gestantes?", chkGestantes));
        riskRow.add(riskCard("👶", "¿Hay Niños < 10?", chkNinos));
        formContent.add(riskRow);

        formContent.add(Box.createVerticalStrut(20));
        
        btnEvaluar = createButton("Evaluar Riesgos del Entorno", () -> accentTeal);
        btnEvaluar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEvaluar.addActionListener(e -> evaluarRiesgoEntorno());
        formContent.add(btnEvaluar);

        JPanel formWrapper = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                if (getParent() instanceof JViewport) d.width = getParent().getWidth();
                return d;
            }
        };
        formWrapper.setOpaque(false);
        formWrapper.add(formContent, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(formWrapper);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scroll);

        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildAlertPanel() {
        alertPanel = createCard();
        alertPanel.setLayout(new BorderLayout(0, 12));

        JPanel alertHeader = new JPanel(new BorderLayout(10, 0));
        alertHeader.setOpaque(false);

        alertIconLabel  = makeLabel("🔍", new Font("Segoe UI Emoji", Font.PLAIN, 32), () -> textMuted);
        alertNivelLabel = makeLabel("EN ESPERA", FONT_SECTION, () -> textMuted);
        alertMascotaLabel = makeLabel("Haz clic en Evaluar para ver el reporte", FONT_LABEL, () -> textMuted);

        JPanel alertTitles = new JPanel(new GridLayout(2, 1, 0, 4));
        alertTitles.setOpaque(false);
        alertTitles.add(alertNivelLabel);
        alertTitles.add(alertMascotaLabel);

        alertHeader.add(alertIconLabel,  BorderLayout.WEST);
        alertHeader.add(alertTitles,     BorderLayout.CENTER);

        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(borderColor);
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        updaters.add(sep::repaint);

        alertTextArea = new JTextArea();
        alertTextArea.setEditable(false);
        alertTextArea.setOpaque(false);
        alertTextArea.setFont(FONT_MONO);
        alertTextArea.setForeground(textMuted);
        alertTextArea.setLineWrap(true);
        alertTextArea.setWrapStyleWord(true);
        alertTextArea.setBorder(null);
        alertTextArea.setText("El resultado del análisis preventivo ambiental aparecerá aquí una vez que evalúes tu zona.");

        JScrollPane alertScroll = new JScrollPane(alertTextArea);
        alertScroll.setOpaque(false);
        alertScroll.getViewport().setOpaque(false);
        alertScroll.setBorder(null);
        styleScrollBar(alertScroll);

        JPanel emptyStatePanel = new JPanel(new GridBagLayout());
        emptyStatePanel.setOpaque(false);
        JLabel emptyIcon = new JLabel("🌍");
        emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        JLabel emptyText = makeLabel("A la espera del análisis...", FONT_LABEL.deriveFont(Font.ITALIC), () -> textMuted);
        GridBagConstraints ec = new GridBagConstraints();
        ec.gridx = 0; ec.gridy = 0;
        emptyStatePanel.add(emptyIcon, ec);
        ec.gridy = 1; ec.insets = new Insets(12, 0, 0, 0);
        emptyStatePanel.add(emptyText, ec);

        alertCenterWrapper = new JPanel(new CardLayout());
        alertCenterWrapper.setOpaque(false);
        alertCenterWrapper.add(emptyStatePanel, "EMPTY");
        alertCenterWrapper.add(alertScroll, "DATA");

        updaters.add(() -> {
            if ("EN ESPERA".equals(alertNivelLabel.getText())) {
                alertNivelLabel.setForeground(textMuted);
                alertMascotaLabel.setForeground(textMuted);
                alertIconLabel.setForeground(textMuted);
            } else {
                alertMascotaLabel.setForeground(textPrimary);
                if (alertNivelLabel.getText().contains("CRÍTICO") || alertNivelLabel.getText().contains("CRITICO")) alertNivelLabel.setForeground(dangerRed);
                else if (alertNivelLabel.getText().contains("ALTO")) alertNivelLabel.setForeground(warnOrange);
                else alertNivelLabel.setForeground(okGreen);
            }
            if (alertTextArea.getText() != null && alertTextArea.getText().contains("aparecerá aquí")) {
                alertTextArea.setForeground(textMuted);
            } else {
                alertTextArea.setForeground(textPrimary);
            }
        });

        JPanel footerRow = new JPanel(new BorderLayout());
        footerRow.setOpaque(false);
        
        JLabel footer = makeLabel("BioGeo Risk · Vigilancia Zoonótica", FONT_LABEL.deriveFont(10f), () -> textMuted);
        footer.setVerticalAlignment(SwingConstants.BOTTOM);
        
        JPanel rightTitles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTitles.setOpaque(false);
        
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 0));
        titles.setOpaque(false);
        JLabel title = makeLabel("VetSentinel", FONT_TITLE.deriveFont(14f), () -> accentTeal);
        title.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel sub   = makeLabel("Un Enfoque One Health", FONT_LABEL.deriveFont(10f), () -> textMuted);
        sub.setHorizontalAlignment(SwingConstants.RIGHT);
        titles.add(title);
        titles.add(sub);
        
        JLabel icon = new JLabel("🐾");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        updaters.add(() -> icon.setForeground(textPrimary));
        
        rightTitles.add(titles);
        rightTitles.add(icon);
        
        footerRow.add(footer, BorderLayout.WEST);
        footerRow.add(rightTitles, BorderLayout.EAST);

        JPanel mainBody = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 120));
                g.fillRect(0, 5, 1, getHeight() - 10);
            }
        };
        mainBody.setOpaque(false);
        mainBody.setBorder(new EmptyBorder(8, 15, 0, 0));
        mainBody.add(sep,    BorderLayout.NORTH);
        mainBody.add(alertCenterWrapper, BorderLayout.CENTER);
        mainBody.add(footerRow, BorderLayout.SOUTH);
        
        alertPanel.add(alertHeader, BorderLayout.NORTH);
        alertPanel.add(mainBody, BorderLayout.CENTER);

        return alertPanel;
    }

    private void evaluarRiesgoEntorno() {
        int altitud = 0;
        try { altitud = Integer.parseInt(txtAltitud.getText().trim()); } catch (NumberFormatException ignored) {}
        
        String departamento = cbDepartamento.getSelectedItem().toString();
        String area = cbArea.getSelectedItem().toString();
        boolean tieneGatos = chkGatos.isSelected();
        boolean tieneGestantes = chkGestantes.isSelected();
        boolean tieneNinos = chkNinos.isSelected();
        
        StringBuilder reporte = new StringBuilder();
        reporte.append("=== REPORTE PREVENTIVO BIOGEO RISK ===\n\n");
        reporte.append("Ubicación: ").append(departamento).append(" | Altitud: ").append(altitud).append(" msnm\n");
        reporte.append("Tipo de Área: ").append(area).append("\n\n");
        
        boolean esCritico = false;
        boolean esAlto = false;
        
        if (altitud < 1600) {
            reporte.append("🚨 ALERTA DE RECEPTIVIDAD VECTORIAL (LEISHMANIASIS):\n");
            reporte.append("- Altitud inferior a 1,600 msnm. Condiciones bioclimáticas óptimas para la presencia del vector Lutzomyia sp.\n\n");
            esAlto = true;
        }
        
        if (area.equalsIgnoreCase("Rural")) {
            reporte.append("🌾 ALERTA DE RURALIDAD (LEISHMANIASIS):\n");
            reporte.append("- El 82.7% de los casos nacionales ocurren en zonas rurales. Mayor probabilidad de contacto peridomiciliario con el vector.\n\n");
            esAlto = true;
        }
        
        if (tieneGatos && tieneGestantes) {
            reporte.append("🤰 ALERTA DE TOXOPLASMOSIS EN EL HOGAR:\n");
            reporte.append("- Coexistencia de felinos y mujeres gestantes. Alto riesgo de transmisión si hay manipulación de excretas.\n\n");
            esCritico = true;
        }
        
        if (tieneNinos) {
            reporte.append("👶 ALERTA PREVENTIVA DE TOXOCARIASIS:\n");
            reporte.append("- Hogar con niños pequeños. Riesgo de transmisión por ingesta accidental de huevos de Toxocara sp. en zonas de juego y parques.\n\n");
            if (!esCritico) esAlto = true;
        }
        
        String nivel;
        Color color;
        String icon;
        
        if (esCritico) {
            nivel = "⚠  RIESGO CRÍTICO";
            color = dangerRed;
            icon = "🚨";
        } else if (esAlto) {
            nivel = "▲  RIESGO ALTO";
            color = warnOrange;
            icon = "⚠️";
        } else {
            nivel = "✓  RIESGO BAJO/MODERADO";
            color = okGreen;
            icon = "✅";
        }
        
        reporte.append("MEDIDAS PREVENTIVAS RECOMENDADAS:\n");
        if (altitud < 1600 || area.equalsIgnoreCase("Rural")) {
            reporte.append("- Sugerir uso de toldillos en los dormitorios del hogar.\n");
            reporte.append("- Control peridomiciliario de Lutzomyia (fumigación y limpieza de follaje seco).\n");
            reporte.append("- Instalar telas mosquiteras en ventanas y aberturas.\n");
        }
        if (tieneGatos && tieneGestantes) {
            reporte.append("- La mujer gestante debe evitar limpiar la caja de arena de los gatos.\n");
            reporte.append("- Suministrar solo alimentos cocidos o concentrados comerciales a los felinos.\n");
        }
        if (tieneNinos) {
            reporte.append("- Evitar que los niños jueguen en parques sin control de excretas caninas/felinas.\n");
            reporte.append("- Desparasitar periódicamente cada 3 meses a las mascotas del hogar.\n");
        }
        if (!tieneGatos && !tieneGestantes && !tieneNinos && altitud >= 1600 && !area.equalsIgnoreCase("Rural")) {
            reporte.append("- Continuar con controles higiénicos rutinarios en el lavado de alimentos y cuidado animal.\n");
        }
        
        alertIconLabel.setText(icon);
        alertNivelLabel.setText(nivel);
        alertNivelLabel.setForeground(color);
        alertMascotaLabel.setText("Autoevaluación de Zoonosis Parasitarias");
        alertMascotaLabel.setForeground(textPrimary);
        alertTextArea.setForeground(textPrimary);
        alertTextArea.setText(reporte.toString());
        alertTextArea.setCaretPosition(0);
        
        ((CardLayout) alertCenterWrapper.getLayout()).show(alertCenterWrapper, "DATA");
    }

    private void resetAlertPanel() {
        alertIconLabel.setText("🔍");
        alertNivelLabel.setText("EN ESPERA");
        alertMascotaLabel.setText("Haz clic en Evaluar para ver el reporte");
        alertTextArea.setText("El resultado del análisis preventivo ambiental aparecerá aquí una vez que evalúes tu zona.");
        alertNivelLabel.setForeground(textMuted);
        alertMascotaLabel.setForeground(textMuted);
        alertTextArea.setForeground(textMuted);
        if (alertCenterWrapper != null) {
            ((CardLayout) alertCenterWrapper.getLayout()).show(alertCenterWrapper, "EMPTY");
        }
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
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        updaters.add(p::repaint);
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = makeLabel(text, FONT_SECTION, () -> accentTeal);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        Runnable updater = () -> {
            l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                    new Color(accentTeal.getRed(), accentTeal.getGreen(), accentTeal.getBlue(), 60)));
        };
        updater.run();
        updaters.add(updater);
        return l;
    }

    private JPanel fieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = makeLabel(labelText, FONT_LABEL, () -> textMuted);
        row.add(lbl,   BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private <T> JComboBox<T> createCombo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_INPUT);
        Runnable updater = () -> {
            cb.setBackground(bgInput);
            cb.setForeground(textPrimary);
            cb.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor),
                    new EmptyBorder(6, 8, 6, 8)));
        };
        updater.run();
        updaters.add(updater);
        
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list,
                                                                    Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Color selBg = isDarkMode ? new Color(40, 60, 80) : new Color(220, 230, 240);
                setBackground(isSelected ? selBg : bgInput);
                setForeground(textPrimary);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
        return cb;
    }

    private JCheckBox createCheckBox() {
        JCheckBox cb = new JCheckBox();
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        return cb;
    }

    private JPanel riskCard(String emoji, String label, JCheckBox cb) {
        cb.setVisible(false);

        JPanel p = new JPanel(new GridBagLayout()) {
            boolean isHovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { isHovered = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e) { isHovered = false; repaint(); }
                    public void mouseReleased(java.awt.event.MouseEvent e) { 
                        if (contains(e.getPoint())) {
                            cb.setSelected(!cb.isSelected());
                        }
                    }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean sel = cb.isSelected();
                Color bg, border;
                
                if (sel) {
                    bg = isHovered ? accentTeal.brighter() : accentTeal;
                    border = bg;
                } else {
                    bg = isDarkMode 
                        ? (isHovered ? new Color(40, 55, 75) : new Color(30, 41, 55)) 
                        : (isHovered ? new Color(235, 240, 245) : new Color(245, 248, 250));
                    border = isHovered ? borderColor.darker() : borderColor;
                }
                
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,20,20));
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,20,20));
                g2.dispose();
            }
        };
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 12, 5));

        JLabel ic  = new JLabel(emoji);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL.deriveFont(Font.BOLD, 11f));
        
        Runnable fgUpdater = () -> {
            boolean sel = cb.isSelected();
            Color fg = sel ? Color.WHITE : textPrimary;
            ic.setForeground(fg);
            lbl.setForeground(fg);
        };
        fgUpdater.run();
        updaters.add(fgUpdater);
        
        cb.addActionListener(e -> { fgUpdater.run(); p.repaint(); });

        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        p.add(ic, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        p.add(lbl, gbc);

        p.add(cb);

        updaters.add(p::repaint);
        return p;
    }

    private void styleScrollBar(JScrollPane scroll) {
        JScrollBar vsb = scroll.getVerticalScrollBar();
        Runnable updater = () -> {
            vsb.setBackground(bgCard);
            vsb.setUI(new BasicScrollBarUI() {
                @Override protected void configureScrollBarColors() {
                    thumbColor  = isDarkMode ? new Color(60, 80, 100) : new Color(180, 190, 200);
                    trackColor  = bgCard;
                }
                @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
                @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
                private JButton zeroButton() {
                    JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
                }
            });
        };
        updater.run();
        updaters.add(updater);
    }
}
