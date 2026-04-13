import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class VentanaVeterinaria extends JFrame {

    private boolean isDarkMode = false;
    private List<Runnable> updaters = new ArrayList<>();

    // ── Paleta de colores Dinámica ─────────────────────────────────────────────
    private Color bgDark;
    private Color bgPanel;
    private Color bgCard;
    private Color bgInput;
    private Color accentTeal;
    private Color accentBlue;
    private Color dangerRed;
    private Color warnOrange;
    private Color okGreen;
    private Color textPrimary;
    private Color textMuted;
    private Color borderColor;

    // ── Tipografía ─────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  20);
    private static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_INPUT   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  12);

    // ── Componentes de UI ──────────────────────────────────────────────────────
    private JPanel           root;
    private JButton          btnThemeToggle;
    private JTextField       txtNombreMascota;
    private JTextField       txtEdadMascota;
    private JComboBox<String> cbEspecie;
    private JComboBox<String> cbParasito;
    private JTextField       txtNombrePropietario;
    private JTextField       txtDireccion;
    private JCheckBox        chkEmbarazada;
    private JCheckBox        chkNinos;
    private JButton          btnGuardar;
    private JButton          btnVerHistorial;

    private JPanel    alertPanel;
    private JLabel    alertIconLabel;
    private JLabel    alertNivelLabel;
    private JTextArea alertTextArea;
    private JLabel    alertMascotaLabel;

    public VentanaVeterinaria() {
        setTitle("VetSentinel — Módulo Clínico Veterinario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        aplicarColores(isDarkMode);

        root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(bgDark);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);
        setPreferredSize(new Dimension(1100, 850));
        setMinimumSize(new Dimension(1000, 800));
        pack();
        setLocationRelativeTo(null);

        wireListeners();
        resetAlertPanel();
    }

    private void aplicarColores(boolean oscuro) {
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

    private void alternarTema() {
        isDarkMode = !isDarkMode;
        aplicarColores(isDarkMode);
        
        getContentPane().setBackground(bgDark);
        root.setBackground(bgDark);
        
        for (Runnable r : updaters) {
            r.run();
        }
        
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
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

        // Retiramos el botón de tema del banner (se mueve arriba del panel derecho)
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
        gbc.weightx = 0.48;
        gbc.weighty = 1.0;
        gbc.gridx = 0; gbc.gridy = 0;
        
        JPanel leftContainer = new JPanel();
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
        leftContainer.setOpaque(false);

        // Sin badge en la izquierda, todo el espacio vertical de la izquierda pertenece ahora al formulario.
        // Solo un pequeño margen superior para evitar que el título se pegue a arriba.
        leftContainer.add(Box.createVerticalStrut(10));
        
        JPanel formPanel = buildFormPanel();
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftContainer.add(formPanel);

        center.add(leftContainer, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0); // Modificado para integrar los botones arriba
        gbc.weightx = 0.52;
        
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
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(sectionLabel("DATOS DE LA MASCOTA"));
        card.add(Box.createVerticalStrut(5));
        card.add(fieldRow("Nombre",  txtNombreMascota = createTextField("Ej: Milo")));
        card.add(Box.createVerticalStrut(3));
        card.add(fieldRow("Edad (años)", txtEdadMascota = createTextField("Ej: 3")));
        card.add(Box.createVerticalStrut(3));
        card.add(fieldRow("Especie", cbEspecie = createCombo(new String[]{"Gato", "Perro"})));
        card.add(Box.createVerticalStrut(3));
        card.add(fieldRow("Parásito diagnosticado",
                cbParasito = createCombo(new String[]{
                        "Toxoplasma gondii",
                        "Leishmania spp",
                        "Toxocara canis/cati"})));

        card.add(Box.createVerticalStrut(10));
        card.add(sectionLabel("DATOS DEL PROPIETARIO"));
        card.add(Box.createVerticalStrut(5));
        card.add(fieldRow("Nombre completo", txtNombrePropietario = createTextField("Nombre del dueño")));
        card.add(Box.createVerticalStrut(3));
        card.add(fieldRow("Dirección del hogar", txtDireccion = createTextField("Calle, barrio, ciudad")));

        card.add(Box.createVerticalStrut(10));
        card.add(sectionLabel("FACTORES DE RIESGO EN EL HOGAR"));
        card.add(Box.createVerticalStrut(5));

        JPanel riskRow = new JPanel(new GridLayout(1, 2, 12, 0));
        riskRow.setOpaque(false);
        riskRow.setAlignmentX(Component.LEFT_ALIGNMENT); 
        // Garantizar que layout no se auto-oculte por BoxLayout: SIN restricciones MaximumSize estrictas!
        riskRow.add(riskCard("🤰", "Mujer embarazada", chkEmbarazada = createCheckBox()));
        riskRow.add(riskCard("👶", "Niños menores",    chkNinos      = createCheckBox()));
        card.add(riskRow);

        card.add(Box.createVerticalStrut(10));
        card.add(buildButtonRow());
        card.add(Box.createVerticalGlue()); // Combate el estiramiento absorbiendo espacio extra

        return card;
    }

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnGuardar = createButton("Guardar y Generar Alerta", () -> dangerRed);
        btnVerHistorial = createButton("Ver Historial", () -> accentBlue);

        row.add(btnGuardar);
        row.add(btnVerHistorial);
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

        JScrollPane scroll = new JScrollPane(alertTextArea);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        styleScrollBar(scroll);

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

        JPanel mainBody = new JPanel(new BorderLayout(0, 8));
        mainBody.setOpaque(false);
        mainBody.setBorder(new EmptyBorder(8, 0, 0, 0));
        mainBody.add(sep,    BorderLayout.NORTH);
        mainBody.add(scroll, BorderLayout.CENTER);
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
    }

    private void resetAlertPanel() {
        alertIconLabel.setText("🔍");
        alertNivelLabel.setText("EN ESPERA");
        alertMascotaLabel.setText("Ingresa un diagnóstico para ver la alerta");
        alertTextArea.setText("El resultado del análisis de riesgo de\nconvivencia aparecerá aquí una vez que\nguardes un diagnóstico.");
        alertNivelLabel.setForeground(textMuted);
        alertMascotaLabel.setForeground(textMuted);
        alertTextArea.setForeground(textMuted);
    }

    private void guardarYMostrarAlerta() {
        if (txtNombreMascota.getText().trim().isEmpty()
                || txtNombrePropietario.getText().trim().isEmpty()) {
            showStyledDialog("Campos vacíos",
                    "Por favor completa el nombre de la mascota y del propietario.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombreMascota    = txtNombreMascota.getText().trim();
        String especie          = cbEspecie.getSelectedItem().toString();
        int    edad             = 1;
        try { edad = Integer.parseInt(txtEdadMascota.getText().trim()); }
        catch (NumberFormatException ignored) {}
        String nombreParasito   = cbParasito.getSelectedItem().toString();
        String nombrePropietario = txtNombrePropietario.getText().trim();
        String direccion        = txtDireccion.getText().trim();
        boolean embarazada      = chkEmbarazada.isSelected();
        boolean ninos           = chkNinos.isSelected();

        String riesgo, medidas;
        switch (nombreParasito) {
            case "Toxoplasma gondii":
                riesgo  = "Transmisión congénita. Seroprevalencia 40-60% en Colombia.";
                medidas = "No limpiar arenero sin guantes. Cocinar carne >70°C. Lavar vegetales.";
                break;
            case "Leishmania spp":
                riesgo  = "Leishmaniasis cutánea. Endémica en Antioquia (INS 2025).";
                medidas = "Control del vector Lutzomyia. Toldillos y repelente. Fumigación.";
                break;
            default:
                riesgo  = "Larva migrans en niños. Prevalencia en perros 7-20% (INS).";
                medidas = "Desparasitar cada 3 meses. Evitar que niños toquen suelo contaminado.";
        }

        Propietario propietario = new Propietario(0, nombrePropietario, direccion, ninos, embarazada);
        Mascota     mascota     = new Mascota(0, nombreMascota, especie, edad, propietario);
        Parasito    parasito    = new Parasito(0, nombreParasito, riesgo, medidas);
        Diagnostico diagnostico = new Diagnostico(0, mascota, parasito,
                java.time.LocalDate.now().toString(), "Activo");

        String alerta = diagnostico.evaluarRiesgoHumano();
        mostrarAlertaEnPanel(alerta, nombreMascota, especie, nombreParasito);
        guardarEnBD(nombrePropietario, direccion, ninos, embarazada,
                nombreMascota, especie, edad, nombreParasito);
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
    }

    private void guardarEnBD(String nombreProp, String dir, boolean ninos, boolean embarazada,
                             String nombreMascota, String especie, int edad, String parasito) {
        try (Connection con = ConexionDB.getConexion()) {
            PreparedStatement psProp = con.prepareStatement(
                    "INSERT INTO Propietarios (nombre, direccion, tiene_ninos, hay_embarazadas) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psProp.setString(1, nombreProp);
            psProp.setString(2, dir);
            psProp.setInt(3, ninos ? 1 : 0);
            psProp.setInt(4, embarazada ? 1 : 0);
            psProp.executeUpdate();
            ResultSet keysProp = psProp.getGeneratedKeys();
            int idProp = keysProp.next() ? keysProp.getInt(1) : 1;

            PreparedStatement psMasc = con.prepareStatement(
                    "INSERT INTO Mascotas (nombre, especie, edad, id_propietario) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psMasc.setString(1, nombreMascota);
            psMasc.setString(2, especie);
            psMasc.setInt(3, edad);
            psMasc.setInt(4, idProp);
            psMasc.executeUpdate();
            ResultSet keysMasc = psMasc.getGeneratedKeys();
            int idMasc = keysMasc.next() ? keysMasc.getInt(1) : 1;

            PreparedStatement psPar = con.prepareStatement("SELECT id FROM Parasitos WHERE nombre = ?");
            psPar.setString(1, parasito);
            ResultSet rsPar = psPar.executeQuery();
            int idPar = rsPar.next() ? rsPar.getInt(1) : 1;

            PreparedStatement psDiag = con.prepareStatement(
                    "INSERT INTO Diagnosticos (id_mascota, id_parasito, fecha, estado_contagio) VALUES (?,?,date('now'),'Activo')");
            psDiag.setInt(1, idMasc);
            psDiag.setInt(2, idPar);
            psDiag.executeUpdate();

        } catch (SQLException ex) {
            showStyledDialog("Error al guardar", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verHistorial() {
        try (Connection con = ConexionDB.getConexion()) {
            String sql =
                    "SELECT d.fecha, d.estado_contagio, " +
                            "m.nombre AS mascota, m.especie, " +
                            "p.nombre AS propietario, " +
                            "par.nombre AS parasito " +
                            "FROM Diagnosticos d " +
                            "JOIN Mascotas m ON d.id_mascota = m.id " +
                            "JOIN Propietarios p ON m.id_propietario = p.id " +
                            "JOIN Parasitos par ON d.id_parasito = par.id " +
                            "ORDER BY d.fecha DESC";

            Statement stmt = con.createStatement();
            ResultSet rs   = stmt.executeQuery(sql);

            String[] cols = {"Fecha", "Estado", "Mascota", "Especie", "Parásito", "Propietario"};
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getString("fecha"),
                        rs.getString("estado_contagio"),
                        rs.getString("mascota"),
                        rs.getString("especie"),
                        rs.getString("parasito"),
                        rs.getString("propietario")
                });
            }

            Object[][] data = rows.toArray(new Object[0][]);

            JTable table = new JTable(data, cols) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
                @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                    Component c = super.prepareRenderer(r, row, col);
                    c.setBackground(row % 2 == 0 ? bgCard : bgPanel);
                    c.setForeground(textPrimary);
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

            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(800, 350));
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

        } catch (SQLException ex) {
            showStyledDialog("Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
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

    private JTextField createTextField(String placeholder) {
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
        JPanel p = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color checkBg = new Color(dangerRed.getRed(), dangerRed.getGreen(), dangerRed.getBlue(), 30);
                Color normalBg = isDarkMode ? new Color(30, 41, 55) : new Color(245, 248, 250);
                Color bg = cb.isSelected() ? checkBg : normalBg;
                
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,8,8));
                
                Color selBorder = new Color(dangerRed.getRed(), dangerRed.getGreen(), dangerRed.getBlue(), 120);
                Color border = cb.isSelected() ? selBorder : borderColor;
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,8,8));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel ic  = makeLabel(emoji, new Font("Segoe UI Emoji", Font.PLAIN, 24), () -> textPrimary);
        JLabel lbl = makeLabel(label, FONT_LABEL, () -> textPrimary);

        JPanel text = new JPanel(new GridLayout(2,1,0,2));
        text.setOpaque(false);
        text.add(lbl);
        JLabel subLbl = makeLabel("Marcar si aplica", FONT_LABEL.deriveFont(10f), () -> textMuted);
        text.add(subLbl);

        p.add(ic,  BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        p.add(cb,  BorderLayout.EAST);

        updaters.add(p::repaint);
        cb.addActionListener(e -> p.repaint());
        return p;
    }

    private JButton createButton(String text, Supplier<Color> bgSupplier) {
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

    private void styleInput(JTextField tf) {
        Runnable updater = () -> {
            tf.setBackground(bgInput);
            tf.setForeground(textPrimary);
            tf.setCaretColor(accentTeal);
            tf.setFont(FONT_INPUT);
            if (!tf.isFocusOwner()) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor),
                    new EmptyBorder(6, 10, 6, 10)));
            } else {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentTeal),
                    new EmptyBorder(6, 10, 6, 10)));
            }
        };
        updater.run();
        updaters.add(updater);
        
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { updater.run(); tf.repaint(); }
            @Override public void focusLost(FocusEvent e) { updater.run(); tf.repaint(); }
        });
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

    private JLabel makeLabel(String text, Font font, Supplier<Color> colorSupplier) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(colorSupplier.get());
        updaters.add(() -> l.setForeground(colorSupplier.get()));
        return l;
    }
}