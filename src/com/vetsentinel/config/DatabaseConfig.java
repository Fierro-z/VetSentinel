package com.vetsentinel.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String URL = "jdbc:sqlite:vetsentinel.db";

    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite no encontrado.", e);
        }
        return DriverManager.getConnection(URL);
    }

    public void inicializarBD() {
        try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Parasitos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL," +
                    "riesgo_principal TEXT," +
                    "medidas_preventivas TEXT," +
                    "alerta_embarazo INTEGER DEFAULT 0," +
                    "alerta_ninos INTEGER DEFAULT 0," +
                    "alerta_zona_rural INTEGER DEFAULT 0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Propietarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "cedula TEXT UNIQUE," +
                    "nombre TEXT NOT NULL," +
                    "direccion TEXT," +
                    "departamento TEXT DEFAULT 'No especificado'," +
                    "tiene_ninos INTEGER DEFAULT 0," +
                    "hay_embarazadas INTEGER DEFAULT 0," +
                    "numero_embarazos_previos INTEGER DEFAULT 0," +
                    "zona_rural INTEGER DEFAULT 0," +
                    "estrato INTEGER DEFAULT 1," +
                    "regimen TEXT DEFAULT 'Contributivo'," +
                    "altitud INTEGER DEFAULT 0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Mascotas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL," +
                    "especie TEXT," +
                    "edad INTEGER," +
                    "id_propietario INTEGER," +
                    "FOREIGN KEY (id_propietario) REFERENCES Propietarios(id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS Diagnosticos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "id_mascota INTEGER," +
                    "id_parasito INTEGER," +
                    "fecha TEXT," +
                    "estado_contagio TEXT," +
                    "nivel_riesgo TEXT," +
                    "FOREIGN KEY (id_mascota) REFERENCES Mascotas(id)," +
                    "FOREIGN KEY (id_parasito) REFERENCES Parasitos(id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS Usuarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL)");

            // Intentos seguros de migración
            try { stmt.execute("ALTER TABLE Diagnosticos ADD COLUMN nivel_riesgo TEXT"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Propietarios ADD COLUMN cedula TEXT"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Parasitos ADD COLUMN alerta_embarazo INTEGER DEFAULT 0"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Parasitos ADD COLUMN alerta_ninos INTEGER DEFAULT 0"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Parasitos ADD COLUMN alerta_zona_rural INTEGER DEFAULT 0"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Propietarios ADD COLUMN departamento TEXT DEFAULT 'No especificado'"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Propietarios ADD COLUMN zona_rural INTEGER DEFAULT 0"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Propietarios ADD COLUMN numero_embarazos_previos INTEGER DEFAULT 0"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Propietarios ADD COLUMN estrato INTEGER DEFAULT 1"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Propietarios ADD COLUMN regimen TEXT DEFAULT 'Contributivo'"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Propietarios ADD COLUMN altitud INTEGER DEFAULT 0"); } catch (SQLException ignore) {}

            // Eliminar parásitos antiguos que no correspondan
            stmt.execute("DELETE FROM Parasitos WHERE nombre NOT LIKE '%Toxoplasma%' AND nombre NOT LIKE '%Leishmania%' AND nombre NOT LIKE '%Toxocara%'");

            // Semilla de parásitos
            String[][] parasitosData = {
                {"Toxoplasmosis", "Infección por Toxoplasma gondii. Se transmite por heces fecales de gatos, carne mal cocida o agua contaminada. Riesgo crítico en gestantes.", "Evitar manipulación de arena de gato por gestantes y cocer bien las carnes.", "1", "0", "0"},
                {"Leishmaniasis Cutánea", "Enfermedad transmitida por la picadura del insecto Lutzomyia. Produce lesiones ulcerativas en la piel.", "Uso de repelentes, ropa de manga larga, toldillos y control de vectores.", "0", "1", "1"},
                {"Leishmaniasis Mucosa", "Afectación de las mucosas nasofaríngeas, causando lesiones destructivas secundarias.", "Tratamiento oportuno de la fase cutánea y control entomológico.", "0", "1", "1"},
                {"Leishmaniasis Visceral", "Enfermedad sistémica grave con afectación de bazo e hígado. Letalidad >95% sin tratamiento.", "Uso de toldillos, fumigación peridomiciliar y control de reservorios.", "0", "1", "1"},
                {"Toxocariasis", "Infección zoonótica común. Los niños se contagian al ingerir huevos presentes en suelos contaminados.", "Desparasitar mascotas periódicamente y evitar contacto con suelos de parques sospechosos.", "0", "1", "0"}
            };

            for (String[] pData : parasitosData) {
                String nombre = pData[0];
                String riesgo = pData[1];
                String medidas = pData[2];
                int alertaEmbarazo = Integer.parseInt(pData[3]);
                int alertaNinos = Integer.parseInt(pData[4]);
                int alertaZonaRural = Integer.parseInt(pData[5]);

                PreparedStatement psCheck = con.prepareStatement("SELECT id FROM Parasitos WHERE nombre = ?");
                psCheck.setString(1, nombre);
                ResultSet rsCheck = psCheck.executeQuery();

                if (rsCheck.next()) {
                    int id = rsCheck.getInt(1);
                    PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE Parasitos SET nombre = ?, riesgo_principal = ?, medidas_preventivas = ?, alerta_embarazo = ?, alerta_ninos = ?, alerta_zona_rural = ? WHERE id = ?");
                    psUpd.setString(1, nombre);
                    psUpd.setString(2, riesgo);
                    psUpd.setString(3, medidas);
                    psUpd.setInt(4, alertaEmbarazo);
                    psUpd.setInt(5, alertaNinos);
                    psUpd.setInt(6, alertaZonaRural);
                    psUpd.setInt(7, id);
                    psUpd.executeUpdate();
                } else {
                    PreparedStatement psIns = con.prepareStatement(
                        "INSERT INTO Parasitos (nombre, riesgo_principal, medidas_preventivas, alerta_embarazo, alerta_ninos, alerta_zona_rural) VALUES (?, ?, ?, ?, ?, ?)");
                    psIns.setString(1, nombre);
                    psIns.setString(2, riesgo);
                    psIns.setString(3, medidas);
                    psIns.setInt(4, alertaEmbarazo);
                    psIns.setInt(5, alertaNinos);
                    psIns.setInt(6, alertaZonaRural);
                    psIns.executeUpdate();
                }
            }

            // Semilla de usuarios por defecto
            var rsUsr = stmt.executeQuery("SELECT COUNT(*) FROM Usuarios");
            int userCount = rsUsr.next() ? rsUsr.getInt(1) : 0;
            if (userCount == 0) {
                stmt.execute("INSERT INTO Usuarios (username, password) VALUES ('admin', 'admin123')");
                stmt.execute("INSERT INTO Usuarios (username, password) VALUES ('estado', 'estado123')");
            } else {
                // Asegurar que 'estado' esté registrado por si acaso
                try { stmt.execute("INSERT OR IGNORE INTO Usuarios (username, password) VALUES ('estado', 'estado123')"); } catch (SQLException ignore) {}
            }

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }
}
