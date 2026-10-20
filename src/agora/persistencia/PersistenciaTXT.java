package agora.persistencia;

import agora.modelo.Asistente;
import agora.modelo.Evento;
import agora.modelo.Recurso;
import agora.servicio.GestorEventos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PersistenciaTXT {

    private final Path carpetaDatos;

    public PersistenciaTXT(String rutaCarpeta) {
        carpetaDatos = Path.of(rutaCarpeta);
    }

    public void guardarDatos(GestorEventos gestor) throws IOException {
        Files.createDirectories(carpetaDatos);

        guardarEventos(gestor);
        guardarAsistentes(gestor);
        guardarRecursos(gestor);
        guardarAsignaciones(gestor);
        guardarAgenda(gestor);
    }

    public void cargarDatos(GestorEventos gestor) throws IOException {
        Files.createDirectories(carpetaDatos);

        cargarRecursos(gestor);
        cargarEventos(gestor);
        cargarAsistentes(gestor);
        cargarAsignaciones(gestor);
        cargarAgenda(gestor);
    }

    private void guardarEventos(GestorEventos gestor) throws IOException {
        List<String> lineas = new ArrayList<>();

        for (Evento evento : gestor.obtenerEventosOrdenados()) {
            String linea = evento.getId() + ";"
                    + limpiar(evento.getNombre()) + ";"
                    + limpiar(evento.getDescripcion()) + ";"
                    + limpiar(evento.getUbicacion()) + ";"
                    + evento.getFecha() + ";"
                    + evento.getHoraInicio() + ";"
                    + evento.getHoraFin() + ";"
                    + evento.getCapacidad();

            lineas.add(linea);
        }

        escribirArchivo("eventos.txt", lineas);
    }

    private void guardarAsistentes(GestorEventos gestor) throws IOException {
        List<String> lineas = new ArrayList<>();

        for (Evento evento : gestor.obtenerEventosOrdenados()) {
            for (Asistente asistente : evento.getAsistentes()) {
                String linea = evento.getId() + ";"
                        + asistente.getId() + ";"
                        + limpiar(asistente.getNombreCompleto()) + ";"
                        + limpiar(asistente.getDocumento()) + ";"
                        + limpiar(asistente.getCargo()) + ";"
                        + limpiar(asistente.getInstitucion()) + ";"
                        + limpiar(asistente.getEmail());

                lineas.add(linea);
            }
        }

        escribirArchivo("asistentes.txt", lineas);
    }

    private void guardarRecursos(GestorEventos gestor) throws IOException {
        List<String> lineas = new ArrayList<>();

        for (Recurso recurso : gestor.obtenerRecursos()) {
            String linea = recurso.getId() + ";"
                    + limpiar(recurso.getNombre()) + ";"
                    + limpiar(recurso.getTipo()) + ";"
                    + recurso.getCantidadTotal();

            lineas.add(linea);
        }

        escribirArchivo("recursos.txt", lineas);
    }

    private void guardarAsignaciones(GestorEventos gestor) throws IOException {
        List<String> lineas = new ArrayList<>();

        for (Evento evento : gestor.obtenerEventosOrdenados()) {
            for (Map.Entry<Recurso, Integer> asignacion
                    : evento.getRecursosAsignados().entrySet()) {

                String linea = evento.getId() + ";"
                        + asignacion.getKey().getId() + ";"
                        + asignacion.getValue();

                lineas.add(linea);
            }
        }

        escribirArchivo("asignaciones.txt", lineas);
    }

    private void guardarAgenda(GestorEventos gestor) throws IOException {
        List<String> lineas = new ArrayList<>();

        for (Evento evento : gestor.obtenerEventosOrdenados()) {
            for (Map.Entry<LocalTime, String> tarea
                    : evento.getAgenda().entrySet()) {

                String linea = evento.getId() + ";"
                        + tarea.getKey() + ";"
                        + limpiar(tarea.getValue());

                lineas.add(linea);
            }
        }

        escribirArchivo("agenda.txt", lineas);
    }

    private void cargarEventos(GestorEventos gestor) throws IOException {
        for (String linea : leerArchivo("eventos.txt")) {
            String[] datos = linea.split(";", -1);

            if (datos.length != 8) {
                continue;
            }

            Evento evento = new Evento(
                    Integer.parseInt(datos[0]),
                    datos[1],
                    datos[2],
                    datos[3],
                    LocalDate.parse(datos[4]),
                    LocalTime.parse(datos[5]),
                    LocalTime.parse(datos[6]),
                    Integer.parseInt(datos[7])
            );

            gestor.registrarEvento(evento);
        }
    }

    private void cargarAsistentes(GestorEventos gestor) throws IOException {
        for (String linea : leerArchivo("asistentes.txt")) {
            String[] datos = linea.split(";", -1);

            if (datos.length != 7) {
                continue;
            }

            Evento evento = gestor.buscarEventoPorId(
                    Integer.parseInt(datos[0])
            );

            if (evento == null) {
                continue;
            }

            Asistente asistente = new Asistente(
                    Integer.parseInt(datos[1]),
                    datos[2],
                    datos[3],
                    datos[4],
                    datos[5],
                    datos[6]
            );

            evento.inscribirAsistente(asistente);
        }
    }

    private void cargarRecursos(GestorEventos gestor) throws IOException {
        for (String linea : leerArchivo("recursos.txt")) {
            String[] datos = linea.split(";", -1);

            if (datos.length != 4) {
                continue;
            }

            Recurso recurso = new Recurso(
                    Integer.parseInt(datos[0]),
                    datos[1],
                    datos[2],
                    Integer.parseInt(datos[3])
            );

            gestor.registrarRecurso(recurso);
        }
    }

    private void cargarAsignaciones(GestorEventos gestor) throws IOException {
        for (String linea : leerArchivo("asignaciones.txt")) {
            String[] datos = linea.split(";", -1);

            if (datos.length != 3) {
                continue;
            }

            int idEvento = Integer.parseInt(datos[0]);
            int idRecurso = Integer.parseInt(datos[1]);
            int cantidad = Integer.parseInt(datos[2]);

            gestor.asignarRecurso(idEvento, idRecurso, cantidad);
        }
    }

    private void cargarAgenda(GestorEventos gestor) throws IOException {
        for (String linea : leerArchivo("agenda.txt")) {
            String[] datos = linea.split(";", -1);

            if (datos.length != 3) {
                continue;
            }

            Evento evento = gestor.buscarEventoPorId(
                    Integer.parseInt(datos[0])
            );

            if (evento != null) {
                evento.agregarTarea(
                        LocalTime.parse(datos[1]),
                        datos[2]
                );
            }
        }
    }

    private void escribirArchivo(String nombreArchivo, List<String> lineas)
            throws IOException {

        Path archivo = carpetaDatos.resolve(nombreArchivo);

        Files.write(
                archivo,
                lineas,
                StandardCharsets.UTF_8
        );
    }

    private List<String> leerArchivo(String nombreArchivo)
            throws IOException {

        Path archivo = carpetaDatos.resolve(nombreArchivo);

        if (!Files.exists(archivo)) {
            return List.of();
        }

        return Files.readAllLines(
                archivo,
                StandardCharsets.UTF_8
        );
    }

    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace(";", ",")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}