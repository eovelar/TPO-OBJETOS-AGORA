package agora.persistencia;

import agora.modelo.Asistente;
import agora.modelo.Evento;
import agora.modelo.Recurso;
import agora.servicio.GestorEventos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class PersistenciaTXT {

    private final Path carpetaDatos;

    private static final List<String> ARCHIVOS = List.of(
            "eventos.txt",
            "asistentes.txt",
            "recursos.txt",
            "asignaciones.txt",
            "agenda.txt"
    );

    private static final DateTimeFormatter FORMATO_BACKUP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public PersistenciaTXT(String rutaCarpeta) {
        if (rutaCarpeta == null || rutaCarpeta.isBlank()) {
            throw new IllegalArgumentException(
                    "La carpeta de datos es obligatoria."
            );
        }

        carpetaDatos = Path.of(rutaCarpeta);
    }

    public void cargarDatos(GestorEventos gestor) throws IOException {
        validarGestor(gestor);

        if (!gestor.obtenerEventosOrdenados().isEmpty()
                || !gestor.obtenerRecursos().isEmpty()) {
            throw new IllegalStateException(
                    "La carga debe realizarse con el gestor vacío."
            );
        }

        Files.createDirectories(carpetaDatos);
        recuperarGuardadoPendiente();
        comprobarArchivos();

        // valida los archivos antes de modificar el gestor principal
        GestorEventos temporal = new GestorEventos();

        cargarRecursos(temporal);
        cargarEventos(temporal);
        cargarAsistentes(temporal);
        cargarAsignaciones(temporal);
        cargarAgenda(temporal);

        for (Recurso recurso : temporal.obtenerRecursos()) {
            gestor.registrarRecurso(recurso);
        }

        for (Evento evento : temporal.obtenerEventosOrdenados()) {
            gestor.registrarEvento(evento);
        }
    }

    public void guardarDatos(GestorEventos gestor) throws IOException {
        validarGestor(gestor);

        Files.createDirectories(carpetaDatos);
        recuperarGuardadoPendiente();
        comprobarArchivos();

        Map<String, List<String>> contenido =
                prepararContenido(gestor);

        Path operacion = carpetaOperacion();
        Files.createDirectory(operacion);

        try {
            Path nuevos = operacion.resolve("nuevos");
            Path anteriores = operacion.resolve("anteriores");

            Files.createDirectory(nuevos);
            Files.createDirectory(anteriores);

            // prepara los cinco archivos sin tocar los actuales
            for (String nombre : ARCHIVOS) {
                Files.write(
                        nuevos.resolve(nombre),
                        contenido.get(nombre),
                        StandardCharsets.UTF_8
                );
            }

            List<String> existentes = new ArrayList<>();

            for (String nombre : ARCHIVOS) {
                Path original = carpetaDatos.resolve(nombre);

                if (Files.exists(original)) {
                    Files.copy(
                            original,
                            anteriores.resolve(nombre)
                    );

                    existentes.add(nombre);
                }
            }

            Files.write(
                    operacion.resolve("archivos_anteriores.txt"),
                    existentes,
                    StandardCharsets.UTF_8
            );

            crearCopiaSeguridad(anteriores, existentes);

            // recién después de esta marca empieza el reemplazo
            escribirMarca(
                    operacion.resolve("preparado.txt"),
                    "Preparado"
            );

            for (String nombre : ARCHIVOS) {
                reemplazarArchivo(
                        nuevos.resolve(nombre),
                        carpetaDatos.resolve(nombre)
                );
            }

            escribirMarca(
                    operacion.resolve("completado.txt"),
                    "Completado"
            );

        } catch (IOException errorGuardado) {
            try {
                recuperarGuardadoPendiente();
            } catch (IOException errorRecuperacion) {
                errorGuardado.addSuppressed(errorRecuperacion);

                throw new IOException(
                        "Falló el guardado y no se pudo completar "
                                + "la recuperación automática.\n"
                                + "No borres la carpeta: "
                                + operacion.toAbsolutePath()
                                + "\nGuardado: "
                                + errorGuardado.getMessage()
                                + "\nRecuperación: "
                                + errorRecuperacion.getMessage(),
                        errorGuardado
                );
            }

            throw new IOException(
                    "No se guardaron los cambios. "
                            + "Los archivos anteriores se conservaron "
                            + "o se restauraron.\n"
                            + errorGuardado.getMessage(),
                    errorGuardado
            );
        }

        // una falla de limpieza no convierte el guardado en fallido
        try {
            limpiarOperacion(operacion);
        } catch (IOException e) {
            System.err.println(
                    "Los datos se guardaron, pero quedó pendiente "
                            + "la limpieza de "
                            + operacion.toAbsolutePath()
                            + ": "
                            + e.getMessage()
            );
        }
    }

    private Path carpetaOperacion() {
        return carpetaDatos.resolve("guardado_pendiente");
    }

    private void escribirMarca(
            Path archivo,
            String descripcion
    ) throws IOException {
        Files.writeString(
                archivo,
                descripcion + ": " + LocalDateTime.now(),
                StandardCharsets.UTF_8
        );
    }

    private void recuperarGuardadoPendiente() throws IOException {
        Path operacion = carpetaOperacion();

        if (Files.notExists(operacion)) {
            return;
        }

        if (!Files.isDirectory(operacion)) {
            throw new IOException(
                    "La ruta de recuperación no es una carpeta: "
                            + operacion.toAbsolutePath()
            );
        }

        boolean completado = Files.exists(
                operacion.resolve("completado.txt")
        );

        boolean restaurado = Files.exists(
                operacion.resolve("restaurado.txt")
        );

        if (completado || restaurado) {
            limpiarOperacion(operacion);
            return;
        }

        if (Files.exists(operacion.resolve("preparado.txt"))) {
            restaurarArchivosAnteriores(operacion);

            // evita repetir la restauración si después falla la limpieza
            escribirMarca(
                    operacion.resolve("restaurado.txt"),
                    "Restaurado"
            );
        }

        // sin preparado todavía no se modificaron los originales
        limpiarOperacion(operacion);
    }

    private void restaurarArchivosAnteriores(Path operacion)
            throws IOException {
        Path registro = operacion.resolve("archivos_anteriores.txt");

        if (!Files.isRegularFile(registro)) {
            throw new IOException(
                    "Falta el registro necesario para recuperar el guardado."
            );
        }

        List<String> existentes = Files.readAllLines(
                registro,
                StandardCharsets.UTF_8
        );

        if (!ARCHIVOS.containsAll(existentes)
                || new HashSet<>(existentes).size() != existentes.size()) {
            throw new IOException(
                    "El registro de recuperación contiene datos incorrectos."
            );
        }

        if (!existentes.isEmpty()
                && existentes.size() != ARCHIVOS.size()) {
            throw new IOException(
                    "El registro de recuperación está incompleto."
            );
        }

        Path anteriores = operacion.resolve("anteriores");

        // comprueba las copias antes de restaurar cualquier archivo
        for (String nombre : existentes) {
            if (!Files.isRegularFile(anteriores.resolve(nombre))) {
                throw new IOException(
                        "Falta la copia anterior de " + nombre + "."
                );
            }
        }

        for (String nombre : ARCHIVOS) {
            Path destino = carpetaDatos.resolve(nombre);

            if (existentes.contains(nombre)) {
                // conserva el respaldo para poder reintentar
                Files.copy(
                        anteriores.resolve(nombre),
                        destino,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } else {
                // el primer guardado puede haber creado archivos nuevos
                Files.deleteIfExists(destino);
            }
        }
    }

    private void reemplazarArchivo(
            Path origen,
            Path destino
    ) throws IOException {
        try {
            Files.move(
                    origen,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                    origen,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private void limpiarOperacion(Path operacion) throws IOException {
        // elimina solo los temporales conocidos de esta operación
        for (String subcarpeta : List.of("nuevos", "anteriores")) {
            Path carpeta = operacion.resolve(subcarpeta);

            for (String nombre : ARCHIVOS) {
                Files.deleteIfExists(carpeta.resolve(nombre));
            }

            Files.deleteIfExists(carpeta);
        }

        Files.deleteIfExists(
                operacion.resolve("archivos_anteriores.txt")
        );

        // las marcas finales se conservan hasta retirar la de preparado
        Files.deleteIfExists(operacion.resolve("preparado.txt"));
        Files.deleteIfExists(operacion.resolve("completado.txt"));
        Files.deleteIfExists(operacion.resolve("restaurado.txt"));
        Files.deleteIfExists(operacion);
    }

    private void comprobarArchivos() throws IOException {
        int presentes = 0;
        List<String> faltantes = new ArrayList<>();

        for (String nombre : ARCHIVOS) {
            Path archivo = carpetaDatos.resolve(nombre);

            if (Files.notExists(archivo)) {
                faltantes.add(nombre);
                continue;
            }

            if (!Files.isRegularFile(archivo)) {
                throw new IOException(
                        "No se puede leer como archivo: "
                                + archivo.toAbsolutePath()
                );
            }

            presentes++;
        }

        if (presentes > 0 && presentes < ARCHIVOS.size()) {
            throw new IOException(
                    "La carpeta de datos está incompleta.\n"
                            + "Faltan: "
                            + String.join(", ", faltantes)
                            + ".\nRecuperá los archivos de una misma "
                            + "copia de seguridad antes de continuar."
            );
        }
    }

    private void crearCopiaSeguridad(
            Path anteriores,
            List<String> existentes
    ) throws IOException {
        if (existentes.isEmpty()) {
            return;
        }

        Path backups = carpetaDatos.resolve("backups");
        Files.createDirectories(backups);

        String fecha = LocalDateTime.now().format(FORMATO_BACKUP);

        Path copia = Files.createTempDirectory(
                backups,
                "copia_" + fecha + "_"
        );

        for (String nombre : existentes) {
            Files.copy(
                    anteriores.resolve(nombre),
                    copia.resolve(nombre)
            );
        }

        escribirMarca(
                copia.resolve("copia_completa.txt"),
                "Copia completada"
        );
    }

    private Map<String, List<String>> prepararContenido(
            GestorEventos gestor
    ) {
        Map<String, List<String>> contenido = new LinkedHashMap<>();

        for (String nombre : ARCHIVOS) {
            contenido.put(nombre, new ArrayList<>());
        }

        for (Evento evento : gestor.obtenerEventosOrdenados()) {
            contenido.get("eventos.txt").add(
                    evento.getId() + ";"
                            + limpiar(evento.getNombre()) + ";"
                            + limpiar(evento.getDescripcion()) + ";"
                            + limpiar(evento.getUbicacion()) + ";"
                            + evento.getFecha() + ";"
                            + evento.getHoraInicio() + ";"
                            + evento.getHoraFin() + ";"
                            + evento.getCapacidad()
            );

            for (Asistente asistente : evento.getAsistentes()) {
                contenido.get("asistentes.txt").add(
                        evento.getId() + ";"
                                + asistente.getId() + ";"
                                + limpiar(asistente.getNombreCompleto()) + ";"
                                + limpiar(asistente.getDocumento()) + ";"
                                + limpiar(asistente.getCargo()) + ";"
                                + limpiar(asistente.getInstitucion()) + ";"
                                + limpiar(asistente.getEmail())
                );
            }

            for (Map.Entry<Recurso, Integer> asignacion
                    : evento.getRecursosAsignados().entrySet()) {
                contenido.get("asignaciones.txt").add(
                        evento.getId() + ";"
                                + asignacion.getKey().getId() + ";"
                                + asignacion.getValue()
                );
            }

            for (Map.Entry<LocalTime, String> tarea
                    : evento.getAgenda().entrySet()) {
                contenido.get("agenda.txt").add(
                        evento.getId() + ";"
                                + tarea.getKey() + ";"
                                + limpiar(tarea.getValue())
                );
            }
        }

        for (Recurso recurso : gestor.obtenerRecursos()) {
            // la quinta columna guarda las unidades fuera de servicio
            contenido.get("recursos.txt").add(
                    recurso.getId() + ";"
                            + limpiar(recurso.getNombre()) + ";"
                            + limpiar(recurso.getTipo()) + ";"
                            + recurso.getCantidadTotal() + ";"
                            + recurso.getCantidadFueraServicio()
            );
        }

        return contenido;
    }

    private void cargarRecursos(GestorEventos gestor) throws IOException {
        // admite el formato anterior y el nuevo
        procesarArchivo(
                "recursos.txt",
                List.of(4, 5),
                datos -> {
                    int fueraServicio = datos.length == 5
                            ? leerEntero(
                            datos[4],
                            "cantidad fuera de servicio"
                    )
                            : 0;

                    Recurso recurso = new Recurso(
                            leerEntero(datos[0], "id del recurso"),
                            datos[1],
                            datos[2],
                            leerEntero(datos[3], "cantidad total"),
                            fueraServicio
                    );

                    gestor.registrarRecurso(recurso);
                }
        );
    }

    private void cargarEventos(GestorEventos gestor) throws IOException {
        procesarArchivo("eventos.txt", List.of(8), datos -> {
            Evento evento = new Evento(
                    leerEntero(datos[0], "id del evento"),
                    datos[1],
                    datos[2],
                    datos[3],
                    leerFecha(datos[4]),
                    leerHora(datos[5], "hora de inicio"),
                    leerHora(datos[6], "hora de finalización"),
                    leerEntero(datos[7], "capacidad")
            );

            gestor.registrarEvento(evento);
        });
    }

    private void cargarAsistentes(GestorEventos gestor) throws IOException {
        procesarArchivo("asistentes.txt", List.of(7), datos -> {
            Evento evento = exigirEvento(
                    gestor,
                    leerEntero(datos[0], "id del evento")
            );

            Asistente asistente = new Asistente(
                    leerEntero(datos[1], "id del asistente"),
                    datos[2],
                    datos[3],
                    datos[4],
                    datos[5],
                    datos[6]
            );

            if (!evento.inscribirAsistente(asistente)) {
                throw new IllegalArgumentException(
                        "No se pudo recuperar la inscripción del documento "
                                + asistente.getDocumento()
                                + ". El evento \""
                                + evento.getNombre()
                                + "\" alcanzó su capacidad máxima."
                );
            }
        });
    }

    private void cargarAsignaciones(GestorEventos gestor) throws IOException {
        procesarArchivo("asignaciones.txt", List.of(3), datos -> {
            int idEvento = leerEntero(
                    datos[0], "id del evento"
            );

            int idRecurso = leerEntero(
                    datos[1], "id del recurso"
            );

            int cantidad = leerEntero(
                    datos[2], "cantidad asignada"
            );

            Evento evento = exigirEvento(gestor, idEvento);
            Recurso recurso = gestor.buscarRecursoPorId(idRecurso);

            if (recurso == null) {
                throw new IllegalArgumentException(
                        "La asignación corresponde a un recurso inexistente: "
                                + idRecurso + "."
                );
            }

            if (cantidad <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad asignada debe ser mayor que cero."
                );
            }

            if (evento.getRecursosAsignados().containsKey(recurso)) {
                throw new IllegalArgumentException(
                        "El recurso \""
                                + recurso.getNombre()
                                + "\" está repetido para el evento \""
                                + evento.getNombre()
                                + "\"."
                );
            }

            // una rotura no debe hacer desaparecer reservas al reiniciar
            validarReservaContraInventario(
                    gestor,
                    evento,
                    recurso,
                    cantidad
            );

            evento.asignarRecurso(recurso, cantidad);
        });
    }

    private void validarReservaContraInventario(
            GestorEventos gestor,
            Evento evento,
            Recurso recurso,
            int cantidad
    ) {
        Map<LocalTime, Long> movimientos = new TreeMap<>();

        movimientos.merge(
                evento.getHoraInicio(),
                (long) cantidad,
                Long::sum
        );

        movimientos.merge(
                evento.getHoraFin(),
                -(long) cantidad,
                Long::sum
        );

        for (Evento otro : gestor.obtenerEventosOrdenados()) {
            if (otro.getId() == evento.getId()
                    || !otro.seSuperponeCon(evento)) {
                continue;
            }

            int reservada = otro.obtenerCantidadAsignada(recurso);

            if (reservada == 0) {
                continue;
            }

            LocalTime inicio = otro.getHoraInicio()
                    .isAfter(evento.getHoraInicio())
                    ? otro.getHoraInicio()
                    : evento.getHoraInicio();

            LocalTime fin = otro.getHoraFin()
                    .isBefore(evento.getHoraFin())
                    ? otro.getHoraFin()
                    : evento.getHoraFin();

            movimientos.merge(
                    inicio,
                    (long) reservada,
                    Long::sum
            );

            movimientos.merge(
                    fin,
                    -(long) reservada,
                    Long::sum
            );
        }

        long enUso = 0;
        long maximo = 0;

        for (long movimiento : movimientos.values()) {
            enUso += movimiento;
            maximo = Math.max(maximo, enUso);
        }

        // admite conflictos por roturas, no reservas superiores al inventario
        if (maximo > recurso.getCantidadTotal()) {
            throw new IllegalArgumentException(
                    "Las reservas simultáneas de \""
                            + recurso.getNombre()
                            + "\" superan el inventario total.\n"
                            + "Máximo simultáneo: "
                            + maximo
                            + ". Inventario total: "
                            + recurso.getCantidadTotal()
                            + "."
            );
        }
    }

    private void cargarAgenda(GestorEventos gestor) throws IOException {
        procesarArchivo("agenda.txt", List.of(3), datos -> {
            Evento evento = exigirEvento(
                    gestor,
                    leerEntero(datos[0], "id del evento")
            );

            LocalTime hora = leerHora(
                    datos[1], "hora de la tarea"
            );

            if (evento.getAgenda().containsKey(hora)) {
                throw new IllegalArgumentException(
                        "Hay dos tareas a las "
                                + hora
                                + " en el evento \""
                                + evento.getNombre()
                                + "\"."
                );
            }

            evento.agregarTarea(hora, datos[2]);
        });
    }

    private Evento exigirEvento(
            GestorEventos gestor,
            int idEvento
    ) {
        Evento evento = gestor.buscarEventoPorId(idEvento);

        if (evento == null) {
            throw new IllegalArgumentException(
                    "El registro corresponde a un evento inexistente: "
                            + idEvento + "."
            );
        }

        return evento;
    }

    private void procesarArchivo(
            String nombreArchivo,
            List<Integer> columnasPermitidas,
            Consumer<String[]> procesar
    ) throws IOException {
        List<String> lineas = leerArchivo(nombreArchivo);

        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);

            if (linea.isBlank()) {
                continue;
            }

            try {
                String[] datos = linea.split(";", -1);

                if (!columnasPermitidas.contains(datos.length)) {
                    throw new IllegalArgumentException(
                            "Cantidad de columnas incorrecta.\n"
                                    + "Cantidades admitidas: "
                                    + columnasPermitidas
                                    + ". Encontradas: "
                                    + datos.length
                                    + "."
                    );
                }

                procesar.accept(datos);

            } catch (RuntimeException e) {
                String detalle = e.getMessage();

                if (detalle == null || detalle.isBlank()) {
                    detalle = "El registro contiene datos incorrectos.";
                }

                throw new IOException(
                        "Archivo: " + nombreArchivo
                                + "\nLínea: " + (i + 1)
                                + "\nProblema: " + detalle,
                        e
                );
            }
        }
    }

    private int leerEntero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El campo \"" + campo
                            + "\" debe contener un número entero válido.",
                    e
            );
        }
    }

    private LocalDate leerFecha(String texto) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "La fecha debe ser válida y tener formato AAAA-MM-DD.",
                    e
            );
        }
    }

    private LocalTime leerHora(String texto, String campo) {
        try {
            return LocalTime.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "El campo \"" + campo
                            + "\" debe contener una hora válida, "
                            + "por ejemplo 14:30.",
                    e
            );
        }
    }

    private List<String> leerArchivo(String nombreArchivo)
            throws IOException {
        Path archivo = carpetaDatos.resolve(nombreArchivo);

        if (Files.notExists(archivo)) {
            return List.of();
        }

        if (!Files.isRegularFile(archivo)) {
            throw new IOException(
                    "No se puede leer como archivo: "
                            + archivo.toAbsolutePath()
            );
        }

        return Files.readAllLines(
                archivo,
                StandardCharsets.UTF_8
        );
    }

    private void validarGestor(GestorEventos gestor) {
        if (gestor == null) {
            throw new IllegalArgumentException(
                    "El gestor de eventos es obligatorio."
            );
        }
    }

    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }

        // evita alterar las columnas y las líneas del archivo
        return texto
                .replace(";", ",")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}