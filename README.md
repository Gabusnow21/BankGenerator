# QuizBank - Gestión de Cuestionarios Matemáticos

QuizBank es una aplicación Android diseñada para docentes que necesitan gestionar y exportar bancos de preguntas con soporte para fórmulas matemáticas (LaTeX). La aplicación sigue una arquitectura modular y utiliza componentes modernos de Android para garantizar el rendimiento y la escalabilidad.

##  Arquitectura Técnica

La aplicación está construida sobre una arquitectura **Layered Architecture** (Arquitectura por Capas), separando las responsabilidades de la siguiente manera:

### 1. Capa de Datos (Data Layer)
*   **Room Database**: Utiliza SQLite como motor de persistencia local. Define entidades como `DocenteEntity` y `PreguntaEntity`.
*   **DAOs**: Interfaces que definen las operaciones de acceso a datos de forma asíncrona.
*   **Repositories**: Actúan como una única fuente de verdad, mediando entre el acceso a la base de datos local y posibles fuentes externas.

### 2. Capa de Interfaz (UI Layer)
*   **ViewBinding**: Implementado para interactuar de forma segura y eficiente con las vistas XML, eliminando el uso de `findViewById`.
*   **RecyclerView + DiffUtil**: La visualización de preguntas utiliza `DiffUtil` para calcular cambios mínimos en la lista, optimizando el rendimiento del renderizado.
*   **Material Components (M3)**: Uso extensivo de Material Design 3 para una interfaz moderna y accesible (Chips, Extended FAB, Material Cards).

### 3. Capa de Exportación y Renderizado (Core/Export)
*   **LaTeX Rendering**: Utiliza un `WebView` configurado dinámicamente para renderizar fórmulas matemáticas mediante la librería **KaTeX**.
*   **Formatos de Exportación**:
    *   **JSON**: Para respaldo y portabilidad de datos.
    *   **Markdown**: Ideal para documentación técnica.
    *   **LaTeX**: Formato nativo para edición profesional en documentos académicos.
    *   **PDF**: Generado a través del sistema de impresión de Android (`PrintManager`) inyectando HTML renderizado con KaTeX.

## Tecnologías Utilizadas

*   **Lenguaje**: Java 11
*   **SDK Mínimo**: API 31 (Android 12)
*   **SDK Objetivo**: API 34 (Android 14)
*   **Base de Datos**: Room Persistence Library
*   **Auth**: Google Play Services Auth para integración de inicio de sesión.
*   **Exportación**: Google API Client para futura integración con Drive.

## Características Principales

1.  **Editor de Preguntas**: Soporte para sintaxis LaTeX (delimitado por `$`) con previsualización en tiempo real.
2.  **Sistema de Filtros**: Clasificación dinámica de preguntas por nivel o grado mediante Chips inteligentes.
3.  **Selección Multimodal**: Permite la selección individual o masiva de preguntas para operaciones de exportación personalizadas.
4.  **Generación de PDF**: Proceso automatizado que convierte el contenido digital en documentos listos para imprimir, manteniendo la fidelidad de las fórmulas matemáticas.

## Configuración del Desarrollo

Para compilar el proyecto:
1. Asegúrate de tener Android Studio Jellyfish o superior.
2. Gradle 8.0+
3. Conexión a internet para la carga de dependencias de KaTeX vía CDN (utilizado en la exportación a PDF).

---
Desarrollado con cariño para la comunidad educativa matematica salvadoreña.
