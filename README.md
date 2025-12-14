# Organizador de Actividades Estudiantiles (App Móvil)

Este proyecto consiste en una aplicación móvil nativa para Android desarrollada en Kotlin, diseñada para ayudar a estudiantes a organizar sus actividades académicas, personales y laborales.

## 📋 Enunciado del Proyecto
Desarrollar un aplicativo móvil para organizar actividades estudiantiles ordenadas por prioridad y fecha de cumplimiento, permitiendo la gestión completa (CRUD) de dichas tareas.

## 🚀 Funcionalidades Principales

*   **Gestión de Actividades (CRUD):**
    *   **Agregar:** Creación de tareas con título, descripción, fecha de entrega y categoría.
    *   **Listar:** Visualización de actividades pendientes ordenadas prioridades (fecha de cumplimiento).
    *   **Modificar:** Edición de actividades existentes.
    *   **Eliminar:** Borrado de tareas (con gesto *Swipe-to-Delete*).
    *   **Completar:** Opción para marcar tareas como realizadas.
*   **Búsqueda y Filtrado:**
    *   Filtrado por estado (Pendiente/Completado).
    *   Filtrado por categorías (Estudio, Trabajo, Hogar).
    *   Buscador integrado por título de actividad.
*   **Notificaciones Inteligentes:**
    *   Recordatorios programables (días antes de la fecha de entrega).
    *   Servicio en primer plano (*Foreground Service*) para monitoreo constante.
    *   Notificaciones con acciones rápidas (Completar/Silenciar).

## 🛠 Características Técnicas

El proyecto ha sido desarrollado siguiendo las mejores prácticas de desarrollo Android moderno:

*   **Lenguaje:** Kotlin.
*   **Arquitectura:** MVVM (Model-View-ViewModel) con Clean Architecture.
*   **Interfaz de Usuario:** Jetpack Compose (Material Design 3).
*   **Almacenamiento Local:** room Database (para actividades y categorías).
*   **Persistencia de Configuración:** DataStore Preferences (para temas y ajustes).
*   **Asincronía:** Kotlin Coroutines & Flow.
*   **Segundo Plano:**
    *   **WorkManager:** Para programación eficiente de recordatorios.
    *   **Foreground Service:** Servicio persistente con notificación fija.

## 👥 Integrantes del Equipo

*   Delgado Allpan, Andree David
*   Gordillo Mendoza, Jose Alonzo
*   Escobedo Ocaña, Jorge Luis
*   Hilacondo Begazo, Andre Jimmy
*   Roque Quispe, William Isaias
