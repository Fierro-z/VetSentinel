package com.vetsentinel.ui;

import com.vetsentinel.model.*;
import com.vetsentinel.repository.*;
import com.vetsentinel.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Supplier;

public class VentanaVeterinaria extends VetBaseFrame {

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  20);
    private static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);

    private JPanel           root;
    private JButton          btnThemeToggle;
    private JTextField       txtNombreMascota;
    private JTextField       txtEdadMascota;
    private JComboBox<String> cbEspecie;
    private JComboBox<Parasito> cbParasito;
    private JComboBox<Parasito> cbSubParasito;
    private JPanel           rowSubtipo;
    private JTextField       txtCedula;
    private JTextField       txtNombrePropietario;
    private JTextField       txtDireccion;
    private JComboBox<String> cbDepartamento;
    private JComboBox<String> cbEstrato;
    private JComboBox<String> cbRegimen;
    private JTextField       txtAltitud;
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

    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;
    private final ParasitoRepository parasitoRepository;
    private final DiagnosticoRepository diagnosticoRepository;
    private final AuthenticationService authenticationService;
    private final RiskAssessmentService riskAssessmentService;

    private static final String[] DEPARTAMENTOS = {
        "Amazonas", "Antioquia", "Arauca", "Atlántico", "Bolívar", "Boyacá", "Caldas", "Caquetá", "Casanare", "Cauca", 
        "Cesar", "Chocó", "Córdoba", "Cundinamarca", "Guainía", "Guaviare", "Huila", "La Guajira", "Magdalena", "Meta", 
        "Nariño", "Norte de Santander", "Putumayo", "Quindío", "Risaralda", "San Andrés y Providencia", "Santander", 
        "Sucre", "Tolima", "Valle del Cauca", "Vaupés", "Vichada"
    };

    public VentanaVeterinaria(PropietarioRepository propietarioRepository,
                              MascotaRepository mascotaRepository,
                              ParasitoRepository parasitoRepository,
                              DiagnosticoRepository diagnosticoRepository,
                              AuthenticationService authenticationService,
                              RiskAssessmentService riskAssessmentService) {
        super("VetSentinel — Módulo Clínico Veterinario");
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
        setPreferredSize(new Dimension(1100, 750));
        setMinimumSize(new Dimension(950, 700));
        pack();
        setLocationRelativeTo(null);

        wireListeners();
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
            new VentanaSelector(propietarioRepository, mascotaRepository, parasitoRepository, diagnosticoRepository, authenticationService, riskAssessmentService).setVisible(true); 
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

    private JPanel buildFormPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());

        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setOpaque(false);
        formContent.setBorder(new EmptyBorder(0, 0, 0, 8));

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
        java.util.List<Parasito> parasitosDB = parasitoRepository.obtenerTodos();
        java.util.List<Parasito> principales = new java.util.ArrayList<>();
        java.util.List<Parasito> subLeishmania = new java.util.ArrayList<>();
        Parasito parentLeishmania = null;

        for (Parasito p : parasitosDB) {
            String nombre = p.getNombre().toLowerCase();
            if (nombre.contains("cutánea") || nombre.contains("cutanea") ||
                nombre.contains("mucosa") ||
                nombre.contains("visceral")) {
                subLeishmania.add(p);
            } else if (nombre.equals("leishmaniasis")) {
                parentLeishmania = p;
            } else {
                principales.add(p);
            }
        }
        if (parentLeishmania != null) {
            principales.add(0, parentLeishmania);
        }

        formContent.add(fieldRow("Parásito diagnosticado",
                cbParasito = createCombo(principales.toArray(new Parasito[0]))));
        formContent.add(Box.createVerticalStrut(3));
        
        rowSubtipo = fieldRow("Tipo de Leishmaniasis",
                cbSubParasito = createCombo(subLeishmania.toArray(new Parasito[0])));
        rowSubtipo.setVisible(parentLeishmania != null && cbParasito.getSelectedItem() == parentLeishmania);
        formContent.add(rowSubtipo);

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
        formContent.add(fieldRow("Estrato Socioeconómico", cbEstrato = createCombo(new String[]{"1", "2", "3", "4", "5", "6"})));
        formContent.add(Box.createVerticalStrut(3));
        formContent.add(fieldRow("Régimen de Salud", cbRegimen = createCombo(new String[]{"Contributivo", "Subsidiado", "Especial", "Otros"})));
        formContent.add(Box.createVerticalStrut(3));
        formContent.add(fieldRow("Altitud del Hogar (msnm)", txtAltitud = createTextField("Ej: 1540")));
        txtAltitud.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) && e.getKeyChar() != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });
        formContent.add(Box.createVerticalStrut(3));
        JPanel rowEmbarazos = fieldRow("Número de embarazos previos (paridad)", txtNumeroEmbarazos = createTextField("Ej: 0, 1, 2..."));
        rowEmbarazos.setVisible(false);
        formContent.add(rowEmbarazos);

        formContent.add(Box.createVerticalStrut(10));
        formContent.add(sectionLabel("FACTORES DE RIESGO EN EL HOGAR"));
        formContent.add(Box.createVerticalStrut(5));

        JPanel riskRow = new JPanel(new GridLayout(1, 3, 6, 0));
        riskRow.setOpaque(false);
        riskRow.setAlignmentX(Component.LEFT_ALIGNMENT); 
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

    private JPanel buildAlertPanel() {
        alertPanel = createCard();
        alertPanel.setLayout(new BorderLayout(0, 12));

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
        alertTextArea.setText("El resultado del análisis de riesgo de\nconvivencia aparecerá aquí una vez que\nguardes un diagnóstico.");

        JScrollPane alertScroll = new JScrollPane(alertTextArea);
        alertScroll.setOpaque(false);
        alertScroll.getViewport().setOpaque(false);
        alertScroll.setBorder(null);
        styleScrollBar(alertScroll);

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
            if (alertTextArea.getText() != null && alertTextArea.getText().contains("aparecerán aquí")) {
                alertTextArea.setForeground(textMuted);
            } else {
                alertTextArea.setForeground(textPrimary);
            }
        });

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

    private void wireListeners() {
        btnGuardar.addActionListener(e -> guardarYMostrarAlerta());
        btnVerHistorial.addActionListener(e -> verHistorial());
        btnEstadisticas.addActionListener(e -> verEstadisticas());

        cbParasito.addActionListener(e -> {
            Object selected = cbParasito.getSelectedItem();
            if (selected instanceof Parasito) {
                Parasito p = (Parasito) selected;
                boolean isLeishmania = p.getNombre().equalsIgnoreCase("Leishmaniasis");
                rowSubtipo.setVisible(isLeishmania);
            } else {
                rowSubtipo.setVisible(false);
            }
            if (rowSubtipo.getParent() != null) {
                rowSubtipo.getParent().revalidate();
                rowSubtipo.getParent().repaint();
            }
        });
    }

    private void verEstadisticas() {
        int totalMascotas = diagnosticoRepository.obtenerTotalMascotasEvaluadas();
        int totalCriticos = diagnosticoRepository.obtenerTotalDiagnosticosCriticos();
        String parasitoComun = diagnosticoRepository.obtenerParasitoPredominante();
        
        StringBuilder stats = new StringBuilder();
        stats.append("Total de mascotas evaluadas: ").append(totalMascotas).append("\n\n");
        stats.append("Total de diagnósticos críticos: ").append(totalCriticos).append("\n\n");
        stats.append("Parásito predominante en clínica: ").append(parasitoComun);
        
        showStyledDialog("Dashboard Epidemiológico INS", stats.toString(), JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void buscarClienteAutocompletar() {
        String ced = txtCedula.getText().trim();
        if (ced.isEmpty()) return;
        if (!ced.matches("^[0-9]{5,15}$")) {
            showStyledDialog("Cédula Inválida", 
                    "La cédula para buscar debe contener únicamente números y tener entre 5 y 15 dígitos.", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Propietario p = propietarioRepository.buscarPorCedula(ced);
        if (p != null) {
            txtNombrePropietario.setText(p.getNombre());
            txtDireccion.setText(p.getDireccion());
            if (p.getDepartamento() != null) cbDepartamento.setSelectedItem(p.getDepartamento());
            txtNumeroEmbarazos.setText(String.valueOf(p.getNumeroDeEmbarazosPrevios()));
            cbEstrato.setSelectedItem(String.valueOf(p.getEstrato()));
            cbRegimen.setSelectedItem(p.getRegimen());
            txtAltitud.setText(String.valueOf(p.getAltitud()));
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

        String rawNombreMascota = txtNombreMascota.getText().trim();
        String rawCedula = txtCedula.getText().trim();
        String rawNombrePropietario = txtNombrePropietario.getText().trim();
        String rawDireccion = txtDireccion.getText().trim();
        String rawEdadMascota = txtEdadMascota.getText().trim();
        String rawNumeroEmbarazos = txtNumeroEmbarazos.getText().trim();
        String rawAltitud = txtAltitud.getText().trim();

        // 1. Sanitización de textos
        String nombreMascota = sanitizar(rawNombreMascota);
        String cedula = sanitizar(rawCedula);
        String nombrePropietario = sanitizar(rawNombrePropietario);
        String direccion = sanitizar(rawDireccion);

        // 2. Validación de Cédula
        if (!cedula.matches("^[0-9]{5,15}$")) {
            showStyledDialog("Cédula Inválida", 
                    "La cédula debe contener únicamente números y tener entre 5 y 15 dígitos.", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Validación de Nombre de Propietario
        if (!nombrePropietario.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'\\-]{2,80}$")) {
            showStyledDialog("Nombre de Propietario Inválido", 
                    "El nombre del propietario debe contener únicamente letras y espacios (entre 2 y 80 caracteres).", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 4. Validación de Nombre de Mascota
        if (!nombreMascota.matches("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s'\\-]{1,40}$")) {
            showStyledDialog("Nombre de Mascota Inválido", 
                    "El nombre de la mascota debe contener únicamente letras, números y espacios (entre 1 y 40 caracteres).", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 5. Validación de Dirección
        if (!direccion.matches("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s.,#\\-()]{3,120}$")) {
            showStyledDialog("Dirección Inválida", 
                    "La dirección debe contener letras, números, espacios y los caracteres permitidos (., # - ()), con una longitud entre 3 y 120 caracteres.", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 6. Validación de Edad de Mascota
        int edad = 1;
        if (!rawEdadMascota.isEmpty()) {
            if (!rawEdadMascota.matches("^[0-9]+$")) {
                showStyledDialog("Edad Inválida", 
                        "La edad de la mascota debe ser un número entero positivo.", 
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                edad = Integer.parseInt(rawEdadMascota);
                if (edad < 0 || edad > 30) {
                    showStyledDialog("Edad Inválida", 
                            "La edad de la mascota debe estar entre 0 y 30 años.", 
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                showStyledDialog("Edad Inválida", "La edad de la mascota no es válida.", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // 7. Validación de Número de Embarazos Previos
        int numeroEmbarazos = 0;
        if (chkEmbarazadas.isSelected() && !rawNumeroEmbarazos.isEmpty()) {
            if (!rawNumeroEmbarazos.matches("^[0-9]+$")) {
                showStyledDialog("Número de Embarazos Inválido", 
                        "El número de embarazos previos debe ser un número entero positivo.", 
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                numeroEmbarazos = Integer.parseInt(rawNumeroEmbarazos);
                if (numeroEmbarazos < 0 || numeroEmbarazos > 20) {
                    showStyledDialog("Número de Embarazos Inválido", 
                            "El número de embarazos previos debe estar entre 0 y 20.", 
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                showStyledDialog("Número de Embarazos Inválido", "El número de embarazos previos no es válido.", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // 8. Validación de Altitud
        int altitud = 0;
        if (!rawAltitud.isEmpty()) {
            if (!rawAltitud.matches("^-?[0-9]+$")) {
                showStyledDialog("Altitud Inválida", 
                        "La altitud debe ser un número entero.", 
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                altitud = Integer.parseInt(rawAltitud);
                if (altitud < -100 || altitud > 6000) {
                    showStyledDialog("Altitud Inválida", 
                            "La altitud debe estar en el rango de -100 a 6000 metros.", 
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                showStyledDialog("Altitud Inválida", "La altitud no es válida.", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String especie          = cbEspecie.getSelectedItem().toString();
        Parasito selectedParasito = (Parasito) cbParasito.getSelectedItem();
        if (selectedParasito != null && selectedParasito.getNombre().equalsIgnoreCase("Leishmaniasis")) {
            selectedParasito = (Parasito) cbSubParasito.getSelectedItem();
        }
        String nombreParasito = selectedParasito != null ? selectedParasito.getNombre() : "No especificado";
        String departamento     = cbDepartamento.getSelectedItem().toString();
        boolean embarazada      = chkEmbarazadas.isSelected();
        boolean ninos           = chkNinos.isSelected();
        boolean zonaRural       = chkZonaRural.isSelected();
        int estrato = 1;
        try { estrato = Integer.parseInt(cbEstrato.getSelectedItem().toString()); }
        catch (NumberFormatException ignored) {}
        String regimen = cbRegimen.getSelectedItem().toString();

        Propietario propietario = new Propietario(0, cedula, nombrePropietario, direccion, departamento, ninos, embarazada, numeroEmbarazos, zonaRural, estrato, regimen, altitud);
        Mascota     mascota     = new Mascota(0, nombreMascota, especie, edad, propietario);
        Diagnostico diagnostico = new Diagnostico(0, mascota, selectedParasito,
                java.time.LocalDate.now().toString(), "Activo");

        com.vetsentinel.model.RiskAssessmentResult result = riskAssessmentService.evaluarRiesgoHumano(diagnostico);
        String alerta = result.getAlertaTexto();
        String nivelBD = result.getNivel().getDbValue();

        mostrarAlertaEnPanel(alerta, nombreMascota, especie, nombreParasito);
        
        try {
            diagnosticoRepository.registrarCasoCompleto(propietario, mascota, selectedParasito.getId(), nivelBD);
        } catch (java.sql.SQLException ex) {
            showStyledDialog("Error al guardar en BD", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarAlertaEnPanel(String alerta, String mascota, String especie, String parasito) {
        String nivel;
        Color  nivelColor;
        String icon;

        if (alerta.contains("EMERGENCIA CRÍTICA") || alerta.contains("EMERGENCIA CRITICA")) {
            nivel = "🚨  EMERGENCIA CRÍTICA";  nivelColor = dangerRed;    icon = "🚨";
        } else if (alerta.contains("NIVEL: CRITICO") || alerta.contains("NIVEL: CRÍTICO")) {
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
        Object[][] data = diagnosticoRepository.obtenerHistorial();
        String[] cols = {"Fecha", "Riesgo", "Cédula", "Propietario", "Dirección", "Mascota", "Especie", "Parásito"};

        JTable table = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? bgCard : bgPanel);
                c.setForeground(textPrimary);
                
                if (col == 1) {
                    String v = getValueAt(row, col).toString();
                    if ("EMERGENCIA CRÍTICA".equals(v) || "EMERGENCIA CRITICA".equals(v)) c.setForeground(dangerRed);
                    else if ("CRITICO".equals(v) || "CRÍTICO".equals(v)) c.setForeground(dangerRed);
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
        
        if(table.getColumnModel().getColumnCount() == 8) {
            table.getColumnModel().getColumn(0).setPreferredWidth(85);
            table.getColumnModel().getColumn(1).setPreferredWidth(80);
            table.getColumnModel().getColumn(2).setPreferredWidth(90);
            table.getColumnModel().getColumn(3).setPreferredWidth(125);
            table.getColumnModel().getColumn(4).setPreferredWidth(140);
            table.getColumnModel().getColumn(5).setPreferredWidth(85);
            table.getColumnModel().getColumn(6).setPreferredWidth(70);
            table.getColumnModel().getColumn(7).setPreferredWidth(150);
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

    private void showStyledDialog(String title, String msg, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }

    private String sanitizar(String input) {
        if (input == null) return "";
        // Eliminar etiquetas HTML/XML
        String clean = input.replaceAll("<[^>]*>", "");
        // Eliminar secuencias que simulan inyección SQL o comentarios
        clean = clean.replace("'", "")
                     .replace("\"", "")
                     .replace(";", "")
                     .replace("--", "")
                     .replace("/*", "")
                     .replace("*/", "");
        return clean.trim();
    }
}
