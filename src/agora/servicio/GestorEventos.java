package agora.servicio;

import agora.modelo.Asistente;
import agora.modelo.Evento;
import agora.modelo.Recurso;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GestorEventos {

    private final List<Evento> eventos;
    private final List<Recurso> recursos;

    public GestorEventos() {
        eventos = new ArrayList<>();
        recursos = new ArrayList<>();
    }

    public void registrarEvento(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento es obligatorio");
        }

        if (buscarEventoPorId(evento.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un evento con ese id");
        }

        eventos.add(evento);
    }

    public void registrarRecurso(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso es obligatorio");
        }

        if (buscarRecursoPorId(recurso.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un recurso con ese id");
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

    public boolean eliminarEvento(int id) {
        Evento evento = buscarEventoPorId(id);

        if (evento == null) {
            return false;
        }

        return eventos.remove(evento);
    }

    public boolean inscribirAsistente(int idEvento, Asistente asistente) {
        Evento evento = buscarEventoPorId(idEvento);

        if (evento == null) {
            return false;
        }

        return evento.inscribirAsistente(asistente);
    }

    public boolean asignarRecurso(int idEvento, int idRecurso, int cantidad) {
        Evento evento = buscarEventoPorId(idEvento);
        Recurso recurso = buscarRecursoPorId(idRecurso);

        if (evento == null || recurso == null || cantidad <= 0) {
            return false;
        }

        int cantidadComprometida = calcularCantidadComprometida(recurso, evento);
        int cantidadDisponible = recurso.getCantidadTotal() - cantidadComprometida;

        if (cantidad > cantidadDisponible) {
            return false;
        }

        evento.asignarRecurso(recurso, cantidad);
        return true;
    }

    private int calcularCantidadComprometida(Recurso recurso, Evento eventoActual) {
        int cantidadComprometida = 0;

        for (Evento otroEvento : eventos) {
            if (!otroEvento.equals(eventoActual)
                    && otroEvento.seSuperponeCon(eventoActual)) {

                cantidadComprometida +=
                        otroEvento.obtenerCantidadAsignada(recurso);
            }
        }

        return cantidadComprometida;
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

    public List<Evento> obtenerEventosPorFecha(LocalDate fecha) {
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