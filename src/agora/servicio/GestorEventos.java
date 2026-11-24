package agora.servicio;

import agora.modelo.Asistente;
import agora.modelo.Evento;
import agora.modelo.Recurso;
import agora.persistencia.PersistenciaTXT;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GestorEventos {

    private final List<Evento> eventos;
    private final List<Recurso> recursos;
    private final PersistenciaTXT persistencia;

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    public GestorEventos() {
        eventos = new ArrayList<>();
        recursos = new ArrayList<>();
        persistencia = new PersistenciaTXT("datos");
    }

    public void cargarDatos() throws IOException {
        if (!eventos.isEmpty() || !recursos.isEmpty()) {
            throw new IllegalStateException(
                    "Los datos deben cargarse antes de registrar información."
            );
        }

        persistencia.cargarDatos(this);
    }

    public void guardarDatos() throws IOException {
        persistencia.guardarDatos(this);
    }

    public void registrarEvento(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException(
                    "El evento es obligatorio."
            );
        }

        if (buscarEventoPorId(evento.getId()) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un evento con ese id."
            );
        }

        eventos.add(evento);
    }

    public void registrarRecurso(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException(
                    "El recurso es obligatorio."
            );
        }

        if (buscarRecursoPorId(recurso.getId()) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un recurso con ese id."
            );
        }

        recursos.add(recurso);
    }

    public Evento buscarEventoPorId(int id) {
        for (Evento evento : eventos) {
            if (evento.getId() == id) {
                return evento;
            }
        }

        return null;
    }

    public Recurso buscarRecursoPorId(int id) {
        for (Recurso recurso : recursos) {
            if (recurso.getId() == id) {
                return recurso;
            }
        }

        return null;
    }

    public void actualizarEvento(
            int id,
            String nombre,
            String descripcion,
            String ubicacion,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            int capacidad
    ) {
        Evento evento = exigirEvento(id);

        // valida la propuesta sin modificar el evento original
        Evento propuesta = new Evento(
                id,
                nombre,
                descripcion,
                ubicacion,
                fecha,
                horaInicio,
                horaFin,
                capacidad
        );

        int inscriptos = evento.getAsistentes().size();

        if (capacidad < inscriptos) {
            throw new IllegalArgumentException(
                    "No se puede reducir la capacidad a "
                            + capacidad
                            + ". Hay "
                            + inscriptos
                            + " asistentes registrados.\n"
                            + "Primero revisá las inscripciones.\n"
                            + "El evento conserva sus datos anteriores."
            );
        }

        for (Map.Entry<LocalTime, String> tarea
                : evento.getAgenda().entrySet()) {

            LocalTime hora = tarea.getKey();

            if (hora.isBefore(horaInicio) || hora.isAfter(horaFin)) {
                throw new IllegalArgumentException(
                        "No se puede confirmar el nuevo horario.\n"
                                + "La tarea \""
                                + tarea.getValue()
                                + "\" de las "
                                + hora.format(HORA)
                                + " quedaría fuera del evento.\n"
                                + "Horario propuesto: "
                                + horaInicio.format(HORA)
                                + " a "
                                + horaFin.format(HORA)
                                + ".\n"
                                + "Revisá la agenda o elegí otro horario.\n"
                                + "El evento conserva sus datos anteriores."
                );
            }
        }

        // revisa las reservas con las unidades que pueden utilizarse
        for (Map.Entry<Recurso, Integer> asignacion
                : evento.getRecursosAsignados().entrySet()) {

            Recurso recurso = asignacion.getKey();
            int cantidad = asignacion.getValue();

            if (cantidad > calcularDisponible(recurso, propuesta)) {
                throw new IllegalArgumentException(
                        "No se puede confirmar el cambio del evento.\n\n"
                                + construirDetalleDisponibilidad(
                                recurso,
                                propuesta,
                                cantidad
                        )
                                + "\n\nPodés elegir otro horario, revisar "
                                + "las reservas o reorganizar los recursos.\n"
                                + "El evento conserva sus datos anteriores."
                );
            }
        }

        evento.actualizarDatos(
                nombre,
                descripcion,
                ubicacion,
                fecha,
                horaInicio,
                horaFin,
                capacidad
        );
    }

    public boolean eliminarEvento(int id) {
        Evento evento = buscarEventoPorId(id);

        return evento != null && eventos.remove(evento);
    }

    public boolean inscribirAsistente(
            int idEvento,
            Asistente asistente
    ) {
        Evento evento = buscarEventoPorId(idEvento);

        if (evento == null) {
            return false;
        }

        return evento.inscribirAsistente(asistente);
    }

    public void actualizarAsistente(
            int idEvento,
            int idAsistente,
            String nombreCompleto,
            String cargo,
            String institucion,
            String email
    ) {
        Asistente asistente = buscarAsistenteEnEvento(
                idEvento,
                idAsistente
        );

        if (asistente == null) {
            throw new IllegalArgumentException(
                    "No se encontró la inscripción que querés modificar."
            );
        }

        asistente.actualizarDatos(
                nombreCompleto,
                cargo,
                institucion,
                email
        );
    }

    public boolean cancelarInscripcion(
            int idEvento,
            int idAsistente
    ) {
        Evento evento = buscarEventoPorId(idEvento);

        Asistente asistente = buscarAsistenteEnEvento(
                idEvento,
                idAsistente
        );

        if (evento == null || asistente == null) {
            return false;
        }

        return evento.quitarAsistente(asistente);
    }

    private Asistente buscarAsistenteEnEvento(
            int idEvento,
            int idAsistente
    ) {
        Evento evento = buscarEventoPorId(idEvento);

        if (evento == null) {
            return null;
        }

        return evento.getAsistentes().stream()
                .filter(asistente -> asistente.getId() == idAsistente)
                .findFirst()
                .orElse(null);
    }

    public boolean asignarRecurso(
            int idEvento,
            int idRecurso,
            int cantidad
    ) {
        Evento evento = buscarEventoPorId(idEvento);
        Recurso recurso = buscarRecursoPorId(idRecurso);

        if (evento == null || recurso == null || cantidad <= 0) {
            return false;
        }

        if (cantidad > calcularDisponible(recurso, evento)) {
            return false;
        }

        // reemplaza la reserva anterior, no suma otra cantidad
        evento.asignarRecurso(recurso, cantidad);
        return true;
    }

    public int obtenerCantidadDisponible(
            int idEvento,
            int idRecurso
    ) {
        Evento evento = exigirEvento(idEvento);
        Recurso recurso = exigirRecurso(idRecurso);

        return calcularDisponible(recurso, evento);
    }

    public String obtenerDetalleDisponibilidad(
            int idEvento,
            int idRecurso,
            int cantidad
    ) {
        Evento evento = exigirEvento(idEvento);
        Recurso recurso = exigirRecurso(idRecurso);

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        return construirDetalleDisponibilidad(
                recurso,
                evento,
                cantidad
        );
    }

    public boolean quitarRecursoAsignado(
            int idEvento,
            int idRecurso
    ) {
        Evento evento = buscarEventoPorId(idEvento);
        Recurso recurso = buscarRecursoPorId(idRecurso);

        if (evento == null || recurso == null
                || evento.obtenerCantidadAsignada(recurso) == 0) {
            return false;
        }

        // retira la reserva, pero conserva el inventario
        evento.quitarRecurso(recurso);
        return true;
    }

    public List<Evento> actualizarCantidadFueraServicio(
            int idRecurso,
            int cantidad
    ) {
        Recurso recurso = exigirRecurso(idRecurso);

        // una rotura se registra aunque afecte reservas existentes
        recurso.actualizarCantidadFueraServicio(cantidad);

        return obtenerEventosConConflictoDeRecurso(idRecurso);
    }

    public List<Evento> obtenerEventosConConflictoDeRecurso(
            int idRecurso
    ) {
        Recurso recurso = exigirRecurso(idRecurso);

        // incluye eventos en curso y futuros, no los que ya terminaron
        return obtenerEventosOrdenados().stream()
                .filter(evento -> !evento.esPasado())
                .filter(evento ->
                        tieneConflictoDeRecurso(evento, recurso)
                )
                .toList();
    }

    public boolean tieneConflictosDeRecursos(int idEvento) {
        Evento evento = exigirEvento(idEvento);

        return evento.getRecursosAsignados().keySet().stream()
                .anyMatch(recurso ->
                        tieneConflictoDeRecurso(evento, recurso)
                );
    }

    private boolean tieneConflictoDeRecurso(
            Evento evento,
            Recurso recurso
    ) {
        int asignada = evento.obtenerCantidadAsignada(recurso);

        return asignada > 0
                && asignada > calcularDisponible(recurso, evento);
    }

    public String obtenerResumenConflictosRecurso(int idRecurso) {
        Recurso recurso = exigirRecurso(idRecurso);

        List<Evento> afectados =
                obtenerEventosConConflictoDeRecurso(idRecurso);

        StringBuilder mensaje = new StringBuilder();

        mensaje.append("Recurso: ")
                .append(recurso.getNombre())
                .append("\nUnidades en inventario: ")
                .append(recurso.getCantidadTotal())
                .append("\nFuera de servicio: ")
                .append(recurso.getCantidadFueraServicio())
                .append("\nUnidades operativas: ")
                .append(recurso.getCantidadOperativa())
                .append(".");

        if (afectados.isEmpty()) {
            mensaje.append(
                    "\n\nNo hay eventos en curso o futuros "
                            + "con reservas en conflicto para este recurso."
            );
        } else {
            mensaje.append(
                    "\n\nEventos cuyas reservas necesitan revisión:"
            );

            for (Evento evento : afectados) {
                mensaje.append("\n- ")
                        .append(evento.getNombre())
                        .append(" (id ")
                        .append(evento.getId())
                        .append(")")
                        .append("\n  ")
                        .append(evento.getFecha().format(FECHA))
                        .append(", ")
                        .append(evento.getHoraInicio().format(HORA))
                        .append(" a ")
                        .append(evento.getHoraFin().format(HORA))
                        .append(". Reserva: ")
                        .append(evento.obtenerCantidadAsignada(recurso))
                        .append(" unidad(es).");
            }

            mensaje.append(
                    "\n\nLas reservas se conservaron. Revisá las cantidades, "
                            + "los horarios o los recursos alternativos.\n"
                            + "El sistema no decide qué evento tiene prioridad."
            );
        }

        return mensaje.toString();
    }

    private Evento exigirEvento(int id) {
        Evento evento = buscarEventoPorId(id);

        if (evento == null) {
            throw new IllegalArgumentException(
                    "No se encontró el evento."
            );
        }

        return evento;
    }

    private Recurso exigirRecurso(int id) {
        Recurso recurso = buscarRecursoPorId(id);

        if (recurso == null) {
            throw new IllegalArgumentException(
                    "No se encontró el recurso."
            );
        }

        return recurso;
    }

    private int calcularDisponible(
            Recurso recurso,
            Evento evento
    ) {
        int comprometida = calcularCantidadComprometida(
                recurso,
                evento
        );

        // descuenta las unidades fuera de servicio y las otras reservas
        return Math.max(
                0,
                recurso.getCantidadOperativa() - comprometida
        );
    }

    private String construirDetalleDisponibilidad(
            Recurso recurso,
            Evento evento,
            int cantidad
    ) {
        int comprometida = calcularCantidadComprometida(
                recurso,
                evento
        );

        int disponible = Math.max(
                0,
                recurso.getCantidadOperativa() - comprometida
        );

        StringBuilder mensaje = new StringBuilder();

        mensaje.append("Recurso: ")
                .append(recurso.getNombre())
                .append("\nFecha: ")
                .append(evento.getFecha().format(FECHA))
                .append("\nHorario consultado: ")
                .append(evento.getHoraInicio().format(HORA))
                .append(" a ")
                .append(evento.getHoraFin().format(HORA))
                .append("\nUnidades en inventario: ")
                .append(recurso.getCantidadTotal())
                .append("\nFuera de servicio: ")
                .append(recurso.getCantidadFueraServicio())
                .append("\nUnidades operativas: ")
                .append(recurso.getCantidadOperativa())
                .append("\nCantidad solicitada para este evento: ")
                .append(cantidad)
                .append("\nMáximo uso simultáneo en otros eventos: ")
                .append(comprometida)
                .append("\nMáximo total que puede reservar este evento: ")
                .append(disponible)
                .append(".");

        List<Evento> reservas = obtenerReservasCoincidentes(
                recurso,
                evento
        );

        if (reservas.isEmpty()) {
            mensaje.append(
                    "\n\nNo hay reservas de otros eventos "
                            + "que coincidan con este horario."
            );
        } else {
            mensaje.append(
                    "\n\nReservas que coinciden con alguna parte "
                            + "del horario consultado:"
            );

            for (Evento otro : reservas) {
                mensaje.append("\n- ")
                        .append(otro.getNombre())
                        .append(" (id ")
                        .append(otro.getId())
                        .append("): ")
                        .append(otro.getHoraInicio().format(HORA))
                        .append(" a ")
                        .append(otro.getHoraFin().format(HORA))
                        .append(", ")
                        .append(otro.obtenerCantidadAsignada(recurso))
                        .append(" unidad(es).");
            }

            mensaje.append(
                    "\n\nLas reservas pueden ocupar tramos diferentes. "
                            + "El cálculo usa el máximo simultáneo, "
                            + "no la suma de toda la lista."
            );
        }

        mensaje.append(
                "\nEl máximo para este evento incluye su reserva actual, "
                        + "si ya tenía una."
        );

        return mensaje.toString();
    }

    private List<Evento> obtenerReservasCoincidentes(
            Recurso recurso,
            Evento evento
    ) {
        return obtenerEventosOrdenados().stream()
                .filter(otro -> otro.getId() != evento.getId())
                .filter(otro -> otro.seSuperponeCon(evento))
                .filter(otro -> otro.obtenerCantidadAsignada(recurso) > 0)
                .toList();
    }

    // calcula el máximo uso simultáneo dentro del horario consultado
    private int calcularCantidadComprometida(
            Recurso recurso,
            Evento eventoActual
    ) {
        Map<LocalTime, Long> movimientos = new TreeMap<>();

        for (Evento otroEvento : eventos) {
            if (otroEvento.getId() == eventoActual.getId()
                    || !otroEvento.seSuperponeCon(eventoActual)) {
                continue;
            }

            int cantidad =
                    otroEvento.obtenerCantidadAsignada(recurso);

            if (cantidad == 0) {
                continue;
            }

            LocalTime inicio = otroEvento.getHoraInicio()
                    .isAfter(eventoActual.getHoraInicio())
                    ? otroEvento.getHoraInicio()
                    : eventoActual.getHoraInicio();

            LocalTime fin = otroEvento.getHoraFin()
                    .isBefore(eventoActual.getHoraFin())
                    ? otroEvento.getHoraFin()
                    : eventoActual.getHoraFin();

            movimientos.merge(
                    inicio,
                    (long) cantidad,
                    Long::sum
            );

            movimientos.merge(
                    fin,
                    -(long) cantidad,
                    Long::sum
            );
        }

        long cantidadEnUso = 0;
        long maximoEnUso = 0;

        // las entradas y salidas a la misma hora se compensan
        for (long cambio : movimientos.values()) {
            cantidadEnUso += cambio;
            maximoEnUso = Math.max(maximoEnUso, cantidadEnUso);
        }

        return (int) Math.min(
                Integer.MAX_VALUE,
                maximoEnUso
        );
    }

    public List<Evento> obtenerEventosOrdenados() {
        return eventos.stream()
                .sorted(
                        Comparator.comparing(Evento::getFecha)
                                .thenComparing(Evento::getHoraInicio)
                )
                .toList();
    }

    public List<Evento> obtenerEventosFuturos() {
        return obtenerEventosOrdenados().stream()
                .filter(evento -> !evento.esPasado())
                .toList();
    }

    public List<Evento> obtenerEventosPasados() {
        return obtenerEventosOrdenados().stream()
                .filter(Evento::esPasado)
                .toList();
    }

    public List<Evento> obtenerEventosProximos(int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de días no puede ser negativa."
            );
        }

        LocalDate fechaLimite = LocalDate.now().plusDays(dias);

        return obtenerEventosOrdenados().stream()
                .filter(evento -> !evento.esPasado())
                .filter(
                        evento ->
                                !evento.getFecha().isAfter(fechaLimite)
                )
                .toList();
    }

    public List<Evento> obtenerEventosPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha de consulta es obligatoria."
            );
        }

        return obtenerEventosOrdenados().stream()
                .filter(evento -> evento.getFecha().equals(fecha))
                .toList();
    }

    public List<Recurso> obtenerRecursos() {
        return List.copyOf(recursos);
    }

    public int obtenerSiguienteIdEvento() {
        return eventos.stream()
                .mapToInt(Evento::getId)
                .max()
                .orElse(0) + 1;
    }

    public int obtenerSiguienteIdRecurso() {
        return recursos.stream()
                .mapToInt(Recurso::getId)
                .max()
                .orElse(0) + 1;
    }

    public int obtenerSiguienteIdAsistente() {
        return eventos.stream()
                .flatMap(evento -> evento.getAsistentes().stream())
                .mapToInt(Asistente::getId)
                .max()
                .orElse(0) + 1;
    }
}