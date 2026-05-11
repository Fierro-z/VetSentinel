![Banner de VetSentinel](img/bannerProyecto.png)
# VetSentinel 🐾: Arquitectura Integral para la Prevención de Zoonosis Parasitarias
**Prevención de enfermedades zoonóticas parasitarias: De la Clínica al Hogar.**

> [!WARNING]
> ⚠️ Este software está actualmente **en desarrollo** con fines académicos. Puede contener errores o funciones incompletas.

---

## ✨ Novedades Recientes (UI/UX)
* **Diseño Responsivo:** Adaptación dinámica a múltiples resoluciones y pantallas mediante proporciones rebalanceadas y barras de desplazamiento (scroll) personalizadas.
* **Toggle Cards Interactivas:** Rediseño del panel de *Factores de Riesgo* con tarjetas seleccionables modernas que brindan retroalimentación visual inmediata.
* **Navegación Fluida:** Incorporación de botones de retorno sutiles para cambiar rápidamente entre el *Módulo Clínico* y el *Módulo del Estado*.
* **Empty States:** Retroalimentación visual elegante ("A la espera de diagnóstico") en los paneles de análisis para guiar al usuario.
* **Gestión de Información Médica:** Mejora sustancial en la captura de datos clínicos, incorporando georreferenciación por departamentos, visualización avanzada del historial de diagnósticos y generación de estadísticas directas.

---

## ⚙️ Arquitectura Modular
El sistema opera a través de dos interfaces independientes pero conectadas a una misma inteligencia de datos local:
* **🏥 Módulo Clínico Veterinario:** Entorno de atención médica para el registro ágil de mascotas y propietarios, emisión de diagnósticos y despliegue automático de alertas preventivas en tiempo real.
* **🏛️ Módulo de Salud Pública (Estado):** Dashboard de vigilancia epidemiológica gubernamental que compila los hallazgos clínicos, mostrando mapas de riesgo a nivel departamental y reportes estadísticos de cepas zoonóticas.

---

## 🌍 1. Visión Estratégica: La Convergencia de la Clínica Veterinaria y la Salud Pública

VetSentinel no es simplemente un software de gestión clínica; es un nodo periférico de inteligencia dentro del ecosistema de vigilancia epidemiológica nacional. Bajo el paradigma de "Una Salud" (*One Health*), la plataforma reconoce que el consultorio veterinario representa la primera línea de defensa contra brotes humanos. La transición de datos "de la clínica al hogar" permite interceptar patógenos antes de que se manifiesten en la población humana, transformando un hallazgo microscópico en una intervención ambiental oportuna. 

Al integrar los datos del **Boletín Epidemiológico Semanal (BES) de la Semana 26 de 2025** del **Instituto Nacional de Salud (INS)**, VetSentinel valida su lógica algorítmica con la realidad biológica y geográfica de Colombia, asegurando que cada diagnóstico veterinario contribuya a la resiliencia sanitaria del territorio.

Tras establecer esta visión, es imperativo definir las reglas operativas que transforman datos brutos en alertas de vida o muerte.

---

## 📖 2. Definición de Historias de Usuario y Lógica de Negocio (Backend Logic)

La arquitectura de VetSentinel se estructura a través de Historias de Usuario (HU) que trascienden la operatividad administrativa para enfocarse en el impacto clínico y preventivo. El sistema no solo almacena registros; procesa indicadores de riesgo zoonótico en tiempo real.

### Impacto Operativo de las Historias de Usuario

| Funcionalidad | Impacto en Salud Pública |
| --- | --- |
| **HU-01: Registro Dinámico** | Captura factores sociodemográficos para el rastreo de vectores, previniendo brotes intradomiciliarios. |
| **HU-02: Diagnóstico Parasitario** | Identifica patógenos de interés en vigilancia (ej. *Leishmania spp.*) antes del contagio humano. |
| **HU-03: Upsert de Propietarios** | Garantiza la integridad de la historia epidemiológica del hogar, actualizando riesgos según cambios en la convivencia. |
| **HU-04: Alertas de Convivencia** | Traduce hallazgos técnicos en acciones preventivas críticas para grupos vulnerables (niños y gestantes). |

### Estratificación de Riesgo y Protocolos de Acción

El motor de reglas de VetSentinel evalúa "banderas lógicas" cruzando el diagnóstico del parásito con la vulnerabilidad del hogar. Pathógenos como *Toxocara* o *Toxoplasma* se gestionan como entidades configurables, mientras que eventos vectoriales detectados por el INS dictan niveles de alerta específicos:

* 🚨 **CRÍTICO:** Hallazgo de Leishmania en mascotas de áreas endémicas (ej. Sucre) o en hogares con niños menores de 5 años. **Acción:** Notificación obligatoria a SIVIGILA (Código INS 220), estudio entomológico del foco y remisión inmediata a pediatría preventiva.
* ⚠️ **ALTO:** Detección de parásitos zoonóticos en hogares con mujeres gestantes. **Acción:** Protocolo de manejo de excretas y comunicación de riesgo sobre transmisión vertical.
* ⚠️ **MEDIO:** Presencia de vectores en zonas rurales con incidencias superiores a la nacional. **Acción:** Intervención en el entorno peridomiciliario y uso de repelentes específicos.
* ✅ **BAJO:** Controles rutinarios negativos en áreas urbanas de baja transmisión.

Una vez definida la interacción del usuario, debemos estructurar el cerebro del sistema: la base de datos.

---

## 📊 3. Arquitectura de Datos y Persistencia en SQLite

Para garantizar la operatividad en zonas rurales dispersas o clínicas de baja complejidad tecnológica, se ha implementado una persistencia basada en **SQLite**. Esta elección técnica prioriza la portabilidad, permitiendo que el sistema funcione sin dependencia de servidores centrales en áreas de conectividad limitada.

### Modelado de Entidades y Esquema Técnico

El esquema está diseñado para evitar el *hardcoding* mediante el uso de atributos dinámicos en la tabla de patógenos, permitiendo que el sistema escale sin modificaciones en el código fuente de Java.

```sql
-- Tabla Propietarios: Estructura sociodemográfica y vulnerabilidades
CREATE TABLE Propietarios (
    cedula_id INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL,
    residencia_departamento TEXT NOT NULL,
    tiene_ninos BOOLEAN DEFAULT 0,
    tiene_embarazadas BOOLEAN DEFAULT 0
);

-- Tabla Mascotas: Vínculo epidemiológico
CREATE TABLE Mascotas (
    mascota_id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT,
    especie TEXT,
    propietario_id INTEGER,
    FOREIGN KEY(propietario_id) REFERENCES Propietarios(cedula_id)
);

-- Tabla Parasitos: Diccionario de metadatos de riesgo
CREATE TABLE Parasitos (
    parasito_id INTEGER PRIMARY KEY,
    nombre_cientifico TEXT NOT NULL,
    alerta_embarazo BOOLEAN, -- Banderas para lógica de negocio
    alerta_ninos BOOLEAN,
    gravedad_base TEXT -- CRÍTICO, ALTO, MEDIO
);

-- Tabla Diagnosticos: Registro longitudinal de eventos
CREATE TABLE Diagnosticos (
    diagnostico_id INTEGER PRIMARY KEY,
    mascota_id INTEGER,
    parasito_id INTEGER,
    fecha_registro DATE,
    FOREIGN KEY(mascota_id) REFERENCES Mascotas(mascota_id),
    FOREIGN KEY(parasito_id) REFERENCES Parasitos(parasito_id)
);
```

### Integridad Referencial mediante "Upsert"

Se implementa la lógica de **Upsert** (Update or Insert) en la tabla `Propietarios` para asegurar que el perfil de riesgo del hogar sea dinámico. Si un propietario con una cédula existente regresa a consulta, el sistema actualiza sus condiciones de vivienda (ej. detección de un nuevo embarazo), manteniendo la integridad referencial sin generar redundancia.

Con la estructura de datos lista, el sistema debe ser alimentado con la realidad epidemiológica de Colombia.

---

## 📈 4. Análisis Epidemiológico: Integración de Datos INS 2025

*(Contexto del Boletín SE26)*

La eficacia diagnóstica de VetSentinel se sustenta en la integración de los datos del **Boletín Epidemiológico Semanal (SE26-2025)**. La situación de las enfermedades vectoriales en Colombia exige que el software actúe con una sensibilidad geográfica diferenciada.

### Análisis de la Leishmaniasis en el Territorio

La incidencia nacional de leishmaniasis cutánea es de 29,57 por 100.000 habitantes en riesgo. El sistema debe priorizar diagnósticos en el 82,7% de los casos que ocurren en áreas rurales y en la población masculina (72,5% de los afectados).

* **Estratificación de Riesgo Geográfico:** VetSentinel elevará automáticamente el nivel de alerta si el propietario reside en departamentos con incidencias críticas que superan la media nacional: Boyacá (157,49), Cesar (97,30), Santander (93,17) y Caldas (77,56).
* **Vigilancia Visceral y Pediátrica:** El BES-2025 reporta casos confirmados en Ovejas y Chalán (Sucre) que involucraron a un niño de 5 años y una niña de 6 meses. Dada la letalidad potencial del 95% sin tratamiento, el software clasifica cualquier hallazgo de *Leishmania* en entornos con menores de 10 años (donde Risaralda presenta un 40% de transmisión domiciliaria) como **RIESGO CRÍTICO**.
* **Contexto de Intervención:** Aunque la letalidad en leishmaniasis visceral es del 0% a la fecha en 2025, la agresividad de la variante exige que el sistema dispare protocolos de notificación inmediata para evitar desenlaces fatales en la población pediátrica.

Estos datos no son estáticos; deben integrarse mediante código eficiente para transformar la aplicación en una herramienta de decisión.

---

## 🦠 Glosario de Enfermedades Parasitarias

### 🐱 Toxoplasmosis
Infección causada por el parásito *Toxoplasma gondii*. Se transmite por heces de gatos, carne mal cocida o alimentos/agua contaminados. Es muy común y peligrosa especialmente para mujeres embarazadas porque puede afectar al bebé.

### 🦟 Leishmaniasis
Enfermedad parasitaria transmitida por la picadura de un insecto. En Colombia es frecuente la forma cutánea, que produce lesiones en la piel y es común en zonas rurales y selváticas.

### 🐶 Toxocariasis
Infección causada por parásitos de perros y gatos. Las personas se contagian al ingerir huevos presentes en suelo contaminado, especialmente en parques. Es común en niños.

---

## 🤖 5. Guía de Implementación Técnica y Prompts

Como arquitectos, utilizamos modelos de lenguaje para optimizar el ciclo de desarrollo, asegurando que la lógica de salud pública se traduzca en código escalable.

**Generación de Migraciones SQL**
> "Actúa como un Database Architect. Genera el script SQL para SQLite de VetSentinel. Incluye restricciones de integridad referencial y utiliza la cláusula ON CONFLICT(cedula_id) DO UPDATE para manejar el perfil sociodemográfico dinámico de los propietarios. Asegúrate de que la tabla 'Parasitos' incluya los campos booleanos de alerta para gestantes y niños."

**Lógica de Cálculo de Riesgo (Java Pattern)**
> "Como Senior Java Developer, implementa un 'RiskAssessmentService' utilizando el Strategy Pattern. Define una interfaz 'RiskStrategy' y clases concretas para evaluar riesgos basados en patógenos zoonóticos y vulnerabilidad del hogar (embarazo/niños). El método principal debe recibir un 'Propietario' y un 'Diagnostico', retornando un objeto 'AlertResult' con el nivel de riesgo (CRÍTICO, ALTO, etc.) y la referencia bibliográfica obligatoria al INS 2025."

**Desarrollo de la Interfaz Swing (Reactiva)**
> "Genera el código para 'VentanaVeterinaria.java' utilizando Java Swing. Implementa un FocusListener en el JTextField de 'cedula_id' para que, al perder el foco, el sistema realice una consulta asíncrona a la base de datos y autocomplete los campos del propietario. Si el propietario ya existe, debe resaltar visualmente si tiene banderas de riesgo activas (gestantes/niños)."

El sistema está diseñado para ser alimentado mediante estos componentes modulares, facilitando su mantenimiento.

---

## 🚀 6. Conclusiones y Recomendaciones de Escalabilidad

VetSentinel representa un salto cualitativo en la informática de salud veterinaria al convertir la consulta privada en un sensor de salud pública. La integración de los datos del BES-2025 no es opcional; es el motor que otorga validez científica a la herramienta.

**Recomendaciones Técnicas y Científicas:**
* **Vigilancia Multievento:** Se recomienda expandir el sistema para incluir el monitoreo de Dengue, dada su incidencia nacional de 255,3 por 100.000 habitantes y que el 37% de los casos presentan signos de alarma o gravedad. Asimismo, se debe integrar la alerta por Fiebre Amarilla, considerando los 116 casos confirmados y la alarmante letalidad del 43% en el último periodo.
* **Validación Basada en Evidencia:** Toda alerta generada en la interfaz de usuario debe incluir de forma obligatoria el pie de página: *"Riesgo basado en Boletín INS SE26-2025. Notificación obligatoria a SIVIGILA (Código INS 210/220) si aplica."*
* **Monitoreo de Tos Ferina:** Dada la incidencia inusual de 0,84 por 100k (un aumento del 100% respecto a periodos previos), el sistema debe alertar sobre contactos estrechos con mascotas en hogares con lactantes no vacunados.

La tecnología, aplicada con rigor epidemiológico, constituye el escudo más eficaz para la salud de las familias colombianas, consolidando el liderazgo de la medicina veterinaria en la prevención de crisis sanitarias.

---

## 🛠️ Tecnologías Adicionales
* **Lenguaje:** Java 17/21.
* **Interfaz Gráfica:** Java Swing (Diseño customizado sin librerías externas).
* **Base de datos:** SQLite (Archivo local `.db`).
* **Patrones aplicados:** POO, separación de responsabilidades, Arquitectura de Componentes.

## ▶️ Cómo ejecutar

Asegúrate de tener Java 17 o superior instalado antes de ejecutar el proyecto.

1. Abre el proyecto en tu IDE preferido (**IntelliJ IDEA** recomendado) o **VS Code**.
2. Agrega el `.jar` de SQLite JDBC en la carpeta `lib/` (creada automáticamente para VS Code) o configúralo en las librerías del proyecto en IntelliJ.
3. Ejecuta la clase `Main.java`.
4. El archivo `vetsentimel.db` se creará y configurará automáticamente en la raíz del proyecto.

---

## 👨‍💻 Autores
* **Carlos Daniel Fierro** ([Fierro-z](https://github.com/Fierro-z))
* **Sebastian Osorio** ([OsOsorio79](https://github.com/OsOsorio79))

> [!NOTE]
> Proyecto académico para las asignaturas de **Proyecto Integrador 1** y **Programación Orientada a Objetos 2 (POO2)**.
