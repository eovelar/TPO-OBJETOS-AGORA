package agora.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class Evento {

    private final int id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int capacidad;

    private final List<Asistente> asistentes;
    private final Map<Recurso, Integer> recursosAsignados;
    private final Map<LocalTime, String> agenda;

    public Evento(
            int id,
            String nombre,
            String descripcion,
            String ubicacion,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            int capacidad
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El id debe ser mayor que cero."
            );
        }

        validarDatos(
                nombre, ubicacion, fecha,
                horaInicio, horaFin, capacidad
        );

        this.id = id;
        this.nombre = nombre.trim();
        this.descripcion = limpiarTexto(descripcion);
        this.ubicacion = ubicacion.trim();
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.capacidad = capacidad;

        asistentes = new ArrayList<>();
        recursosAsignados = new LinkedHashMap<>();
        agenda = new TreeMap<>();
    }

    public void actualizarDatos(
            String nombre,
            String descripcion,
            String ubicacion,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            int capacidad
    ) {
        validarDatos(
                nombre, ubicacion, fecha,
                horaInicio, horaFin, capacidad
        );

        if (capacidad < asistentes.size()) {
            throw new IllegalArgumentException(
                    "La capacidad no puede ser menor "
                            + "a la cantidad de asistentes registrados."
            );
        }

        // comprueba la agenda antes de cambiar el horario
        for (LocalTime horaTarea : agenda.keySet()) {
            if (horaTarea.isBefore(horaInicio)
                    || horaTarea.isAfter(horaFin)) {

                throw new IllegalArgumentException(
                        "El nuevo horario deja una tarea fuera del evento: "
                                + horaTarea
                                + ". Revisá la agenda antes de modificarlo."
                );
            }
        }

        this.nombre = nombre.trim();
        this.descripcion = limpiarTexto(descripcion);
        this.ubicacion = ubicacion.trim();
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.capacidad = capacidad;
    }

    private void validarDatos(
            String nombre,
            String ubicacion,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            int capacidad
    ) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del evento es obligatorio."
            );
        }

        if (ubicacion == null || ubicacion.isBlank()) {
            throw new IllegalArgumentException(
                    "La ubicación es obligatoria."
            );
        }

        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException(
                    "La fecha y los horarios son obligatorios."
            );
        }

        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException(
                    "La hora de finalización debe ser "
                            + "posterior a la hora de inicio."
            );
        }

        if (capacidad <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor que cero."
            );
        }
    }

    public boolean inscribirAsistente(Asistente asistente) {
        if (asistente == null) {
            throw new IllegalArgumentException(
                    "El asistente es obligatorio."
            );
        }

        // un mismo documento no puede inscribirse dos veces
        for (Asistente registrado : asistentes) {
            if (registrado.getDocumento().equals(
                    asistente.getDocumento()
            )) {
                throw new IllegalArgumentException(
                        "Ya hay un asistente inscripto en este evento "
                                + "con el documento "
                                + asistente.getDocumento() + "."
                );
            }
        }

        if (asistentes.contains(asistente)) {
            throw new IllegalArgumentException(
                    "El asistente ya está inscripto en este evento."
            );
        }

        if (!hayCupo()) {
            return false;
        }

        asistentes.add(asistente);
        return true;
    }

    public boolean quitarAsistente(Asistente asistente) {
        return asistentes.remove(asistente);
    }

    public boolean hayCupo() {
        return asistentes.size() < capacidad;
    }

    public void asignarRecurso(Recurso recurso, int cantidad) {
        if (recurso == null) {
            throw new IllegalArgumentException(
                    "El recurso es obligatorio."
            );
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        if (cantidad > recurso.getCantidadTotal()) {
            throw new IllegalArgumentException(
                    "La cantidad solicitada supera "
                            + "el total registrado del recurso."
            );
        }

        // si ya estaba asignado, reemplaza la cantidad anterior
        recursosAsignados.put(recurso, cantidad);
    }

    public void quitarRecurso(Recurso recurso) {
        recursosAsignados.remove(recurso);
    }

    public int obtenerCantidadAsignada(Recurso recurso) {
        return recursosAsignados.getOrDefault(recurso, 0);
    }

    public void agregarTarea(
            LocalTime hora,
            String descripcionTarea
    ) {
        if (hora == null) {
            throw new IllegalArgumentException(
                    "La hora de la tarea es obligatoria."
            );
        }

        if (descripcionTarea == null || descripcionTarea.isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción de la tarea es obligatoria."
            );
        }

        if (hora.isBefore(horaInicio) || hora.isAfter(horaFin)) {
            throw new IllegalArgumentException(
                    "La tarea debe realizarse dentro "
                            + "del horario del evento."
            );
        }

        // mantiene las tareas ordenadas por hora
        agenda.put(hora, descripcionTarea.trim());
    }

    public void quitarTarea(LocalTime hora) {
        agenda.remove(hora);
    }

    public boolean seSuperponeCon(Evento otroEvento) {
        if (otroEvento == null || !fecha.equals(otroEvento.fecha)) {
            return false;
        }

        // si uno termina cuando empieza el otro, no se superponen
        return horaInicio.isBefore(otroEvento.horaFin)
                && horaFin.isAfter(otroEvento.horaInicio);
    }

    public boolean esPasado() {
        LocalDateTime finalizacion =
                LocalDateTime.of(fecha, horaFin);

        return finalizacion.isBefore(LocalDateTime.now());
    }

    private String limpiarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public int getCapacidad() {
        return capacidad;
    }

    // permite consultar las colecciones sin modificarlas directamente
    public List<Asistente> getAsistentes() {
        return Collections.unmodifiableList(asistentes);
    }

    public Map<Recurso, Integer> getRecursosAsignados() {
        return Collections.unmodifiableMap(recursosAsignados);
    }

    public Map<LocalTime, String> getAgenda() {
        return Collections.unmodifiableMap(agenda);
    }

    @Override
    public String toString() {
        return nombre + " - " + fecha + " " + horaInicio;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Evento)) {
            return false;
        }

        Evento otro = (Evento) objeto;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}