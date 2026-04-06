import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class VentanaVeterinaria extends JFrame {

    private JTextField txtNombreMascota;
    private JTextField txtEdadMascota;
    private JComboBox<String> cbEspecie;
    private JComboBox<String> cbParasito;
    private JTextField txtNombrePropietario;
    private JTextField txtDireccion;
    private JCheckBox chkEmbarazada;
    private JCheckBox chkNinos;
    private JButton btnGuardar;
    private JButton btnVerHistorial;

    public VentanaVeterinaria() {
        setTitle("VetSentinel - Modulo Clinico Veterinario");
        setSize(520, 440);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(11, 2, 8, 8));

        add(new JLabel("  Nombre de la mascota:"));
        txtNombreMascota = new JTextField();
        add(txtNombreMascota);

        add(new JLabel("  Edad (años):"));
        txtEdadMascota = new JTextField();
        add(txtEdadMascota);

        add(new JLabel("  Especie:"));
        cbEspecie = new JComboBox<>(new String[]{"Gato", "Perro"});
        add(cbEspecie);

        add(new JLabel("  Parasito diagnosticado:"));
        cbParasito = new JComboBox<>(new String[]{"Toxoplasma gondii", "Leishmania spp", "Toxocara canis/cati"});
        add(cbParasito);

        add(new JLabel("  Nombre del propietario:"));
        txtNombrePropietario = new JTextField();
        add(txtNombrePropietario);

        add(new JLabel("  Direccion del hogar:"));
        txtDireccion = new JTextField();
        add(txtDireccion);

        add(new JLabel("  ¿Hay embarazada en el hogar?"));
        chkEmbarazada = new JCheckBox("Si");
        add(chkEmbarazada);

        add(new JLabel("  ¿Hay niños menores en el hogar?"));
        chkNinos = new JCheckBox("Si");
        add(chkNinos);

        add(new JLabel(""));
        btnGuardar = new JButton("Guardar y Generar Alerta");
        btnGuardar.setBackground(new Color(180, 30, 30));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 13));
        add(btnGuardar);

        add(new JLabel(""));
        btnVerHistorial = new JButton("Ver Diagnosticos Guardados");
        btnVerHistorial.setBackground(new Color(30, 100, 180));
        btnVerHistorial.setForeground(Color.WHITE);
        add(btnVerHistorial);

        add(new JLabel(""));
        JLabel footer = new JLabel("  VetSentinel | Fuente: INS Colombia BES SE26-2025", JLabel.LEFT);
        footer.setFont(new Font("Arial", Font.ITALIC, 10));
        add(footer);

        btnGuardar.addActionListener(e -> guardarYMostrarAlerta());
        btnVerHistorial.addActionListener(e -> verHistorial());
    }

    private void guardarYMostrarAlerta() {
        if (txtNombreMascota.getText().isEmpty() || txtNombrePropietario.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos.", "Campos vacios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombreMascota = txtNombreMascota.getText();
        String especie = cbEspecie.getSelectedItem().toString();
        int edad = 1;
        try { edad = Integer.parseInt(txtEdadMascota.getText()); } catch (NumberFormatException ignored) {}
        String nombreParasito = cbParasito.getSelectedItem().toString();
        String nombrePropietario = txtNombrePropietario.getText();
        String direccion = txtDireccion.getText();
        boolean embarazada = chkEmbarazada.isSelected();
        boolean ninos = chkNinos.isSelected();

        String riesgo, medidas;
        switch (nombreParasito) {
            case "Toxoplasma gondii":
                riesgo = "Transmision congenita. Seroprevalencia 40-60% en Colombia.";
                medidas = "No limpiar arenero sin guantes. Cocinar carne a mas de 70 grados. Lavar vegetales.";
                break;
            case "Leishmania spp":
                riesgo = "Leishmaniasis cutanea. Endemica en Antioquia (INS 2025).";
                medidas = "Control del vector Lutzomyia. Toldillos y repelente. Fumigacion.";
                break;
            default:
                riesgo = "Larva migrans en ninos. Prevalencia en perros 7-20% (INS).";
                medidas = "Desparasitar cada 3 meses. Evitar que ninos toquen suelo contaminado.";
        }

        Propietario propietario = new Propietario(0, nombrePropietario, direccion, ninos, embarazada);
        Mascota mascota = new Mascota(0, nombreMascota, especie, edad, propietario);
        Parasito parasito = new Parasito(0, nombreParasito, riesgo, medidas);
        Diagnostico diagnostico = new Diagnostico(0, mascota, parasito, java.time.LocalDate.now().toString(), "Activo");

        String alerta = diagnostico.evaluarRiesgoHumano();

        JTextArea areaAlerta = new JTextArea(alerta);
        areaAlerta.setEditable(false);
        areaAlerta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaAlerta.setBackground(new Color(255, 248, 230));
        JScrollPane scroll = new JScrollPane(areaAlerta);
        scroll.setPreferredSize(new Dimension(480, 300));
        JOptionPane.showMessageDialog(this, scroll, "Alerta de Convivencia", JOptionPane.WARNING_MESSAGE);

        guardarEnBD(nombrePropietario, direccion, ninos, embarazada, nombreMascota, especie, edad, nombreParasito);
    }

    private void guardarEnBD(String nombreProp, String dir, boolean ninos, boolean embarazada,
                             String nombreMascota, String especie, int edad, String parasito) {
        try (Connection con = ConexionDB.getConexion()) {

            // Insertar propietario
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

            // Insertar mascota
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

            // Obtener id del parásito desde la BD
            PreparedStatement psPar = con.prepareStatement("SELECT id FROM Parasitos WHERE nombre = ?");
            psPar.setString(1, parasito);
            ResultSet rsPar = psPar.executeQuery();
            int idPar = rsPar.next() ? rsPar.getInt(1) : 1;

            // Insertar diagnóstico
            PreparedStatement psDiag = con.prepareStatement(
                    "INSERT INTO Diagnosticos (id_mascota, id_parasito, fecha, estado_contagio) VALUES (?,?,date('now'),'Activo')");
            psDiag.setInt(1, idMasc);
            psDiag.setInt(2, idPar);
            psDiag.executeUpdate();

            JOptionPane.showMessageDialog(this, "Diagnostico guardado en vetsentimel.db", "Guardado", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verHistorial() {
        try (Connection con = ConexionDB.getConexion()) {
            String sql = "SELECT d.fecha, d.estado_contagio, " +
                    "m.nombre AS mascota, m.especie, " +
                    "p.nombre AS propietario, " +
                    "par.nombre AS parasito " +
                    "FROM Diagnosticos d " +
                    "JOIN Mascotas m ON d.id_mascota = m.id " +
                    "JOIN Propietarios p ON m.id_propietario = p.id " +
                    "JOIN Parasitos par ON d.id_parasito = par.id " +
                    "ORDER BY d.fecha DESC";

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            StringBuilder sb = new StringBuilder("HISTORIAL DE DIAGNOSTICOS\n");
            sb.append("─────────────────────────────────────────────────────\n");
            sb.append(String.format("%-12s %-12s %-10s %-20s %-15s\n", "Fecha", "Estado", "Mascota", "Parasito", "Propietario"));
            sb.append("─────────────────────────────────────────────────────\n");

            int contador = 0;
            while (rs.next()) {
                contador++;
                sb.append(String.format("%-12s %-12s %-10s %-20s %-15s\n",
                        rs.getString("fecha"),
                        rs.getString("estado_contagio"),
                        rs.getString("mascota") + "(" + rs.getString("especie") + ")",
                        rs.getString("parasito"),
                        rs.getString("propietario")));
            }

            if (contador == 0) sb.append("No hay registros aun.");

            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setFont(new Font("Monospaced", Font.PLAIN, 11));
            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(680, 300));
            JOptionPane.showMessageDialog(this, scroll, "Historial de Diagnosticos", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}