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


## 🧠 Lógica general de la solución
El sistema simula una red de sensores conectados a dispositivos, administrados por los usuarios de Rol Administrador, que registran lecturas (temperatura, humedad, movimiento, etc.)
que se administran por los usuarios con Rol de OPERATOR o ADMIN.
Cada lectura se almacena en una base de datos y se muestra en una interfaz web protegida con autenticación.  
Cuando una lectura supera un umbral peligroso, el sistema genera una alerta simulada (por ejemplo, envío de correo electrónico).  

## 🔄 Flujo del programa

1. **Inicio de la aplicación**  
   - `Main.java` lanza la aplicación Spring Boot e inicializa todos los componentes. Inicia Sesión con el Administrador que se
      crea automáticamente [admin,AdminPass123] para poder controlar todos los sensores y usuarios. Después puedes Registrarte en el /login
      -> /register, para iniciar como un Usuario OPERATOR, el cual solo se encarga de procesar las lecturas y clasificarlas.

2. **Registro de dispositivos y sensores**  
   - Desde la interfaz o mediante el repositorio, se crean objetos `SensorDevice` que representan dispositivos físicos.  
   - Cada uno puede tener múltiples sensores asociados (`SensorReading`).

3. **Generación de lecturas**  
   - Los sensores producen datos de forma periódica o manual (simulados).  
   - Estas lecturas (`TemperatureReading`, `HumidityReading`, etc.) se guardan en la base de datos a través del repositorio JPA.

4. **Evaluación de condiciones peligrosas**  
   - El servicio `AlertService` analiza las lecturas entrantes y detecta valores fuera de rango.  
   - Si una lectura se considera peligrosa, se genera una alerta **simulada** (por consola o log del sistema).

5. **Visualización en el panel web**  
   - El frontend (`index.html`, `main.js`) se comunica con la API REST para obtener las lecturas y los dispositivos registrados.  
   - Se actualiza automáticamente cada pocos segundos.  
   - Los usuarios pueden filtrar por tipo de sensor o dispositivo.

6. **Autenticación y roles**  
   - El acceso al panel está protegido por Spring Security (`SecurityConfig.java`).  
   - Solo los usuarios registrados pueden acceder al panel.  
   - Las credenciales se guardan en la base de datos.

---

## 📁 Archivos principales

- **Main.java** → Clase principal que arranca la aplicación Spring Boot.  
- **SensorReading.java** → Clase abstracta base para las distintas lecturas de sensores.  
- **TemperatureReading.java / HumidityReading.java** → Subclases concretas de `SensorReading` con atributos específicos.  
- **SensorDevice.java** → Representa un dispositivo físico con uno o varios sensores.  
- **SensorDeviceRepo.java** → Repositorio JPA para acceder a los dispositivos almacenados en la base de datos.  
- **SensorReadingRepo.java** → Repositorio JPA para gestionar las lecturas de sensores.  
- **AlertService.java** → Servicio que detecta condiciones peligrosas y simula el envío de alertas.  
- **SecurityConfig.java** → Configuración de seguridad (Spring Security) para la autenticación de usuarios.  
- **application.properties** → Configuración de la base de datos y parámetros generales del proyecto.  
- **static/js/main.js** → Lógica del panel web: actualización automática, filtrado y visualización de sensores.  
- **templates/index.html** → Interfaz principal del panel de control.  

---

## 💡 Notas adicionales
- La base de datos utilizada es **PostgreSQL**, configurada en `application.properties`.  
- El proyecto sigue la arquitectura MVC (Modelo - Vista - Controlador) con uso de Spring Boot y JPA.


