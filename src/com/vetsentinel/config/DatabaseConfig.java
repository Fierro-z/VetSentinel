package com.vetsentinel.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String URL = "jdbc:sqlite:vetsentinel.db";
    private final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();
    private final SimpleConnectionPool connectionPool = new SimpleConnectionPool(URL);

    public Connection getConnection() throws SQLException {
        Connection con = threadConnection.get();
        if (con != null && !con.isClosed()) {
            return (Connection) java.lang.reflect.Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, methodArgs) -> {
                    if (method.getName().equals("close")) {
                        return null; // Ignore close during transaction
                    }
                    try {
                        return method.invoke(con, methodArgs);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
            );
        }

        return connectionPool.getConnection();
    }

    public void iniciarTransaccion() throws SQLException {
        if (threadConnection.get() != null) {
            throw new SQLException("Una transacción ya está activa en este hilo.");
        }
        Connection con = connectionPool.getConnection();
        try {
            try (Statement stmt = con.createStatement()) {
                stmt.execute("PRAGMA journal_mode = WAL;");
                stmt.execute("PRAGMA busy_timeout = 5000;");
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            con.setAutoCommit(false);
            threadConnection.set(con);
        } catch (SQLException e) {
            try { con.close(); } catch (SQLException ignore) {}
            throw e;
        }
    }

    public void commitTransaccion() throws SQLException {
        Connection con = threadConnection.get();
        if (con == null) {
            throw new SQLException("No hay ninguna transacción activa.");
        }
        try {
            con.commit();
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ignore) {}
            try { con.close(); } catch (SQLException ignore) {}
            threadConnection.remove();
        }
    }

    public void rollbackTransaccion() {
        Connection con = threadConnection.get();
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException e) {
                com.vetsentinel.util.VetLogger.error("Error al hacer rollback", e);
            } finally {
                try { con.setAutoCommit(true); } catch (SQLException ignore) {}
                try { con.close(); } catch (SQLException ignore) {}
                threadConnection.remove();
            }
        }
    }

    public void inicializarBD() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            com.vetsentinel.util.VetLogger.error("Driver SQLite no encontrado", e);
            return;
        }
        try (Connection con = DriverManager.getConnection(URL);
             Statement stmt = con.createStatement()) {
                
            stmt.execute("PRAGMA foreign_keys = OFF"); // Desactivar al inicio
            stmt.execute("PRAGMA journal_mode = WAL");
            stmt.execute("PRAGMA busy_timeout = 5000");

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
                    "FOREIGN KEY (id_propietario) REFERENCES Propietarios(id)," +
                    "UNIQUE(nombre, id_propietario))");

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

            // Índices de optimización de consultas
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_propietarios_departamento ON Propietarios(departamento)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_mascotas_propietario ON Mascotas(id_propietario)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_diagnosticos_mascota ON Diagnosticos(id_mascota)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_diagnosticos_parasito ON Diagnosticos(id_parasito)");

            // Intentos seguros de migración
            try { stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_mascotas_unique_nombre_prop ON Mascotas(nombre, id_propietario)"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Diagnosticos ADD COLUMN nivel_riesgo TEXT"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Diagnosticos ADD COLUMN reporte TEXT"); } catch (SQLException ignore) {}
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

            // NOTA: Esta limpieza destructiva ha sido desactivada en producción para prevenir la pérdida de datos reales.
            // Si se requiere limpiar registros antiguos/legacy, ejecutar estos comandos como un script de migración manual.
            // stmt.execute("DELETE FROM Diagnosticos WHERE id_parasito NOT IN (SELECT id FROM Parasitos WHERE nombre LIKE '%Toxoplasma%' OR nombre LIKE '%Leishmania%' OR nombre LIKE '%Toxocara%')");
            // stmt.execute("DELETE FROM Parasitos WHERE nombre NOT LIKE '%Toxoplasma%' AND nombre NOT LIKE '%Leishmania%' AND nombre NOT LIKE '%Toxocara%'");

            // Semilla de parásitos
            String[][] parasitosData = {
                {"Toxoplasmosis", "Infección por Toxoplasma gondii. Se transmite por heces fecales de gatos, carne mal cocida o agua contaminada. Riesgo crítico en gestantes.", "Evitar manipulación de arena de gato por gestantes y cocer bien las carnes.", "1", "0", "0"},
                {"Leishmaniasis", "Enfermedad parasitaria transmitida por la picadura de flebótomos (insecto Lutzomyia). Puede presentarse en forma cutánea, mucosa o visceral.", "Uso de repelentes, ropa de manga larga, toldillos y control de vectores.", "0", "1", "1"},
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

                try (PreparedStatement psCheck = con.prepareStatement("SELECT id FROM Parasitos WHERE nombre = ?")) {
                    psCheck.setString(1, nombre);
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next()) {
                            int id = rsCheck.getInt(1);
                            try (PreparedStatement psUpd = con.prepareStatement(
                                "UPDATE Parasitos SET nombre = ?, riesgo_principal = ?, medidas_preventivas = ?, alerta_embarazo = ?, alerta_ninos = ?, alerta_zona_rural = ? WHERE id = ?")) {
                                psUpd.setString(1, nombre);
                                psUpd.setString(2, riesgo);
                                psUpd.setString(3, medidas);
                                psUpd.setInt(4, alertaEmbarazo);
                                psUpd.setInt(5, alertaNinos);
                                psUpd.setInt(6, alertaZonaRural);
                                psUpd.setInt(7, id);
                                psUpd.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement psIns = con.prepareStatement(
                                "INSERT INTO Parasitos (nombre, riesgo_principal, medidas_preventivas, alerta_embarazo, alerta_ninos, alerta_zona_rural) VALUES (?, ?, ?, ?, ?, ?)")) {
                                psIns.setString(1, nombre);
                                psIns.setString(2, riesgo);
                                psIns.setString(3, medidas);
                                psIns.setInt(4, alertaEmbarazo);
                                psIns.setInt(5, alertaNinos);
                                psIns.setInt(6, alertaZonaRural);
                                psIns.executeUpdate();
                            }
                        }
                    }
                }
            }

            // Migración: Eliminar contraseñas legacy en texto plano (que no tienen formato 'salt:hash')
            stmt.execute("DELETE FROM Usuarios WHERE password NOT LIKE '%:%'");

            // Semilla de usuarios por defecto
            var rsUsr = stmt.executeQuery("SELECT COUNT(*) FROM Usuarios");
            int userCount = rsUsr.next() ? rsUsr.getInt(1) : 0;

            String adminPass = System.getenv("VETSENTINEL_ADMIN_PASS");
            String estadoPass = System.getenv("VETSENTINEL_ESTADO_PASS");

            if (adminPass == null || estadoPass == null) {
                java.io.File configFile = new java.io.File("config.properties");
                if (configFile.exists()) {
                    java.util.Properties props = new java.util.Properties();
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(configFile)) {
                        props.load(fis);
                        if (adminPass == null) adminPass = props.getProperty("admin.password");
                        if (estadoPass == null) estadoPass = props.getProperty("estado.password");
                    } catch (java.io.IOException e) {
                        com.vetsentinel.util.VetLogger.error("Error al leer archivo config.properties", e);
                    }
                }
            }

            if (adminPass == null || adminPass.trim().isEmpty()) {
                adminPass = "admin123";
            }
            if (estadoPass == null || estadoPass.trim().isEmpty()) {
                estadoPass = "estado123";
            }

            if (userCount == 0) {
                String hashedAdmin = com.vetsentinel.util.PasswordHasher.hash(adminPass);
                String hashedEstado = com.vetsentinel.util.PasswordHasher.hash(estadoPass);
                
                try (PreparedStatement psAdmin = con.prepareStatement("INSERT INTO Usuarios (username, password) VALUES ('admin', ?)")) {
                    psAdmin.setString(1, hashedAdmin);
                    psAdmin.executeUpdate();
                }
                try (PreparedStatement psEstado = con.prepareStatement("INSERT INTO Usuarios (username, password) VALUES ('estado', ?)")) {
                    psEstado.setString(1, hashedEstado);
                    psEstado.executeUpdate();
                }
            } else {
                // Asegurar que 'estado' esté registrado por si acaso
                try (PreparedStatement psCheck = con.prepareStatement("SELECT COUNT(*) FROM Usuarios WHERE username = 'estado'")) {
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                            String hashedEstado = com.vetsentinel.util.PasswordHasher.hash(estadoPass);
                            try (PreparedStatement psEstado = con.prepareStatement("INSERT INTO Usuarios (username, password) VALUES ('estado', ?)")) {
                                psEstado.setString(1, hashedEstado);
                                psEstado.executeUpdate();
                            }
                        }
                    }
                }
            }
            
            stmt.execute("PRAGMA foreign_keys = ON"); // Reactivar al final
        } catch (SQLException e) {
            com.vetsentinel.util.VetLogger.error("Error al inicializar la base de datos", e);
        }
    }
}
