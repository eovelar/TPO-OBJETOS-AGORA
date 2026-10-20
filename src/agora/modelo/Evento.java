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

    private int id;
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

    public Evento(int id, String nombre, String descripcion,
                  String ubicacion, LocalDate fecha,
                  LocalTime horaInicio, LocalTime horaFin,
                  int capacidad) {

        validarDatos(nombre, ubicacion, fecha, horaInicio, horaFin, capacidad);

        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor que cero");
        }

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.capacidad = capacidad;

        this.asistentes = new ArrayList<>();
        this.recursosAsignados = new LinkedHashMap<>();
        this.agenda = new TreeMap<>();
    }

    public void actualizarDatos(String nombre, String descripcion,
                                String ubicacion, LocalDate fecha,
                                LocalTime horaInicio, LocalTime horaFin,
                                int capacidad) {

        validarDatos(nombre, ubicacion, fecha, horaInicio, horaFin, capacidad);

        if (capacidad < asistentes.size()) {
            throw new IllegalArgumentException(
                    "La capacidad no puede ser menor a los asistentes registrados"
            );
        }

        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.capacidad = capacidad;
    }

    private void validarDatos(String nombre, String ubicacion,
                              LocalDate fecha, LocalTime horaInicio,
                              LocalTime horaFin, int capacidad) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (ubicacion == null || ubicacion.isBlank()) {
            throw new IllegalArgumentException("La ubicación es obligatoria");
        }

        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("La fecha y los horarios son obligatorios");
        }

        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException(
                    "La hora de finalización debe ser posterior a la hora de inicio"
            );
        }

        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero");
        }
    }

    public boolean inscribirAsistente(Asistente asistente) {
        if (asistente == null) {
            return false;
        }

        if (asistentes.contains(asistente)) {
            return false;
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
            throw new IllegalArgumentException("El recurso es obligatorio");
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        recursosAsignados.put(recurso, cantidad);
    }

    public void quitarRecurso(Recurso recurso) {
        recursosAsignados.remove(recurso);
    }

    public int obtenerCantidadAsignada(Recurso recurso) {
        return recursosAsignados.getOrDefault(recurso, 0);
    }

    public void agregarTarea(LocalTime hora, String descripcionTarea) {
        if (hora == null) {
            throw new IllegalArgumentException("La hora es obligatoria");
        }

        if (descripcionTarea == null || descripcionTarea.isBlank()) {
            throw new IllegalArgumentException("La descripción de la tarea es obligatoria");
        }

        if (hora.isBefore(horaInicio) || hora.isAfter(horaFin)) {
            throw new IllegalArgumentException(
                    "La tarea debe realizarse dentro del horario del evento"
            );
        }

        agenda.put(hora, descripcionTarea);
    }

    public void quitarTarea(LocalTime hora) {
        agenda.remove(hora);
    }

    public boolean seSuperponeCon(Evento otroEvento) {
        if (otroEvento == null || !fecha.equals(otroEvento.fecha)) {
            return false;
        }

        return horaInicio.isBefore(otroEvento.horaFin)
                && horaFin.isAfter(otroEvento.horaInicio);
    }

    public boolean esPasado() {
        LocalDateTime finalizacion = LocalDateTime.of(fecha, horaFin);
        return finalizacion.isBefore(LocalDateTime.now());
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