![Banner de VetSentinel](img/bannerProyecto.png)
# VetSentinel 🐾
**Prevención de enfermedades zoonóticas parasitarias: De la Clínica al Hogar.**

VetSentinel es un software en Java diseñado para registrar diagnósticos veterinarios y generar **alertas automáticas** para proteger la salud de los humanos que conviven con mascotas infectadas.

> [!WARNING]
> ⚠️ Este software está actualmente **en desarrollo** con fines académicos. Puede contener errores o funciones incompletas.

---

## 📖 Historias de Usuario

Las funcionalidades del sistema fueron definidas a partir de las necesidades reales de sus usuarios.

**HU-01 — Registro de diagnóstico y alerta de convivencia**
> Como **veterinario**, quiero **registrar el diagnóstico parasitario de una mascota junto con los datos del hogar del propietario**, para que el sistema **genere automáticamente una alerta de riesgo** que indique si algún humano conviviente está en peligro de contagio.
* ✅ El veterinario ingresa nombre, especie y edad de la mascota.
* ✅ El veterinario selecciona el parásito diagnosticado.
* ✅ El sistema evalúa dinámicamente si hay niños o embarazadas en el hogar y genera una **Alerta de Convivencia** con nivel de riesgo (CRÍTICO, ALTO, MEDIO o BAJO).

**HU-02 — Consulta del historial de diagnósticos**
> Como **veterinario**, quiero **consultar el historial completo de diagnósticos registrados**, para poder **hacer seguimiento a los casos activos de zoonosis** atendidos en la clínica.
* ✅ El historial muestra todos los casos ordenados del más reciente al más antiguo.
* ✅ Cada registro incluye: fecha, nivel de riesgo, cédula, mascota, parásito diagnosticado y propietario.

**HU-03 — Búsqueda y autocompletado de pacientes**
> Como **recepcionista/veterinario**, quiero **buscar a un propietario por su documento (cédula)**, para **autocompletar sus datos y evitar registros duplicados** en el sistema.
* ✅ Búsqueda en tiempo real conectada a la base de datos.
* ✅ Autocompletado de nombre, dirección y factores de riesgo (niños/embarazadas).
* ✅ Lógica *Upsert* (Actualizar si existe, Insertar si es nuevo) para mantener la integridad referencial.

**HU-04 — Dashboard Epidemiológico**
> Como **analista del INS (Instituto Nacional de Salud)**, quiero **ver un resumen estadístico de los casos**, para **identificar los parásitos predominantes y el volumen de casos críticos**.
* ✅ Cálculo automático del total de mascotas evaluadas.
* ✅ Conteo de diagnósticos en nivel "CRÍTICO".
* ✅ Identificación del parásito con mayor prevalencia en la clínica.

---

## ✨ Características Principales
* **Diseño Moderno y Adaptable (UI/UX):** Interfaz gráfica responsiva con soporte nativo para **Modo Claro ☀️ y Modo Oscuro 🌙** en tiempo real.
* **Alertas Inteligentes impulsadas por BD:** Las reglas de riesgo clínico (ej. peligro para embarazadas) no están *hardcodeadas* en Java, sino que se leen dinámicamente desde la base de datos, permitiendo escalar el sistema fácilmente.
* **Evita Duplicados (Integridad de Datos):** Sistema inteligente de validación por Cédula que vincula historiales a dueños existentes.
* **100% Portable:** No requiere instalación de servidores (funciona con **SQLite** con migraciones automáticas).
* **Base Científica:** Recomendaciones basadas en datos del **INS Colombia BES SE26-2025**.

## 🛠️ Tecnologías
* **Lenguaje:** Java 17/21.
* **Interfaz Gráfica:** Java Swing (Diseño customizado sin librerías externas).
* **Base de datos:** SQLite (Archivo local `.db`).
* **Driver BD:** Xerial SQLite JDBC.
* **Patrones aplicados:** POO, separación de responsabilidades (Lógica de BD encapsulada).

## 📊 Estructura de Datos
El sistema organiza y relaciona la información en:
1. **Propietarios:** Almacena Cédula (UNIQUE), nombre, dirección y factores de riesgo en casa.
2. **Mascotas:** Datos clínicos (edad, especie) vinculados a un Propietario (FK).
3. **Parásitos:** Catálogo de enfermedades que incluye banderas lógicas (`alerta_embarazo`, `alerta_ninos`) y medidas preventivas.
4. **Diagnósticos:** Historial transaccional que une Mascota, Parásito, Fecha y Nivel de Riesgo calculado.

## 🦠 Glosario de Enfermedades Parasitarias

### 🐱 Toxoplasmosis
Infección causada por el parásito *Toxoplasma gondii*. Se transmite por heces de gatos, carne mal cocida o alimentos/agua contaminados. Es muy común y peligrosa especialmente para mujeres embarazadas porque puede afectar al bebé.

### 🦟 Leishmaniasis
Enfermedad parasitaria transmitida por la picadura de un insecto. En Colombia es frecuente la forma cutánea, que produce lesiones en la piel y es común en zonas rurales y selváticas.

### 🐶 Toxocariasis
Infección causada por parásitos de perros y gatos. Las personas se contagian al ingerir huevos presentes en suelo contaminado, especialmente en parques. Es común en niños.

---

## ▶️ Cómo ejecutar

Asegúrate de tener Java 17 o superior instalado antes de ejecutar el proyecto.

1. Abre el proyecto en tu IDE preferido (**IntelliJ IDEA** recomendado).
2. Agrega el `.jar` de SQLite JDBC en las librerías del proyecto (`File → Project Structure → Libraries`).
3. Ejecuta la clase `Main.java`.
4. El archivo `vetsentimel.db` se creará y configurará automáticamente en la raíz del proyecto.

---

## 👨‍💻 Autores
* **Carlos Daniel Fierro** ([Fierro-z](https://github.com/Fierro-z))
* **Sebastian Osorio** ([OsOsorio79](https://github.com/OsOsorio79))

---

> [!NOTE]
> Proyecto académico para las asignaturas de **Proyecto Integrador 1** y **Programación Orientada a Objetos 2 (POO2)**.
