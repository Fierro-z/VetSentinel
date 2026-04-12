![Banner de VetSentinel](img/bannerProyecto.png)
# VetSentinel 🐾
**Prevención de enfermedades zoonóticas parasitarias: De la Clínica al Hogar.**

VetSentinel es un software en Java diseñado para registrar diagnósticos veterinarios y generar **alertas automáticas** para proteger la salud de los humanos que conviven con mascotas infectadas.

> [!WARNING]
> **ESTADO DEL PROYECTO:** Este software está actualmente **en desarrollo** con fines académicos. Puede contener errores o funciones incompletas.

---

## 📖 Historias de Usuario

Las funcionalidades del sistema fueron definidas a partir de las necesidades reales de sus usuarios.

**HU-01 — Registro de diagnóstico y alerta de convivencia**

Como **veterinario**, quiero **registrar el diagnóstico parasitario de una mascota junto con los datos del hogar del propietario**, para que el sistema **genere automáticamente una alerta de riesgo** que indique si algún humano conviviente está en peligro de contagio.

* ✅ El veterinario ingresa nombre, especie y edad de la mascota.
* ✅ El veterinario selecciona el parásito diagnosticado.
* ✅ El sistema evalúa si hay niños o embarazadas en el hogar y genera una **Alerta de Convivencia** con nivel de riesgo (CRÍTICO, ALTO, MEDIO o BAJO).

**HU-02 — Consulta del historial de diagnósticos**

Como **veterinario**, quiero **consultar el historial completo de diagnósticos registrados**, para poder **hacer seguimiento a los casos activos de zoonosis** atendidos en la clínica.

* ✅ El historial muestra todos los casos ordenados del más reciente al más antiguo.
* ✅ Cada registro incluye: fecha, mascota, parásito diagnosticado y propietario.
* ✅ La información persiste entre sesiones gracias a SQLite.

---

## ✨ Características Principales
* **Alertas Inteligentes:** Notifica riesgos específicos si en el hogar hay niños o mujeres embarazadas.
* **100% Portable:** No requiere instalación de servidores (funciona con **SQLite**).
* **Base Científica:** Recomendaciones basadas en datos del **INS Colombia BES SE26-2025**.
* **Fácil de Usar:** Interfaz gráfica sencilla creada con Java Swing.

## 🛠️ Tecnologías
* **Lenguaje:** Java 17/21.
* **Base de datos:** SQLite (Archivo local `.db`).
* **Driver BD:** Xerial SQLite JDBC.
* **Entorno:** Compatible con cualquier PC con Java instalado.

## 📊 Estructura de Datos
El sistema organiza la información en:
1. **Propietarios** (Factores de riesgo en casa).
2. **Mascotas** (Datos clínicos).
3. **Parásitos** (Catálogo de enfermedades).
4. **Diagnósticos** (Historial de casos).

## ▶️ Cómo ejecutar

Asegúrate de tener Java 17 o superior instalado antes de ejecutar el proyecto.

1. Abre el proyecto en **IntelliJ IDEA**.
2. Agrega el `.jar` de SQLite JDBC en `File → Project Structure → Libraries`.
3. Ejecuta `Main.java`.
4. El archivo `vetsentimel.db` se crea automáticamente en la raíz del proyecto.

---

## 👨‍💻 Autores
* **Carlos Daniel Fierro** ([Fierro-z](https://github.com/Fierro-z))
* **Sebastian Osorio** ([OsOsorio79](https://github.com/OsOsorio79))

---

> [!NOTE]
> Proyecto académico para las asignaturas de **Proyecto Integrador 1** y **Programación Orientada a Objetos (POO)**.