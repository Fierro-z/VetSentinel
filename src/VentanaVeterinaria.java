import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.*;

public class VentanaVeterinaria extends VetBaseFrame {

    // ── Tipografía ─────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  20);
    private static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);

    // ── Componentes de UI ──────────────────────────────────────────────────────
    private JPanel           root;
    private JButton          btnThemeToggle;
    private JTextField       txtNombreMascota;
    private JTextField       txtEdadMascota;
    private JComboBox<String> cbEspecie;
    private JComboBox<Parasito> cbParasito;
    private JTextField       txtCedula;
    private JTextField       txtNombrePropietario;
    private JTextField       txtDireccion;
    private JComboBox<String> cbDepartamento;
    private JCheckBox        chkEmbarazadas;
    private JCheckBox        chkNinos;
    private JCheckBox        chkZonaRural;
    private JTextField       txtNumeroEmbarazos;
    private JButton          btnGuardar;
    private JButton          btnVerHistorial;
    private JButton          btnEstadisticas;

    private JPanel    alertPanel;
    private JPanel    alertCenterWrapper;
    private JLabel    alertIconLabel;
    private JLabel    alertNivelLabel;
    private JTextArea alertTextArea;
    private JLabel    alertMascotaLabel;

    private static final String[] DEPARTAMENTOS = {
        "Amazonas", "Antioquia", "Arauca", "Atlántico", "Bolívar", "Boyacá", "Caldas", "Caquetá", "Casanare", "Cauca", 
        "Cesar", "Chocó", "Córdoba", "Cundinamarca", "Guainía", "Guaviare", "Huila", "La Guajira", "Magdalena", "Meta", 
        "Nariño", "Norte de Santander", "Putumayo", "Quindío", "Risaralda", "San Andrés y Providencia", "Santander", 
        "Sucre", "Tolima", "Valle del Cauca", "Vaupés", "Vichada"
    };

    public VentanaVeterinaria() {
        super("VetSentinel — Módulo Clínico Veterinario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(bgDark);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);
        // Dimensiones ajustadas para adaptarse a pantallas más pequeñas
        setPreferredSize(new Dimension(1100, 750));

        // Ancho mínimo: 950 | Alto mínimo: 700
        setMinimumSize(new Dimension(950, 700));
        pack();
        setLocationRelativeTo(null);

        wireListeners();
        resetAlertPanel();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HEADER (CON BANNER Y SWITCH DE TEMA)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(getWidth(), 180));
        
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
        imageContainer.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Fila superior izquierda sobre el banner para el botón de retroceso
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
        btnVolverTop.addActionListener(e -> { this.dispose(); new VentanaSelector().setVisible(true); });

        topLeftRow.add(btnVolverTop);
        imageContainer.add(topLeftRow, BorderLayout.NORTH);

        headerPanel.add(imageContainer, BorderLayout.CENTER);
        return headerPanel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CENTRO: formulario (izq) + panel alerta (der)
    // ══════════════════════════════════════════════════════════════════════════
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
        gbc.weightx = 0.65; // Dar mayor prioridad de espacio al formulario
        gbc.weighty = 1.0;
        gbc.gridx = 0; gbc.gridy = 0;
        
        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.setOpaque(false);
        leftContainer.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JPanel formPanel = buildFormPanel();
        leftContainer.add(formPanel, BorderLayout.CENTER);

        center.add(leftContainer, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0); // Modificado para integrar los botones arriba
        gbc.weightx = 0.35; // Reducir el espacio del panel de alertas
        
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);
        
        // Fila superior derecha ("Sistema activo" y Botón Tema)
        JPanel topRightRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        topRightRow.setOpaque(false);

        JPanel badge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badge.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        updaters.add(() -> dot.setForeground(okGreen));
        JLabel status = makeLabel("Sistema activo", FONT_LABEL, () -> textMuted);
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

    // ══════════════════════════════════════════════════════════════════════════
    //  FORMULARIO
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildFormPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());

        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setOpaque(false);
        formContent.setBorder(new EmptyBorder(0, 0, 0, 8)); // Margen derecho para el scroll

        formContent.add(sectionLabel("DATOS DE LA MASCOTA"));
        formContent.add(Box.createVerticalStrut(5));
        formContent.add(fieldRow("Nombre",  txtNombreMascota = createTextField("Ej: Milo")));
        formContent.add(Box.createVerticalStrut(3));
        formContent.add(fieldRow("Edad (años)", txtEdadMascota = createTextField("Ej: 3")));
        txtEdadMascota.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) && e.getKeyChar() != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });
        formContent.add(Box.createVerticalStrut(3));
        formContent.add(fieldRow("Especie", cbEspecie = createCombo(new String[]{"Gato", "Perro"})));
        formContent.add(Box.createVerticalStrut(3));
        java.util.List<Parasito> parasitosDB = VeterinariaDAO.obtenerTodosLosParasitos();
        formContent.add(fieldRow("Parásito diagnosticado",
                cbParasito = createCombo(parasitosDB.toArray(new Parasito[0]))));

        formContent.add(Box.createVerticalStrut(10));
        formContent.add(sectionLabel("DATOS DEL PROPIETARIO"));
        formContent.add(Box.createVerticalStrut(5));
        
        JPanel pnlCedula = new JPanel(new BorderLayout(5, 0));
        pnlCedula.setOpaque(false);
        txtCedula = createTextField("Ej: 1020304050");
        JButton btnBuscar = createButton("Buscar", () -> accentTeal);
        btnBuscar.setPreferredSize(new Dimension(80, 42)); 
        btnBuscar.addActionListener(e -> buscarClienteAutocompletar());
        pnlCedula.add(txtCedula, BorderLayout.CENTER);
        pnlCedula.add(btnBuscar, BorderLayout.EAST);
        
        formContent.add(fieldRow("Cédula/Documento", pnlCedula));
        formContent.add(Box.createVerticalStrut(3));
        formContent.add(fieldRow("Nombre completo", txtNombrePropietario = createTextField("Nombre del dueño")));
        formContent.add(Box.createVerticalStrut(3));
        formContent.add(fieldRow("Dirección del hogar", txtDireccion = createTextField("Calle, barrio, ciudad")));
        formContent.add(Box.createVerticalStrut(3));
        formContent.add(fieldRow("Departamento", cbDepartamento = createCombo(DEPARTAMENTOS)));
        formContent.add(Box.createVerticalStrut(3));
        JPanel rowEmbarazos = fieldRow("Número de embarazos previos (paridad)", txtNumeroEmbarazos = createTextField("Ej: 0, 1, 2..."));
        rowEmbarazos.setVisible(false);
        formContent.add(rowEmbarazos);
        txtNumeroEmbarazos.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) && e.getKeyChar() != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });

        formContent.add(Box.createVerticalStrut(10));
        formContent.add(sectionLabel("FACTORES DE RIESGO EN EL HOGAR"));
        formContent.add(Box.createVerticalStrut(5));

        JPanel riskRow = new JPanel(new GridLayout(1, 3, 6, 0)); // Espacio optimizado entre tarjetas
        riskRow.setOpaque(false);
        riskRow.setAlignmentX(Component.LEFT_ALIGNMENT); 
        // Garantizar que layout no se auto-oculte por BoxLayout: SIN restricciones MaximumSize estrictas!
        chkEmbarazadas = createCheckBox();
        chkEmbarazadas.addItemListener(e -> {
            rowEmbarazos.setVisible(chkEmbarazadas.isSelected());
            formContent.revalidate();
            formContent.repaint();
        });
        riskRow.add(riskCard("🤰", "Embarazada", chkEmbarazadas));
        riskRow.add(riskCard("👶", "Niños",      chkNinos       = createCheckBox()));
        riskRow.add(riskCard("🌾", "Zona Rural", chkZonaRural   = createCheckBox()));
        formContent.add(riskRow);

        formContent.add(Box.createVerticalStrut(10));
        formContent.add(buildButtonRow());

        // Envolver en BorderLayout.NORTH evita estiramientos y cortes verticales u horizontales
        JPanel formWrapper = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                // Forza al panel a adaptarse dinámicamente al ancho visible del scroll
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

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnGuardar = createButton("Guardar", () -> dangerRed);
        btnVerHistorial = createButton("Historial", () -> accentBlue);
        btnEstadisticas = createButton("Estadísticas", () -> warnOrange);

        row.add(btnGuardar);
        row.add(btnVerHistorial);
        row.add(btnEstadisticas);
        return row;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PANEL DE ALERTA (derecha)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildAlertPanel() {
        alertPanel = createCard();
        alertPanel.setLayout(new BorderLayout(0, 12));

        // Cabecera de la alerta
        JPanel alertHeader = new JPanel(new BorderLayout(10, 0));
        alertHeader.setOpaque(false);

        alertIconLabel  = makeLabel("🔍", new Font("Segoe UI Emoji", Font.PLAIN, 32), () -> textMuted);
        alertNivelLabel = makeLabel("EN ESPERA", FONT_SECTION, () -> textMuted);
        alertMascotaLabel = makeLabel("Ingresa un diagnóstico para ver la alerta", FONT_LABEL, () -> textMuted);

        JPanel alertTitles = new JPanel(new GridLayout(2, 1, 0, 4));
        alertTitles.setOpaque(false);
        alertTitles.add(alertNivelLabel);
        alertTitles.add(alertMascotaLabel);

        alertHeader.add(alertIconLabel,  BorderLayout.WEST);
        alertHeader.add(alertTitles,     BorderLayout.CENTER);

        // Separador
        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(borderColor);
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        updaters.add(sep::repaint);

        // Área de texto de la alerta
        alertTextArea = new JTextArea();
        alertTextArea.setEditable(false);
        alertTextArea.setOpaque(false);
        alertTextArea.setFont(FONT_MONO);
        alertTextArea.setForeground(textMuted);
        alertTextArea.setLineWrap(true);
        alertTextArea.setWrapStyleWord(true);
        alertTextArea.setBorder(null);
        alertTextArea.setText("El resultado del análisis de riesgo de\nconvivencia aparecerá aquí una vez que\nguardes un diagnóstico.");

        JScrollPane alertScroll = new JScrollPane(alertTextArea);
        alertScroll.setOpaque(false);
        alertScroll.getViewport().setOpaque(false);
        alertScroll.setBorder(null);
        styleScrollBar(alertScroll);

        // Estado vacío (Empty State) con ilustración minimalista
        JPanel emptyStatePanel = new JPanel(new GridBagLayout());
        emptyStatePanel.setOpaque(false);
        JLabel emptyIcon = new JLabel("🩺");
        emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        JLabel emptyText = makeLabel("A la espera de diagnóstico...", FONT_LABEL.deriveFont(Font.ITALIC), () -> textMuted);
        GridBagConstraints ec = new GridBagConstraints();
        ec.gridx = 0; ec.gridy = 0;
        emptyStatePanel.add(emptyIcon, ec);
        ec.gridy = 1; ec.insets = new Insets(12, 0, 0, 0);
        emptyStatePanel.add(emptyText, ec);

        alertCenterWrapper = new JPanel(new CardLayout());
        alertCenterWrapper.setOpaque(false);
        alertCenterWrapper.add(emptyStatePanel, "EMPTY");
        alertCenterWrapper.add(alertScroll, "DATA");

        // Updater específico para los textos del panel interactivo de la derecha
        updaters.add(() -> {
            if ("EN ESPERA".equals(alertNivelLabel.getText())) {
                alertNivelLabel.setForeground(textMuted);
                alertMascotaLabel.setForeground(textMuted);
                alertIconLabel.setForeground(textMuted);
            } else {
                alertMascotaLabel.setForeground(textPrimary);
                if (alertNivelLabel.getText().contains("CRÍTICO")) alertNivelLabel.setForeground(dangerRed);
                else if (alertNivelLabel.getText().contains("ALTO")) alertNivelLabel.setForeground(warnOrange);
                else alertNivelLabel.setForeground(okGreen);
            }
            if (alertTextArea.getText() != null && alertTextArea.getText().contains("aparecerán aquí")) {
                alertTextArea.setForeground(textMuted);
            } else {
                alertTextArea.setForeground(textPrimary);
            }
        });

        // Footer interactivo con logo reubicado a la esquina inferior derecha
        JPanel footerRow = new JPanel(new BorderLayout());
        footerRow.setOpaque(false);
        
        JLabel footer = makeLabel("Fuente: INS Colombia BES SE26-2025", FONT_LABEL.deriveFont(10f), () -> textMuted);
        footer.setVerticalAlignment(SwingConstants.BOTTOM);
        
        JPanel rightTitles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTitles.setOpaque(false);
        
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 0));
        titles.setOpaque(false);
        JLabel title = makeLabel("VetSentinel", FONT_TITLE.deriveFont(14f), () -> accentTeal);
        title.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel sub   = makeLabel("Vigilancia Zoonótica INS", FONT_LABEL.deriveFont(10f), () -> textMuted);
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

        // Separador sutil a la izquierda para enmarcar el panel de análisis
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

    // ══════════════════════════════════════════════════════════════════════════
    //  LÓGICA
    // ══════════════════════════════════════════════════════════════════════════
    private void wireListeners() {
        btnGuardar.addActionListener(e -> guardarYMostrarAlerta());
        btnVerHistorial.addActionListener(e -> verHistorial());
        btnEstadisticas.addActionListener(e -> verEstadisticas());
    }

    private void verEstadisticas() {
        String stats = VeterinariaDAO.obtenerEstadisticasEpidemiologicas();
        showStyledDialog("Dashboard Epidemiológico INS", stats, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void buscarClienteAutocompletar() {
        String ced = txtCedula.getText().trim();
        if (ced.isEmpty()) return;
        Propietario p = VeterinariaDAO.buscarPropietarioPorCedula(ced);
        if (p != null) {
            txtNombrePropietario.setText(p.getNombre());
            txtDireccion.setText(p.getDireccion());
            if (p.getDepartamento() != null) cbDepartamento.setSelectedItem(p.getDepartamento());
            txtNumeroEmbarazos.setText(String.valueOf(p.getNumeroDeEmbarazosPrevios()));
            chkNinos.setSelected(p.isTieneNinos());
            chkEmbarazadas.setSelected(p.isHayEmbarazadas());
            chkZonaRural.setSelected(p.isZonaRural());
            chkNinos.repaint();
            chkEmbarazadas.repaint();
            chkZonaRural.repaint();
            showStyledDialog("Búsqueda Exitosa", "Perfil de cliente cargado de la base de datos.", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showStyledDialog("Búsqueda", "Cliente no fue encontrado. Procede registrarlo nuevo.", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetAlertPanel() {
        alertIconLabel.setText("🔍");
        alertNivelLabel.setText("EN ESPERA");
        alertMascotaLabel.setText("Ingresa un diagnóstico para ver la alerta");
        alertTextArea.setText("El resultado del análisis de riesgo de\nconvivencia aparecerá aquí una vez que\nguardes un diagnóstico.");
        alertNivelLabel.setForeground(textMuted);
        alertMascotaLabel.setForeground(textMuted);
        alertTextArea.setForeground(textMuted);
        if (alertCenterWrapper != null) {
            ((CardLayout) alertCenterWrapper.getLayout()).show(alertCenterWrapper, "EMPTY");
        }
    }

    private void guardarYMostrarAlerta() {
        if (txtNombreMascota.getText().trim().isEmpty()
                || txtCedula.getText().trim().isEmpty()
                || txtNombrePropietario.getText().trim().isEmpty()) {
            showStyledDialog("Campos vacíos",
                    "Por favor completa la cédula, el nombre de la mascota y del propietario.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombreMascota    = txtNombreMascota.getText().trim();
        String especie          = cbEspecie.getSelectedItem().toString();
        int    edad             = 1;
        try { edad = Integer.parseInt(txtEdadMascota.getText().trim()); }
        catch (NumberFormatException ignored) {}
        Parasito selectedParasito = (Parasito) cbParasito.getSelectedItem();
        String nombreParasito = selectedParasito.getNombre();
        String cedula           = txtCedula.getText().trim();
        String nombrePropietario = txtNombrePropietario.getText().trim();
        String direccion        = txtDireccion.getText().trim();
        String departamento     = cbDepartamento.getSelectedItem().toString();
        boolean embarazada      = chkEmbarazadas.isSelected();
        boolean ninos           = chkNinos.isSelected();
        boolean zonaRural       = chkZonaRural.isSelected();
        int numeroEmbarazos = 0;
        try { numeroEmbarazos = Integer.parseInt(txtNumeroEmbarazos.getText().trim()); }
        catch (NumberFormatException ignored) {}

        Propietario propietario = new Propietario(0, cedula, nombrePropietario, direccion, departamento, ninos, embarazada, numeroEmbarazos, zonaRural);
        Mascota     mascota     = new Mascota(0, nombreMascota, especie, edad, propietario);
        Diagnostico diagnostico = new Diagnostico(0, mascota, selectedParasito,
                java.time.LocalDate.now().toString(), "Activo");

        String alerta = RiesgoService.evaluarRiesgoHumano(diagnostico);
        
        String nivelBD = "BAJO";
        if (alerta.contains("NIVEL: CRITICO")) nivelBD = "CRITICO";
        else if (alerta.contains("NIVEL: ALTO")) nivelBD = "ALTO";
        else if (alerta.contains("NIVEL: MEDIO")) nivelBD = "MEDIO";
        else if (alerta.contains("NIVEL: MODERADO")) nivelBD = "MODERADO";

        mostrarAlertaEnPanel(alerta, nombreMascota, especie, nombreParasito);
        
        try {
            int idProp = VeterinariaDAO.upsertPropietario(propietario);
            propietario.setId(idProp);
            mascota.getPropietario().setId(idProp);
            
            int idMasc = VeterinariaDAO.upsertMascota(mascota);
            mascota.setId(idMasc);
            
            VeterinariaDAO.insertarDiagnostico(idMasc, selectedParasito.getId(), nivelBD);
        } catch (java.sql.SQLException ex) {
            showStyledDialog("Error al guardar en BD", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarAlertaEnPanel(String alerta, String mascota, String especie, String parasito) {
        String nivel;
        Color  nivelColor;
        String icon;

        if (alerta.contains("NIVEL: CRITICO")) {
            nivel = "⚠  NIVEL CRÍTICO";  nivelColor = dangerRed;    icon = "🚨";
        } else if (alerta.contains("NIVEL: ALTO") || alerta.contains("NIVEL: MEDIO")) {
            nivel = "▲  ATENCIÓN";     nivelColor = warnOrange;   icon = "⚠️";
        } else {
            nivel = "✓  NIVEL BAJO";     nivelColor = okGreen;       icon = "✅";
        }

        alertIconLabel.setText(icon);
        alertNivelLabel.setText(nivel);
        alertNivelLabel.setForeground(nivelColor);
        alertMascotaLabel.setText(mascota + " (" + especie + ")  ·  " + parasito);
        alertMascotaLabel.setForeground(textPrimary);
        alertTextArea.setForeground(textPrimary);
        alertTextArea.setText(alerta);
        alertTextArea.setCaretPosition(0);
        if (alertCenterWrapper != null) {
            ((CardLayout) alertCenterWrapper.getLayout()).show(alertCenterWrapper, "DATA");
        }
    }

    private void verHistorial() {
        Object[][] data = VeterinariaDAO.obtenerHistorial();
        String[] cols = {"Fecha", "Riesgo", "Cédula", "Propietario", "Dirección", "Mascota", "Especie", "Parásito"};

            JTable table = new JTable(data, cols) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
                @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                    Component c = super.prepareRenderer(r, row, col);
                    c.setBackground(row % 2 == 0 ? bgCard : bgPanel);
                    c.setForeground(textPrimary);
                    
                    if (col == 1) { // RIESGO column
                        String v = getValueAt(row, col).toString();
                        if ("CRITICO".equals(v)) c.setForeground(dangerRed);
                        else if ("ALTO".equals(v) || "MODERADO".equals(v)) c.setForeground(warnOrange);
                        else if ("MEDIO".equals(v)) c.setForeground(new Color(230, 180, 50));
                        else if ("BAJO".equals(v)) c.setForeground(okGreen);
                    }
                    
                    ((JComponent)c).setBorder(new EmptyBorder(6, 10, 6, 10));
                    return c;
                }
            };
            table.setBackground(bgCard);
            table.setForeground(textPrimary);
            table.setFont(FONT_INPUT);
            table.setRowHeight(34);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.getTableHeader().setBackground(bgDark);
            table.getTableHeader().setForeground(accentTeal);
            table.getTableHeader().setFont(FONT_SECTION);
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
            
            // Mejorar estética ajustando el ancho de cada columna individualmente
            if(table.getColumnModel().getColumnCount() == 8) {
                table.getColumnModel().getColumn(0).setPreferredWidth(85);  // Fecha
                table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Riesgo
                table.getColumnModel().getColumn(2).setPreferredWidth(90);  // Cédula
                table.getColumnModel().getColumn(3).setPreferredWidth(125); // Propietario
                table.getColumnModel().getColumn(4).setPreferredWidth(140); // Dirección
                table.getColumnModel().getColumn(5).setPreferredWidth(85);  // Mascota
                table.getColumnModel().getColumn(6).setPreferredWidth(70);  // Especie
                table.getColumnModel().getColumn(7).setPreferredWidth(150); // Parásito
            }

            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(950, 380));
            scroll.setBackground(bgCard);
            scroll.getViewport().setBackground(bgCard);
            scroll.setBorder(BorderFactory.createLineBorder(borderColor));
            styleScrollBar(scroll);

            JPanel dialogPanel = new JPanel(new BorderLayout(0, 10));
            dialogPanel.setBackground(bgPanel);
            dialogPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

            JLabel dlgTitle = makeLabel("Historial de Diagnósticos", FONT_TITLE, () -> accentTeal);
            JLabel dlgSub   = makeLabel(data.length + " registro(s) encontrado(s)", FONT_LABEL, () -> textMuted);
            JPanel dlgHeader = new JPanel(new GridLayout(2,1,0,4));
            dlgHeader.setOpaque(false);
            dlgHeader.add(dlgTitle);
            dlgHeader.add(dlgSub);

            dialogPanel.add(dlgHeader, BorderLayout.NORTH);
            dialogPanel.add(scroll,    BorderLayout.CENTER);

            JOptionPane pane = new JOptionPane(dialogPanel,
                    JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
            JDialog dialog = pane.createDialog(this, "VetSentinel — Historial");
            dialog.getContentPane().setBackground(bgPanel);
            dialog.setBackground(bgPanel);
            dialog.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS DE UI
    // ══════════════════════════════════════════════════════════════════════════
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
        cb.setVisible(false); // Ocultar el checkbox real

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

        p.add(cb); // Checkbox oculto acoplado para no romper lógica externa

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

    private void showStyledDialog(String title, String msg, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }
}