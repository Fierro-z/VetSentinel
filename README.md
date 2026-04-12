![Banner de VetSentinel](img/bannerProyecto.png)
# VetSentinel 🐾
**Prevención de enfermedades zoonóticas parasitarias: De la Clínica al Hogar.**

VetSentinel es un software en Java diseñado para registrar diagnósticos veterinarios y generar **alertas automáticas** para proteger la salud de los humanos que conviven con mascotas infectadas.

⚠️ **ESTADO DEL PROYECTO:** Este software está actualmente **en desarrollo** con fines académicos. Puede contener errores o funciones incompletas.

---

## 📖 Historias de Usuario

Las funcionalidades del sistema fueron definidas a partir de las necesidades reales de sus usuarios. A continuación se presentan las historias de usuario que guían el desarrollo de VetSentinel.

---

### Historia de Usuario 1 — Registro de diagnóstico y alerta de convivencia

| Campo | Descripción |
|---|---|
| **ID** | HU-01 |
| **Rol** | Veterinario clínico |
| **Enunciado** | Como **veterinario**, quiero **registrar el diagnóstico parasitario de una mascota junto con los datos del hogar del propietario**, para que el sistema **genere automáticamente una alerta de riesgo** que indique si algún humano conviviente (niños o mujeres embarazadas) está en peligro de contagio. |
| **Prioridad** | Alta |
| **Estado** | ✅ Implementada |

**Criterios de aceptación:**
- El veterinario puede ingresar el nombre, especie y edad de la mascota.
- El veterinario puede seleccionar el parásito diagnosticado entre las opciones disponibles (Toxoplasma gondii, Leishmania spp, Toxocara canis/cati).
- El veterinario puede indicar si en el hogar del propietario hay niños menores o mujeres embarazadas.
- Al guardar, el sistema evalúa la combinación parásito + factores del hogar y genera una **Alerta de Convivencia** con nivel de riesgo (CRÍTICO, ALTO, MEDIO o BAJO) y medidas preventivas específicas.
- La alerta se muestra en pantalla de forma inmediata, sin necesidad de pasos adicionales.

**Reglas de negocio implementadas (`Diagnostico.evaluarRiesgoHumano()`):**
- `Toxoplasma gondii` + Gato + Embarazada en el hogar → **NIVEL CRÍTICO** (riesgo de toxoplasmosis congénita).
- `Toxocara canis/cati` + Niños en el hogar → **NIVEL ALTO** (riesgo de larva migrans visceral, vigilar geofagia).
- `Leishmania spp` → **NIVEL ALTO** (el perro es reservorio; el vector Lutzomyia puede picar a humanos).

---

### Historia de Usuario 2 — Consulta del historial de diagnósticos

| Campo | Descripción |
|---|---|
| **ID** | HU-02 |
| **Rol** | Veterinario clínico |
| **Enunciado** | Como **veterinario**, quiero **consultar el historial completo de diagnósticos registrados en el sistema**, para poder **hacer seguimiento a los casos activos de zoonosis** atendidos en la clínica y verificar la información de cada propietario y mascota. |
| **Prioridad** | Alta |
| **Estado** | ✅ Implementada |

**Criterios de aceptación:**
- El veterinario puede acceder al historial desde la pantalla principal con un solo clic.
- El historial muestra todos los diagnósticos ordenados del más reciente al más antiguo.
- Cada registro incluye: fecha, estado del contagio, nombre y especie de la mascota, parásito diagnosticado y nombre del propietario.
- La información es persistente entre sesiones gracias al almacenamiento en base de datos SQLite local.

---

## ✨ Características Principales

* **Alertas Inteligentes:** Notifica riesgos específicos si en el hogar hay niños o mujeres embarazadas.
* **100% Portable:** No requiere instalación de servidores (funciona con **SQLite**).
* **Base Científica:** Recomendaciones basadas en datos del **INS Colombia BES SE26-2025**.
* **Fácil de Usar:** Interfaz gráfica creada con Java Swing.

---

## 🛠️ Tecnologías

* **Lenguaje:** Java 17/21
* **Base de datos:** SQLite (archivo local `.db` — no requiere servidor)
* **Interfaz:** Java Swing
* **Driver BD:** Xerial SQLite JDBC (`.jar` incluido en el proyecto)
* **Entorno:** Compatible con cualquier PC con Java instalado

---

## 📊 Estructura de la Base de Datos

El sistema organiza la información en 4 tablas relacionadas:

```
Parasitos        Propietarios
(id, nombre,     (id, nombre,
 riesgo,          direccion,
 medidas)         tiene_ninos,
                  hay_embarazadas)
      ↑                 ↑
      |                 |
   Diagnosticos ←── Mascotas
   (id,              (id, nombre,
    id_mascota,       especie,
    id_parasito,      edad,
    fecha,            id_propietario)
    estado_contagio)
```

---

## ▶️ Cómo ejecutar

1. Abre el proyecto en **IntelliJ IDEA**.
2. Agrega el `.jar` de SQLite JDBC en `File → Project Structure → Libraries`.
3. Ejecuta `Main.java`.
4. El archivo `vetsentimel.db` se crea automáticamente en la raíz del proyecto.

---

## 👨‍💻 Autores

* **Carlos Daniel Fierro** ([Fierro-z](https://github.com/Fierro-z))
* **Sebastian Osorio** ([OsOsorio79](https://github.com/OsOsorio79))

---
*Proyecto académico — Asignaturas: Proyecto Integrador 1 y Programación Orientada a Objetos (POO).*