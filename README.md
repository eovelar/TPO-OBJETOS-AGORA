# ÁGORA Producciones

Aplicación de escritorio para gestionar eventos, desarrollada como trabajo práctico integrador de Paradigma Orientado a Objetos.

ÁGORA es una productora ficticia de actividades culturales y musicales. El proyecto toma como escenario la visita papal a la Argentina planteada en la consigna. Los eventos cargados son ejemplos académicos y no representan una agenda oficial.

El sistema está dirigido al personal interno de la productora, encargado de organizar las actividades, registrar asistentes y administrar los recursos necesarios.

## Tecnologías

- Java estándar.
- Swing para la interfaz gráfica.
- Archivos TXT para guardar la información.
- LocalDate y LocalTime para manejar fechas y horarios.
- Sin frameworks ni librerías externas.

## Funcionalidades

### Eventos

- Crear, editar y eliminar eventos.
- Registrar nombre, descripción, ubicación, fecha, horarios y capacidad.
- Consultar eventos próximos, pasados y con recursos a revisar.
- Buscar por nombre o ubicación mientras se escribe.
- Ordenar el listado por columnas.
- Elegir la fecha desde un calendario o escribirla manualmente.

Los eventos comienzan y terminan dentro del mismo día. La hora de finalización debe ser posterior a la de inicio.

### Asistentes

- Inscribir personas en un evento.
- Consultar y editar sus datos.
- Cancelar inscripciones y liberar lugares.
- Controlar la capacidad máxima.
- Evitar documentos repetidos dentro del mismo evento.

El documento admite solamente números. El correo es opcional, pero si se completa debe tener un formato válido. No se comprueba la existencia de la cuenta.

### Recursos

- Registrar recursos y su cantidad total.
- Asignar cantidades a cada evento.
- Modificar o quitar una reserva.
- Consultar las reservas que coinciden con un horario.
- Registrar unidades fuera de servicio.
- Identificar eventos que necesitan reorganizar sus recursos.

La disponibilidad considera las unidades en condiciones y el máximo uso simultáneo de las reservas de otros eventos.

Cambiar una cantidad asignada reemplaza la reserva anterior, no la suma.

### Agenda y calendario

- Agregar, editar y eliminar tareas de cada evento.
- Mantener las tareas ordenadas por hora.
- Evitar tareas fuera del horario del evento.
- Consultar actividades y tareas desde un calendario mensual.
- Mostrar un recordatorio al iniciar si hay eventos próximos dentro de los siguientes tres días, incluyendo hoy.

Los recordatorios son avisos dentro de la aplicación. No se envían correos a los asistentes.

## Control de cambios y conflictos

Antes de modificar un evento, el sistema comprueba que:

- La capacidad alcance para los asistentes registrados.
- Las tareas permanezcan dentro del nuevo horario.
- Los recursos asignados puedan cubrirse en la fecha y el horario propuestos.

Si alguna comprobación falla, el cambio se rechaza y el evento conserva sus datos anteriores.

Si un recurso queda fuera de servicio después de haber sido reservado, sus asignaciones no se eliminan. Los eventos afectados se resaltan en rojo y aparecen en el filtro A revisar.

Desde el inventario también se pueden consultar los eventos afectados y abrir directamente su pestaña de recursos.

El programa no decide qué actividad tiene prioridad. La reorganización queda a cargo del usuario.

## Estructura del proyecto

El proyecto contiene seis clases principales:

| Paquete | Clase | Responsabilidad |
| --- | --- | --- |
| agora.modelo | Evento | Datos del evento, inscripciones, reservas y agenda. |
| agora.modelo | Asistente | Datos personales y validaciones del asistente. |
| agora.modelo | Recurso | Inventario y unidades fuera de servicio. |
| agora.servicio | GestorEventos | Coordinación de operaciones y controles entre eventos. |
| agora.persistencia | PersistenciaTXT | Lectura, guardado, respaldos y recuperación de archivos. |
| agora.vista | InterfazPrincipal | Pantallas, formularios, tablas y calendarios. |

La interfaz reutiliza métodos para construir formularios, ordenar tablas y dibujar las grillas de los calendarios.

## Cómo ejecutar

1. Clonar o descargar el repositorio.
2. Abrir el proyecto en IntelliJ IDEA.
3. Configurar un JDK compatible. Para el desarrollo se utilizó JDK 27.
4. Verificar que la carpeta src esté marcada como Sources Root.
5. Ejecutar el método main de agora.vista.InterfazPrincipal.

No requiere Maven, una base de datos ni dependencias externas.

El directorio de trabajo de la ejecución debe ser la raíz del proyecto para utilizar la carpeta datos que se encuentra allí.

## Archivos de datos

La información se guarda en cinco archivos dentro de datos:

| Archivo | Contenido |
| --- | --- |
| eventos.txt | Datos generales de los eventos. |
| asistentes.txt | Inscripciones y datos de los asistentes. |
| recursos.txt | Inventario y unidades fuera de servicio. |
| asignaciones.txt | Recursos reservados para cada evento y sus cantidades. |
| agenda.txt | Horarios y descripciones de las tareas. |

Los archivos utilizan UTF-8 y campos separados por punto y coma.

recursos.txt guarda cinco campos: identificador, nombre, tipo, cantidad total y cantidad fuera de servicio. También se admite el formato anterior de cuatro campos, considerando cero unidades fuera de servicio.

La carpeta datos se crea desde el programa si no existe. Si no hay ninguno de los cinco TXT, se puede iniciar sin información precargada. Si existen algunos y faltan otros, la carga se detiene para evitar trabajar con datos incompletos.

Al cambiar de computadora, hay que conservar también los TXT si se quiere continuar con la misma información.

## Guardado y copias de seguridad

Después de una modificación se solicita el guardado.

Antes de reemplazar los archivos, el programa prepara los cinco TXT nuevos y conserva una copia de los anteriores. Los respaldos se encuentran dentro de datos/backups.

Cada respaldo tiene su propia carpeta. El archivo copia_completa.txt indica que terminó la copia de los archivos, no que se haya validado todo su contenido.

Si falla el reemplazo, se intenta restaurar la versión anterior. Al iniciar también se revisa si quedó una operación pendiente en datos/guardado_pendiente.

Si un cambio queda en memoria sin guardarse, la interfaz muestra un aviso y permite reintentar. Antes de cerrar ofrece guardar, salir sin guardar o volver al programa.

### Recuperación manual

Con el programa cerrado:

1. Conservar una copia de la carpeta datos actual.
2. Elegir un respaldo que contenga los cinco TXT y la marca copia_completa.txt.
3. Copiar los cinco TXT de ese mismo respaldo a datos.
4. Volver a iniciar el programa.

No mezclar archivos de distintos respaldos ni eliminar una carpeta guardado_pendiente sin revisar el error informado.

## Validaciones y alcance

El sistema controla campos obligatorios, fechas, horarios, cantidades, cupos y duplicados. Durante la lectura también comprueba las relaciones entre registros e informa el archivo y la línea cuando encuentra datos incorrectos.

La aplicación está pensada para uso interno y local. No incluye:

- Inicio de sesión ni permisos por usuario.
- Edición simultánea desde varias computadoras.
- Envío de correos.
- Integración con calendarios externos.
- Tiempos automáticos de traslado, armado o desarme.
- Historial de reparaciones de los recursos.

Una ubicación escrita como texto no bloquea por sí sola la reserva de ese espacio. Para controlar su disponibilidad mediante el sistema debe registrarse y asignarse como recurso.

Los respaldos y la recuperación mejoran la protección de los datos, pero no reemplazan una base de datos transaccional ni garantizan recuperar cualquier daño del disco.

## Comprobaciones realizadas

Durante el desarrollo se revisaron desde la interfaz:

- Guardado de cambios y recuperación al volver a abrir.
- Generación de copias de seguridad.
- Rechazo de cambios de horario que generan conflictos de recursos.
- Avisos por recursos fuera de servicio con reservas futuras.

Las pruebas de errores de escritura e interrupciones durante el guardado deben realizarse sobre copias de prueba, no sobre los datos que se quieran conservar.

