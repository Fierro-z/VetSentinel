import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;

public class VentanaVeterinaria extends JFrame {

    // ── Paleta de colores ──────────────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(13,  17,  23);
    private static final Color BG_PANEL      = new Color(22,  30,  40);
    private static final Color BG_CARD       = new Color(30,  41,  55);
    private static final Color BG_INPUT      = new Color(18,  24,  33);
    private static final Color ACCENT_TEAL   = new Color(20, 184, 166);
    private static final Color ACCENT_BLUE   = new Color(56, 139, 253);
    private static final Color DANGER_RED    = new Color(220,  53,  69);
    private static final Color WARN_ORANGE   = new Color(255, 152,   0);
    private static final Color OK_GREEN      = new Color( 40, 167,  69);
    private static final Color TEXT_PRIMARY  = new Color(230, 237, 243);
    private static final Color TEXT_MUTED    = new Color(110, 130, 150);
    private static final Color BORDER_COLOR  = new Color( 48,  62,  78);

    // ── Tipografía ─────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  20);
    private static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_INPUT   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  12);

    // ── Componentes del formulario ─────────────────────────────────────────────
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

    // ── Panel de alerta integrado ──────────────────────────────────────────────
    private JPanel    alertPanel;
    private JLabel    alertIconLabel;
    private JLabel    alertNivelLabel;
    private JTextArea alertTextArea;
    private JLabel    alertMascotaLabel;

    public VentanaVeterinaria() {
        setTitle("VetSentinel — Módulo Clínico Veterinario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BG_DARK);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);

        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(900, 640));
        setLocationRelativeTo(null);

        wireListeners();
        resetAlertPanel();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradiente sutil de izquierda a derecha
                GradientPaint gp = new GradientPaint(0, 0, new Color(16, 185, 129, 30),
                        getWidth(), 0, new Color(56, 139, 253, 10));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // línea inferior
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        header.setBackground(BG_PANEL);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Logo + título
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("🐾");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        JLabel title = makeLabel("VetSentinel", FONT_TITLE, ACCENT_TEAL);
        JLabel sub   = makeLabel("Sistema de Vigilancia Zoonótica — INS Colombia", FONT_LABEL, TEXT_MUTED);
        titles.add(title);
        titles.add(sub);

        left.add(icon);
        left.add(titles);

        // Badge de estado
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badge.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dot.setForeground(OK_GREEN);
        JLabel status = makeLabel("Sistema activo", FONT_LABEL, TEXT_MUTED);
        badge.add(dot);
        badge.add(status);

        header.add(left,  BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CENTRO: formulario (izq) + panel alerta (der)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_DARK);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.weightx = 0.48;
        gbc.weighty = 1.0;
        gbc.gridx = 0; gbc.gridy = 0;
        center.add(buildFormPanel(), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 0.52;
        center.add(buildAlertPanel(), gbc);

        return center;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FORMULARIO
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildFormPanel() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(sectionLabel("DATOS DE LA MASCOTA"));
        card.add(Box.createVerticalStrut(10));
        card.add(fieldRow("Nombre",  txtNombreMascota = createTextField("Ej: Milo")));
        card.add(Box.createVerticalStrut(8));
        card.add(fieldRow("Edad (años)", txtEdadMascota = createTextField("Ej: 3")));
        card.add(Box.createVerticalStrut(8));
        card.add(fieldRow("Especie", cbEspecie = createCombo(new String[]{"Gato", "Perro"})));
        card.add(Box.createVerticalStrut(8));
        card.add(fieldRow("Parásito diagnosticado",
                cbParasito = createCombo(new String[]{
                        "Toxoplasma gondii",
                        "Leishmania spp",
                        "Toxocara canis/cati"})));

        card.add(Box.createVerticalStrut(20));
        card.add(sectionLabel("DATOS DEL PROPIETARIO"));
        card.add(Box.createVerticalStrut(10));
        card.add(fieldRow("Nombre completo", txtNombrePropietario = createTextField("Nombre del dueño")));
        card.add(Box.createVerticalStrut(8));
        card.add(fieldRow("Dirección del hogar", txtDireccion = createTextField("Calle, barrio, ciudad")));

        card.add(Box.createVerticalStrut(16));
        card.add(sectionLabel("FACTORES DE RIESGO EN EL HOGAR"));
        card.add(Box.createVerticalStrut(10));

        JPanel riskRow = new JPanel(new GridLayout(1, 2, 12, 0));
        riskRow.setOpaque(false);
        riskRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        riskRow.add(riskCard("🤰", "Mujer embarazada", chkEmbarazada = createCheckBox()));
        riskRow.add(riskCard("👶", "Niños menores",    chkNinos      = createCheckBox()));
        card.add(riskRow);

        card.add(Box.createVerticalStrut(20));
        card.add(buildButtonRow());

        return card;
    }

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        btnGuardar = createButton("Guardar y Generar Alerta", DANGER_RED);
        btnVerHistorial = createButton("Ver Historial", new Color(30, 80, 140));

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

        alertIconLabel  = makeLabel("🔍", new Font("Segoe UI Emoji", Font.PLAIN, 32), TEXT_MUTED);
        alertNivelLabel = makeLabel("EN ESPERA", FONT_SECTION, TEXT_MUTED);
        alertMascotaLabel = makeLabel("Ingresa un diagnóstico para ver la alerta", FONT_LABEL, TEXT_MUTED);

        JPanel alertTitles = new JPanel(new GridLayout(2, 1, 0, 4));
        alertTitles.setOpaque(false);
        alertTitles.add(alertNivelLabel);
        alertTitles.add(alertMascotaLabel);

        alertHeader.add(alertIconLabel,  BorderLayout.WEST);
        alertHeader.add(alertTitles,     BorderLayout.CENTER);

        // Separador
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);

        // Área de texto de la alerta
        alertTextArea = new JTextArea();
        alertTextArea.setEditable(false);
        alertTextArea.setOpaque(false);
        alertTextArea.setFont(FONT_MONO);
        alertTextArea.setForeground(TEXT_MUTED);
        alertTextArea.setLineWrap(true);
        alertTextArea.setWrapStyleWord(true);
        alertTextArea.setBorder(null);
        alertTextArea.setText("El resultado del análisis de riesgo de\nconvivencia aparecerá aquí una vez que\nguardes un diagnóstico.");

        JScrollPane scroll = new JScrollPane(alertTextArea);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        styleScrollBar(scroll);

        // Footer con fuente
        JLabel footer = makeLabel("Fuente: INS Colombia BES SE26-2025", FONT_LABEL, new Color(60, 80, 100));
        footer.setHorizontalAlignment(SwingConstants.RIGHT);

        alertPanel.add(alertHeader, BorderLayout.NORTH);
        alertPanel.add(sep,         BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(scroll,  BorderLayout.CENTER);
        body.add(footer,  BorderLayout.SOUTH);
        alertPanel.add(body, BorderLayout.SOUTH);

        // Hacer que el scroll ocupe bien el espacio
        alertPanel.setLayout(new BorderLayout(0, 10));
        alertPanel.removeAll();
        alertPanel.add(alertHeader, BorderLayout.NORTH);

        JPanel mainBody = new JPanel(new BorderLayout(0, 8));
        mainBody.setOpaque(false);
        mainBody.setBorder(new EmptyBorder(8, 0, 0, 0));
        mainBody.add(sep,    BorderLayout.NORTH);
        mainBody.add(scroll, BorderLayout.CENTER);
        mainBody.add(footer, BorderLayout.SOUTH);
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
        alertNivelLabel.setForeground(TEXT_MUTED);
        alertMascotaLabel.setText("Ingresa un diagnóstico para ver la alerta");
        alertMascotaLabel.setForeground(TEXT_MUTED);
        alertTextArea.setForeground(TEXT_MUTED);
        alertTextArea.setText("El resultado del análisis de riesgo de\nconvivencia aparecerá aquí una vez que\nguardes un diagnóstico.");
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
        // Detectar nivel de riesgo
        String nivel;
        Color  nivelColor;
        String icon;

        if (alerta.contains("NIVEL: CRITICO")) {
            nivel = "⚠  NIVEL CRÍTICO";  nivelColor = DANGER_RED;    icon = "🚨";
        } else if (alerta.contains("NIVEL: ALTO")) {
            nivel = "▲  NIVEL ALTO";     nivelColor = WARN_ORANGE;   icon = "⚠️";
        } else if (alerta.contains("NIVEL: MEDIO")) {
            nivel = "●  NIVEL MEDIO";    nivelColor = new Color(255, 193, 7); icon = "⚡";
        } else {
            nivel = "✓  NIVEL BAJO";     nivelColor = OK_GREEN;       icon = "✅";
        }

        alertIconLabel.setText(icon);
        alertNivelLabel.setText(nivel);
        alertNivelLabel.setForeground(nivelColor);
        alertMascotaLabel.setText(mascota + " (" + especie + ")  ·  " + parasito);
        alertMascotaLabel.setForeground(TEXT_PRIMARY);
        alertTextArea.setForeground(TEXT_PRIMARY);
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
                    c.setBackground(row % 2 == 0 ? BG_CARD : BG_PANEL);
                    c.setForeground(TEXT_PRIMARY);
                    ((JComponent)c).setBorder(new EmptyBorder(6, 10, 6, 10));
                    return c;
                }
            };
            table.setBackground(BG_CARD);
            table.setForeground(TEXT_PRIMARY);
            table.setFont(FONT_INPUT);
            table.setRowHeight(34);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.getTableHeader().setBackground(BG_DARK);
            table.getTableHeader().setForeground(ACCENT_TEAL);
            table.getTableHeader().setFont(FONT_SECTION);
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(760, 340));
            scroll.setBackground(BG_CARD);
            scroll.getViewport().setBackground(BG_CARD);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            styleScrollBar(scroll);

            JPanel dialogPanel = new JPanel(new BorderLayout(0, 10));
            dialogPanel.setBackground(BG_PANEL);
            dialogPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

            JLabel dlgTitle = makeLabel("Historial de Diagnósticos", FONT_TITLE, ACCENT_TEAL);
            JLabel dlgSub   = makeLabel(data.length + " registro(s) encontrado(s)", FONT_LABEL, TEXT_MUTED);
            JPanel dlgHeader = new JPanel(new GridLayout(2,1,0,4));
            dlgHeader.setOpaque(false);
            dlgHeader.add(dlgTitle);
            dlgHeader.add(dlgSub);

            dialogPanel.add(dlgHeader, BorderLayout.NORTH);
            dialogPanel.add(scroll,    BorderLayout.CENTER);

            JOptionPane pane = new JOptionPane(dialogPanel,
                    JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
            JDialog dialog = pane.createDialog(this, "VetSentinel — Historial");
            dialog.getContentPane().setBackground(BG_PANEL);
            dialog.setBackground(BG_PANEL);
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
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = makeLabel(text, FONT_SECTION, ACCENT_TEAL);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                new Color(ACCENT_TEAL.getRed(), ACCENT_TEAL.getGreen(), ACCENT_TEAL.getBlue(), 60)));
        return l;
    }

    private JPanel fieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = makeLabel(labelText, FONT_LABEL, TEXT_MUTED);
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
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_INPUT.deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, getHeight()/2 + 5);
                    g2.dispose();
                }
            }
        };
        styleInput(tf);
        return tf;
    }

    private <T> JComboBox<T> createCombo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(FONT_INPUT);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 8, 6, 8)));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list,
                                                                    Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(40, 60, 80) : BG_INPUT);
                setForeground(TEXT_PRIMARY);
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
                Color bg = cb.isSelected()
                        ? new Color(220, 53, 69, 30)
                        : new Color(30, 41, 55);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,8,8));
                Color border = cb.isSelected() ? new Color(DANGER_RED.getRed(), DANGER_RED.getGreen(), DANGER_RED.getBlue(), 120) : BORDER_COLOR;
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,8,8));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel ic  = makeLabel(emoji, new Font("Segoe UI Emoji", Font.PLAIN, 20), TEXT_PRIMARY);
        JLabel lbl = makeLabel(label, FONT_LABEL, TEXT_PRIMARY);

        JPanel text = new JPanel(new GridLayout(2,1,0,2));
        text.setOpaque(false);
        text.add(lbl);
        text.add(makeLabel("Marcar si aplica", FONT_LABEL.deriveFont(10f), TEXT_MUTED));

        p.add(ic,  BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        p.add(cb,  BorderLayout.EAST);

        // Repintar al cambiar estado
        cb.addActionListener(e -> p.repaint());
        return p;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        btn.setPreferredSize(new Dimension(0, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleInput(JTextField tf) {
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_TEAL);
        tf.setFont(FONT_INPUT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)));
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_TEAL),
                        new EmptyBorder(6, 10, 6, 10)));
                tf.repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        new EmptyBorder(6, 10, 6, 10)));
                tf.repaint();
            }
        });
    }

    private void styleScrollBar(JScrollPane scroll) {
        JScrollBar vsb = scroll.getVerticalScrollBar();
        vsb.setBackground(BG_CARD);
        vsb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor  = new Color(60, 80, 100);
                trackColor  = BG_CARD;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });
    }

    private void showStyledDialog(String title, String msg, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }

    private JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }
}