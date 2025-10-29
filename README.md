# Miembros del grupo:

Iván Hidalgo
Gabriel Kaakedjian

INSTRUCCIONES DE ENTREGA:

- El ejercicio debe ser realizado en grupos de 2 y 3 estudiantes. Todos los miembros del grupo deben hacer la entrega con los mismos archivos.
- Se debe incluir un archivo tipo README donde se especifica el nombre de los miembros del grupo y una explicación breve de la lógica de la solución indicando en una línea qué contiene cada archivo de relevancia (sólo aquellos necesarios para entender la solución).
- Para facilitar la subida de archivos es preferible que estos estén comprimidos en un único archivo .zip, .rar, etc.

# Nombre del caso: Implementación de un Sistema de Seguridad Concurrente en Stark Industries

# Resumen:

Stark Industries está desarrollando un sistema de seguridad avanzado para su nueva sede en Nueva York. Este sistema debe ser capaz de manejar múltiples sensores de seguridad en tiempo real, garantizando la 
eficiencia y la capacidad de respuesta ante cualquier posible amenaza. Utilizando Spring Framework, se busca implementar un sistema concurrente que permita la integración y gestión de diferentes tipos de sensores 
(movimiento, temperatura, acceso) asegurando la máxima eficiencia y fiabilidad en la detección y respuesta a eventos.

# Reto:

El principal desafío es gestionar de manera eficiente y concurrente los datos de diferentes sensores, asegurando que el sistema pueda procesar grandes volúmenes de datos en tiempo real sin afectar la performance. 
Además, es necesario implementar mecanismos de control de acceso y notificación inmediata ante posibles intrusiones.

# Objetivo:

Desarrollar un sistema de seguridad que pueda procesar y gestionar datos de múltiples sensores en tiempo real, utilizando Spring Framework para asegurar la eficiencia, modularidad y escalabilidad del sistema. El 
sistema debe ser capaz de enviar alertas inmediatas y controlar el acceso de personas autorizadas, manteniendo un alto rendimiento y confiabilidad.

# Solución propuesta:

# 1.      Configuración del Proyecto:

- Crear un proyecto Spring Boot con dependencias necesarias para la gestión de seguridad y concurrencia.

# 2.      Gestión de Sensores:

- Implementar beans para cada tipo de sensor utilizando el principio de Inversión de Control (IoC).
- Configurar el contenedor de Spring para gestionar el ciclo de vida de los beans.

# 3.      Procesamiento Concurrente:

- Utilizar @Async y ExecutorService para manejar el procesamiento concurrente de los datos de los sensores.
- Implementar servicios que procesen los datos de los sensores de manera eficiente.

# 4.      Control de Acceso:

- Configurar Spring Security para manejar la autenticación y autorización de usuarios.
- Definir roles y permisos adecuados para el acceso al sistema.

# 5.      Notificaciones:

- Implementar un sistema de notificaciones en tiempo real utilizando WebSocket para alertar sobre cualquier intrusión o evento crítico.
- Configurar servicios de mensajería para enviar alertas a dispositivos móviles y correos electrónicos.

# 6.      Monitorización y Logs:

- Configurar Spring Actuator para monitorizar el estado del sistema.
- Implementar logging eficiente para rastrear eventos y posibles errores.

# Equipo:

- Desarrollador Backend: Encargado de implementar los servicios y la lógica de procesamiento concurrente.
- Ingeniero de Seguridad: Responsable de configurar y asegurar el sistema de autenticación y autorización.
- Desarrollador Frontend: Encargado de implementar la interfaz de usuario y las notificaciones en tiempo real.
- Administrador de Sistemas: Responsable de la configuración y monitorización del sistema.

# Resultados:

- Métricas de rendimiento: Tiempo de respuesta medio, tasa de procesamiento de eventos por segundo.
- Criterios de éxito: Sistema funcionando en tiempo real sin caídas, alertas enviadas correctamente, control de acceso efectivo.

# Elementos visuales:

- Tablas: Tabla de eventos procesados por tipo de sensor.
- Gráficos: Gráfico de rendimiento del sistema en tiempo real.
- Imágenes: Diagrama de arquitectura del sistema.

# Referencias:

- [Documentación de Spring Framework](https://spring.io/projects/spring-framework)
- [Spring Security Reference](https://spring.io/projects/spring-security)
