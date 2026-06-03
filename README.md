![Banner de VetSentinel](resources/img/bannerProyecto.png)
# VetSentinel 🐾: Arquitectura Integral para la Prevención de Zoonosis Parasitarias
**Prevención de enfermedades zoonóticas parasitarias: De la Clínica al Hogar.**

> [!WARNING]
> ⚠️ Este software está actualmente **en desarrollo** con fines académicos. Puede contener errores o funciones incompletas.

---

## ✨ Características Destacadas

VetSentinel ofrece una experiencia de usuario intuitiva y funcionalidades clave para la gestión y prevención de zoonosis:

*   **Interfaz Amigable:** Diseño adaptable a diferentes pantallas y una navegación fluida entre módulos.
*   **Gestión Clínica Integral:** Permite el registro ágil de mascotas y propietarios, la emisión de diagnósticos parasitarios y la generación automática de alertas preventivas.
*   **Vigilancia Epidemiológica:** Un panel de control para autoridades de salud pública que compila hallazgos clínicos, mostrando mapas de riesgo a nivel departamental y reportes estadísticos.
*   **Detección de Riesgos:** Identificación automática de riesgos zoonóticos en hogares con niños o mujeres embarazadas, activando protocolos de acción específicos.
*   **Información en Tiempo Real:** Integración con datos epidemiológicos nacionales (como el Boletín del INS de Colombia) para una evaluación de riesgo precisa y actualizada.

---

## 🌍 Nuestra Visión: Una Salud para Todos

VetSentinel va más allá de la gestión clínica. Creemos en el concepto "Una Salud" (*One Health*), donde la salud animal, humana y ambiental están interconectadas. Nuestra plataforma convierte cada diagnóstico veterinario en un punto de inteligencia para la salud pública, ayudando a prevenir brotes de enfermedades zoonóticas antes de que afecten a las personas. Al integrar datos del Instituto Nacional de Salud (INS) de Colombia, VetSentinel asegura que cada acción contribuya a la resiliencia sanitaria del país.

---

## 🚨 Sistema de Alertas Inteligente

El corazón de VetSentinel es su capacidad para evaluar y clasificar el riesgo de enfermedades zoonóticas en tiempo real. El sistema considera el tipo de parásito diagnosticado y la vulnerabilidad del hogar (presencia de niños o mujeres embarazadas) para generar alertas claras:

*   🚨 **CRÍTICO:** Riesgo muy alto, requiere acción inmediata (ej. Leishmaniasis en áreas endémicas o en hogares con niños pequeños).
*   ⚠️ **ALTO:** Riesgo significativo, se necesitan medidas preventivas (ej. Parásitos zoonóticos en hogares con mujeres gestantes).
*   ⚠️ **MEDIO:** Riesgo moderado, se recomienda vigilancia y precauciones.
*   ✅ **BAJO:** Riesgo mínimo, controles rutinarios.

Este sistema ayuda a los veterinarios a tomar decisiones informadas y a las autoridades de salud a actuar proactivamente.

---

## 🦠 Glosario de Enfermedades Parasitarias

### 🐱 Toxoplasmosis
Infección causada por el parásito *Toxoplasma gondii*. Se transmite por heces de gatos, carne mal cocida o alimentos/agua contaminados. Es muy común y peligrosa especialmente para mujeres embarazadas porque puede afectar al bebé.

### 🦟 Leishmaniasis
Enfermedad parasitaria transmitida por la picadura de un insecto. En Colombia es frecuente la forma cutánea, que produce lesiones en la piel y es común en zonas rurales y selváticas.

### 🐶 Toxocariasis
Infección causada por parásitos de perros y gatos. Las personas se contagian al ingerir huevos presentes en suelo contaminado, especialmente en parques. Es común en niños.

---

## 🛠️ Tecnologías Utilizadas
*   **Lenguaje:** Java 17/21.
*   **Interfaz Gráfica:** Java Swing (Diseño personalizado).
*   **Base de datos:** SQLite (Archivo local `.db`).

---

## ▶️ Cómo Ejecutar el Proyecto

Para poner en marcha VetSentinel, sigue estos pasos detallados:

### Requisitos Previos
Asegúrate de tener instalado **Java Development Kit (JDK) versión 17 o superior** en tu sistema. Puedes descargarlo desde el sitio oficial de Oracle o a través de tu gestor de paquetes preferido.

### Paso 1: Descargar el Conector JDBC de SQLite

VetSentinel utiliza SQLite para su base de datos local. Necesitarás el conector JDBC para Java:
1.  Descarga el archivo `.jar` del conector JDBC de SQLite desde el repositorio oficial de Maven Central: [SQLite JDBC Driver](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar) (o busca la versión más reciente).
2.  Guarda este archivo `.jar` en una carpeta llamada `lib/` dentro de la raíz de tu proyecto VetSentinel. Si la carpeta `lib/` no existe, créala.

### Paso 2: Abrir el Proyecto en tu IDE

Puedes usar tu Entorno de Desarrollo Integrado (IDE) preferido. Recomendamos **IntelliJ IDEA** o **VS Code**.

#### Opción A: IntelliJ IDEA
1.  Abre IntelliJ IDEA.
2.  Selecciona `File` > `Open...` y navega hasta la carpeta raíz de tu proyecto VetSentinel.
3.  Una vez abierto el proyecto, necesitas añadir el JAR de SQLite como una librería del proyecto:
    *   Ve a `File` > `Project Structure...` (o presiona `Ctrl+Alt+Shift+S`).
    *   En el panel izquierdo, selecciona `Libraries`.
    *   Haz clic en el botón `+` y elige `Java`.
    *   Navega hasta la carpeta `lib/` dentro de tu proyecto y selecciona el archivo `sqlite-jdbc-X.X.X.jar` que descargaste (ej. `sqlite-jdbc-3.45.1.0.jar`).
    *   Haz clic en `OK` y luego en `Apply` y `OK` para cerrar las ventanas.

#### Opción B: VS Code
1.  Abre VS Code.
2.  Selecciona `File` > `Open Folder...` y navega hasta la carpeta raíz de tu proyecto VetSentinel.
3.  VS Code con las extensiones de Java (como "Extension Pack for Java") debería detectar automáticamente el archivo `.jar` en la carpeta `lib/` y configurarlo como una dependencia. Si no es así, puedes añadirlo manualmente al `CLASSPATH` o configurar tu `settings.json` para incluir la carpeta `lib/`.

### Paso 3: Ejecutar la Aplicación

1.  Localiza el archivo `Main.java` en la ruta `src/com/vetsentinel/Main.java`.
2.  Haz clic derecho sobre `Main.java` y selecciona `Run 'Main.main()'`.
3.  La aplicación se iniciará y el archivo de base de datos `vetsentinel.db` se creará y configurará automáticamente en la raíz de tu proyecto la primera vez que la ejecutes.

¡Listo! Ya puedes empezar a usar VetSentinel.

---

## 👨‍💻 Autores
*   **Carlos Daniel Fierro** ([Fierro-z](https://github.com/Fierro-z))
*   **Sebastian Osorio** ([OsOsorio79](https://github.com/OsOsorio79))

> [!NOTE]
> Proyecto académico para las asignaturas de **Proyecto Integrador 1** y **Programación Orientada a Objetos 2 (POO2)**.
