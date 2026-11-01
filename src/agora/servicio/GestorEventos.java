package agora.servicio;

import agora.modelo.Asistente;
import agora.modelo.Evento;
import agora.modelo.Recurso;
import agora.persistencia.PersistenciaTXT;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GestorEventos {

    private final List<Evento> eventos;
    private final List<Recurso> recursos;
    private final PersistenciaTXT persistencia;

    public GestorEventos() {
        eventos = new ArrayList<>();
        recursos = new ArrayList<>();
        persistencia = new PersistenciaTXT("datos");
    }

    // la carga se realiza antes de empezar a registrar información
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

    // revisa los recursos antes de modificar el evento original
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
        Evento evento = buscarEventoPorId(id);

        if (evento == null) {
            throw new IllegalArgumentException(
                    "No se encontró el evento que querés modificar."
            );
        }

        // permite validar la propuesta sin tocar los datos guardados
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

        if (capacidad < evento.getAsistentes().size()) {
            throw new IllegalArgumentException(
                    "La capacidad no puede ser menor "
                            + "a la cantidad de asistentes registrados."
            );
        }

        // también comprueba que las tareas entren en el nuevo horario
        for (Map.Entry<LocalTime, String> tarea
                : evento.getAgenda().entrySet()) {

            propuesta.agregarTarea(
                    tarea.getKey(),
                    tarea.getValue()
            );
        }

        for (Map.Entry<Recurso, Integer> asignacion
                : evento.getRecursosAsignados().entrySet()) {

            Recurso recurso = asignacion.getKey();
            int cantidadAsignada = asignacion.getValue();

            int comprometida = calcularCantidadComprometida(
                    recurso,
                    propuesta
            );

            int disponible =
                    recurso.getCantidadTotal() - comprometida;

            if (cantidadAsignada > disponible) {
                throw new IllegalArgumentException(
                        "No se puede cambiar la fecha o el horario.\n"
                                + "El recurso \""
                                + recurso.getNombre()
                                + "\" está comprometido en otro evento.\n"
                                + "Cantidad necesaria: "
                                + cantidadAsignada
                                + ". Disponible en ese horario: "
                                + Math.max(0, disponible)
                                + "."
                );
            }
        }

        // solo modifica el evento cuando todas las comprobaciones pasan
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

        if (evento == null) {
            return false;
        }

        return eventos.remove(evento);
    }

    public boolean inscribirAsistente(
            int idEvento,
            Asistente asistente
    ) {
        Evento evento = buscarEventoPorId(idEvento);

        if (evento == null) {
            return false;
        }

        // el evento controla los documentos duplicados y el cupo
        return evento.inscribirAsistente(asistente);
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

        int cantidadComprometida =
                calcularCantidadComprometida(recurso, evento);

        int cantidadDisponible =
                recurso.getCantidadTotal() - cantidadComprometida;

        if (cantidad > cantidadDisponible) {
            return false;
        }

        // reemplaza la cantidad anterior si el recurso ya estaba asignado
        evento.asignarRecurso(recurso, cantidad);
        return true;
    }

    // calcula el máximo uso simultáneo dentro del horario consultado
    private int calcularCantidadComprometida(
            Recurso recurso,
            Evento eventoActual
    ) {
        Map<LocalTime, Long> movimientos = new TreeMap<>();

        for (Evento otroEvento : eventos) {
            // excluye el mismo evento, incluso al revisar una propuesta
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

        return (int) Math.min(Integer.MAX_VALUE, maximoEnUso);
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

    // incluye los eventos de hoy que todavía no terminaron
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

    // devuelve una copia para no modificar la colección desde afuera
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