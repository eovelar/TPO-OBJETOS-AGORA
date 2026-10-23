package agora.vista;

import agora.modelo.Asistente;
import agora.modelo.Evento;
import agora.modelo.Recurso;
import agora.persistencia.PersistenciaTXT;
import agora.servicio.GestorEventos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class InterfazPrincipal extends JFrame {

    private final GestorEventos gestor;
    private final PersistenciaTXT persistencia;
    private final DefaultListModel<Evento> modeloEventos;
    private final JList<Evento> listaEventos;
    private final JComboBox<String> filtroEventos;

    public InterfazPrincipal() {
        gestor = new GestorEventos();
        persistencia = new PersistenciaTXT("datos");
        modeloEventos = new DefaultListModel<>();
        listaEventos = new JList<>(modeloEventos);

        filtroEventos = new JComboBox<>(
                new String[]{"Todos", "Próximos", "Pasados"}
        );

        configurarVentana();
        crearContenido();
        cargarDatos();
        actualizarListaEventos();
    }

    private void configurarVentana() {
        setTitle("ÁGORA Producciones - Gestión de eventos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 580);
        setLocationRelativeTo(null);
    }

    private void crearContenido() {
        JPanel panelPrincipal = new JPanel(
                new BorderLayout(10, 10)
        );

        panelPrincipal.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        JLabel titulo = new JLabel("Eventos programados");
        titulo.setFont(
                new Font("SansSerif", Font.BOLD, 24)
        );

        JPanel panelSuperior = new JPanel(
                new BorderLayout()
        );

        JPanel panelFiltro = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        panelFiltro.add(new JLabel("Mostrar:"));
        panelFiltro.add(filtroEventos);

        filtroEventos.addActionListener(
                e -> actualizarListaEventos()
        );

        panelSuperior.add(titulo, BorderLayout.WEST);
        panelSuperior.add(panelFiltro, BorderLayout.EAST);

        listaEventos.setFont(
                new Font("SansSerif", Font.PLAIN, 15)
        );

        listaEventos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollEventos =
                new JScrollPane(listaEventos);

        JButton botonAgregar =
                new JButton("Agregar evento");

        JButton botonEditar =
                new JButton("Editar evento");

        JButton botonDetalle =
                new JButton("Ver detalle");

        JButton botonAsistente =
                new JButton("Inscribir asistente");

        JButton botonAgregarTarea =
                new JButton("Agregar tarea");

        JButton botonEliminar =
                new JButton("Eliminar evento");

        JButton botonRegistrarRecurso =
                new JButton("Registrar recurso");

        JButton botonAsignarRecurso =
                new JButton("Asignar recurso");

        JButton botonCalendario =
                new JButton("Calendario diario");

        botonAgregar.addActionListener(
                e -> mostrarFormularioNuevoEvento()
        );

        botonEditar.addActionListener(
                e -> editarEventoSeleccionado()
        );

        botonDetalle.addActionListener(
                e -> mostrarDetalleEvento()
        );

        botonAsistente.addActionListener(
                e -> inscribirAsistente()
        );

        botonAgregarTarea.addActionListener(
                e -> agregarTareaEvento()
        );

        botonEliminar.addActionListener(
                e -> eliminarEventoSeleccionado()
        );

        botonRegistrarRecurso.addActionListener(
                e -> registrarRecurso()
        );

        botonAsignarRecurso.addActionListener(
                e -> asignarRecursoEvento()
        );

        botonCalendario.addActionListener(
                e -> mostrarCalendarioDiario()
        );

        JPanel panelEventos = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        panelEventos.add(botonAgregar);
        panelEventos.add(botonEditar);
        panelEventos.add(botonDetalle);
        panelEventos.add(botonAsistente);
        panelEventos.add(botonAgregarTarea);
        panelEventos.add(botonEliminar);

        JPanel panelHerramientas = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        panelHerramientas.add(botonRegistrarRecurso);
        panelHerramientas.add(botonAsignarRecurso);
        panelHerramientas.add(botonCalendario);

        JPanel panelBotones = new JPanel(
                new GridLayout(2, 1)
        );

        panelBotones.add(panelEventos);
        panelBotones.add(panelHerramientas);

        panelPrincipal.add(
                panelSuperior,
                BorderLayout.NORTH
        );

        panelPrincipal.add(
                scrollEventos,
                BorderLayout.CENTER
        );

        panelPrincipal.add(
                panelBotones,
                BorderLayout.SOUTH
        );

        setContentPane(panelPrincipal);
    }

    private void cargarDatos() {
        try {
            persistencia.cargarDatos(gestor);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudieron cargar los datos guardados.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void actualizarListaEventos() {
        modeloEventos.clear();

        String filtro =
                (String) filtroEventos.getSelectedItem();

        List<Evento> eventos;

        if ("Próximos".equals(filtro)) {
            eventos = gestor.obtenerEventosFuturos();

        } else if ("Pasados".equals(filtro)) {
            eventos = gestor.obtenerEventosPasados();

        } else {
            eventos = gestor.obtenerEventosOrdenados();
        }

        for (Evento evento : eventos) {
            modeloEventos.addElement(evento);
        }
    }

    private void mostrarFormularioNuevoEvento() {
        JTextField campoNombre = new JTextField();
        JTextField campoDescripcion = new JTextField();
        JTextField campoUbicacion = new JTextField();
        JTextField campoFecha =
                new JTextField("2026-11-11");
        JTextField campoHoraInicio =
                new JTextField("11:00");
        JTextField campoHoraFin =
                new JTextField("13:00");
        JTextField campoCapacidad =
                new JTextField("100");

        JPanel formulario = crearFormularioEvento(
                campoNombre,
                campoDescripcion,
                campoUbicacion,
                campoFecha,
                campoHoraInicio,
                campoHoraFin,
                campoCapacidad
        );

        int resultado = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Agregar evento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Evento evento = new Evento(
                    gestor.obtenerSiguienteIdEvento(),
                    campoNombre.getText().trim(),
                    campoDescripcion.getText().trim(),
                    campoUbicacion.getText().trim(),
                    LocalDate.parse(
                            campoFecha.getText().trim()
                    ),
                    LocalTime.parse(
                            campoHoraInicio.getText().trim()
                    ),
                    LocalTime.parse(
                            campoHoraFin.getText().trim()
                    ),
                    Integer.parseInt(
                            campoCapacidad.getText().trim()
                    )
            );

            gestor.registrarEvento(evento);
            persistencia.guardarDatos(gestor);
            actualizarListaEventos();

            JOptionPane.showMessageDialog(
                    this,
                    "El evento fue registrado correctamente."
            );

        } catch (IOException e) {
            mostrarErrorArchivos();

        } catch (RuntimeException e) {
            mostrarErrorDatos(e);
        }
    }

    private void editarEventoSeleccionado() {
        Evento evento = listaEventos.getSelectedValue();

        if (evento == null) {
            mostrarAvisoSeleccion();
            return;
        }

        JTextField campoNombre =
                new JTextField(evento.getNombre());

        JTextField campoDescripcion =
                new JTextField(evento.getDescripcion());

        JTextField campoUbicacion =
                new JTextField(evento.getUbicacion());

        JTextField campoFecha =
                new JTextField(
                        evento.getFecha().toString()
                );

        JTextField campoHoraInicio =
                new JTextField(
                        evento.getHoraInicio().toString()
                );

        JTextField campoHoraFin =
                new JTextField(
                        evento.getHoraFin().toString()
                );

        JTextField campoCapacidad =
                new JTextField(
                        String.valueOf(evento.getCapacidad())
                );

        JPanel formulario = crearFormularioEvento(
                campoNombre,
                campoDescripcion,
                campoUbicacion,
                campoFecha,
                campoHoraInicio,
                campoHoraFin,
                campoCapacidad
        );

        int resultado = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Editar evento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            evento.actualizarDatos(
                    campoNombre.getText().trim(),
                    campoDescripcion.getText().trim(),
                    campoUbicacion.getText().trim(),
                    LocalDate.parse(
                            campoFecha.getText().trim()
                    ),
                    LocalTime.parse(
                            campoHoraInicio.getText().trim()
                    ),
                    LocalTime.parse(
                            campoHoraFin.getText().trim()
                    ),
                    Integer.parseInt(
                            campoCapacidad.getText().trim()
                    )
            );

            persistencia.guardarDatos(gestor);
            actualizarListaEventos();

            JOptionPane.showMessageDialog(
                    this,
                    "El evento fue actualizado correctamente."
            );

        } catch (IOException e) {
            mostrarErrorArchivos();

        } catch (RuntimeException e) {
            mostrarErrorDatos(e);
        }
    }

    private void inscribirAsistente() {
        Evento evento = listaEventos.getSelectedValue();

        if (evento == null) {
            mostrarAvisoSeleccion();
            return;
        }

        JTextField campoNombre = new JTextField();
        JTextField campoDocumento = new JTextField();
        JTextField campoCargo = new JTextField();
        JTextField campoInstitucion = new JTextField();
        JTextField campoEmail = new JTextField();

        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );

        formulario.add(new JLabel("Nombre completo:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("Documento:"));
        formulario.add(campoDocumento);

        formulario.add(new JLabel("Cargo:"));
        formulario.add(campoCargo);

        formulario.add(new JLabel("Institución:"));
        formulario.add(campoInstitucion);

        formulario.add(new JLabel("Correo electrónico:"));
        formulario.add(campoEmail);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Inscribir asistente en " +
                        evento.getNombre(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Asistente asistente = new Asistente(
                    gestor.obtenerSiguienteIdAsistente(),
                    campoNombre.getText().trim(),
                    campoDocumento.getText().trim(),
                    campoCargo.getText().trim(),
                    campoInstitucion.getText().trim(),
                    campoEmail.getText().trim()
            );

            gestor.inscribirAsistente(
                    evento.getId(),
                    asistente
            );

            persistencia.guardarDatos(gestor);
            actualizarListaEventos();

            JOptionPane.showMessageDialog(
                    this,
                    "El asistente fue registrado correctamente."
            );

        } catch (IOException e) {
            mostrarErrorArchivos();

        } catch (RuntimeException e) {
            mostrarErrorDatos(e);
        }
    }

    private void registrarRecurso() {
        JTextField campoNombre = new JTextField();
        JTextField campoTipo = new JTextField();
        JTextField campoCantidad = new JTextField("1");

        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );

        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("Tipo:"));
        formulario.add(campoTipo);

        formulario.add(new JLabel("Cantidad disponible:"));
        formulario.add(campoCantidad);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Registrar recurso",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Recurso recurso = new Recurso(
                    gestor.obtenerSiguienteIdRecurso(),
                    campoNombre.getText().trim(),
                    campoTipo.getText().trim(),
                    Integer.parseInt(
                            campoCantidad.getText().trim()
                    )
            );

            gestor.registrarRecurso(recurso);
            persistencia.guardarDatos(gestor);

            JOptionPane.showMessageDialog(
                    this,
                    "El recurso fue registrado correctamente."
            );

        } catch (IOException e) {
            mostrarErrorArchivos();

        } catch (RuntimeException e) {
            mostrarErrorDatos(e);
        }
    }

    private void asignarRecursoEvento() {
        Evento evento = listaEventos.getSelectedValue();

        if (evento == null) {
            mostrarAvisoSeleccion();
            return;
        }

        if (gestor.obtenerRecursos().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero tenés que registrar un recurso.",
                    "No hay recursos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JComboBox<Recurso> comboRecursos =
                new JComboBox<>(
                        gestor.obtenerRecursos()
                                .toArray(new Recurso[0])
                );

        JTextField campoCantidad = new JTextField("1");

        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );

        formulario.add(new JLabel("Recurso:"));
        formulario.add(comboRecursos);

        formulario.add(new JLabel("Cantidad a asignar:"));
        formulario.add(campoCantidad);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Asignar recurso a " + evento.getNombre(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Recurso recursoSeleccionado =
                    (Recurso) comboRecursos
                            .getSelectedItem();

            if (recursoSeleccionado == null) {
                return;
            }

            int cantidad = Integer.parseInt(
                    campoCantidad.getText().trim()
            );

            gestor.asignarRecurso(
                    evento.getId(),
                    recursoSeleccionado.getId(),
                    cantidad
            );

            persistencia.guardarDatos(gestor);

            JOptionPane.showMessageDialog(
                    this,
                    "El recurso fue asignado correctamente."
            );

        } catch (IOException e) {
            mostrarErrorArchivos();

        } catch (RuntimeException e) {
            mostrarErrorDatos(e);
        }
    }

    private void agregarTareaEvento() {
        Evento evento = listaEventos.getSelectedValue();

        if (evento == null) {
            mostrarAvisoSeleccion();
            return;
        }

        JTextField campoHora = new JTextField(
                evento.getHoraInicio().toString()
        );

        JTextField campoDescripcion =
                new JTextField();

        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );

        formulario.add(new JLabel("Hora (HH:MM):"));
        formulario.add(campoHora);

        formulario.add(
                new JLabel("Descripción de la tarea:")
        );
        formulario.add(campoDescripcion);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Agregar tarea a " + evento.getNombre(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            evento.agregarTarea(
                    LocalTime.parse(
                            campoHora.getText().trim()
                    ),
                    campoDescripcion.getText().trim()
            );

            persistencia.guardarDatos(gestor);

            JOptionPane.showMessageDialog(
                    this,
                    "La tarea fue agregada correctamente."
            );

        } catch (IOException e) {
            mostrarErrorArchivos();

        } catch (RuntimeException e) {
            mostrarErrorDatos(e);
        }
    }

    private void mostrarCalendarioDiario() {
        String fechaIngresada =
                JOptionPane.showInputDialog(
                        this,
                        "Ingresá la fecha que querés consultar:",
                        LocalDate.now().toString()
                );

        if (fechaIngresada == null) {
            return;
        }

        try {
            LocalDate fecha = LocalDate.parse(
                    fechaIngresada.trim()
            );

            JTextArea calendario = new JTextArea();
            calendario.setEditable(false);
            calendario.setFont(
                    new Font(
                            "SansSerif",
                            Font.PLAIN,
                            14
                    )
            );

            calendario.setMargin(
                    new Insets(10, 10, 10, 10)
            );

            calendario.append(
                    "CALENDARIO DEL DÍA " +
                            fecha +
                            "\n\n"
            );

            List<Evento> eventosDelDia =
                    gestor.obtenerEventosPorFecha(fecha);

            if (eventosDelDia.isEmpty()) {
                calendario.append(
                        "No hay eventos programados para esta fecha."
                );

            } else {
                for (Evento evento : eventosDelDia) {
                    calendario.append(
                            evento.getHoraInicio() +
                                    " a " +
                                    evento.getHoraFin() +
                                    " | " +
                                    evento.getNombre() +
                                    "\n"
                    );

                    calendario.append(
                            "Ubicación: " +
                                    evento.getUbicacion() +
                                    "\n"
                    );

                    if (evento.getAgenda().isEmpty()) {
                        calendario.append(
                                "Sin tareas registradas.\n"
                        );

                    } else {
                        evento.getAgenda().forEach(
                                (hora, tarea) ->
                                        calendario.append(
                                                "  " +
                                                        hora +
                                                        " - " +
                                                        tarea +
                                                        "\n"
                                        )
                        );
                    }

                    calendario.append("\n");
                }
            }

            JScrollPane scroll =
                    new JScrollPane(calendario);

            scroll.setPreferredSize(
                    new Dimension(550, 400)
            );

            JOptionPane.showMessageDialog(
                    this,
                    scroll,
                    "Calendario diario",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ingresá una fecha válida con el formato AAAA-MM-DD.",
                    "Fecha incorrecta",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void mostrarDetalleEvento() {
        Evento evento = listaEventos.getSelectedValue();

        if (evento == null) {
            mostrarAvisoSeleccion();
            return;
        }

        JTextArea detalle = new JTextArea();
        detalle.setEditable(false);
        detalle.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );
        detalle.setLineWrap(true);
        detalle.setWrapStyleWord(true);
        detalle.setMargin(
                new Insets(10, 10, 10, 10)
        );

        detalle.append(
                "Nombre: " +
                        evento.getNombre() +
                        "\n\n"
        );

        detalle.append(
                "Descripción: " +
                        evento.getDescripcion() +
                        "\n\n"
        );

        detalle.append(
                "Ubicación: " +
                        evento.getUbicacion() +
                        "\n"
        );

        detalle.append(
                "Fecha: " +
                        evento.getFecha() +
                        "\n"
        );

        detalle.append(
                "Horario: " +
                        evento.getHoraInicio() +
                        " a " +
                        evento.getHoraFin() +
                        "\n"
        );

        detalle.append(
                "Capacidad: " +
                        evento.getCapacidad() +
                        "\n\n"
        );

        detalle.append("ASISTENTES\n");

        if (evento.getAsistentes().isEmpty()) {
            detalle.append(
                    "No hay asistentes registrados.\n"
            );

        } else {
            for (Asistente asistente :
                    evento.getAsistentes()) {

                detalle.append(
                        asistente.getNombreCompleto() +
                                " - " +
                                asistente.getCargo() +
                                " - " +
                                asistente.getInstitucion() +
                                "\n"
                );
            }
        }

        detalle.append("\nAGENDA\n");

        if (evento.getAgenda().isEmpty()) {
            detalle.append(
                    "No hay tareas registradas.\n"
            );

        } else {
            evento.getAgenda().forEach(
                    (hora, tarea) ->
                            detalle.append(
                                    hora +
                                            " - " +
                                            tarea +
                                            "\n"
                            )
            );
        }

        detalle.append("\nRECURSOS\n");

        if (evento.getRecursosAsignados().isEmpty()) {
            detalle.append(
                    "No hay recursos asignados.\n"
            );

        } else {
            evento.getRecursosAsignados().forEach(
                    (recurso, cantidad) ->
                            detalle.append(
                                    recurso.getNombre() +
                                            ": " +
                                            cantidad +
                                            "\n"
                            )
            );
        }

        JScrollPane scroll =
                new JScrollPane(detalle);

        scroll.setPreferredSize(
                new Dimension(520, 400)
        );

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Detalle del evento",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private JPanel crearFormularioEvento(
            JTextField campoNombre,
            JTextField campoDescripcion,
            JTextField campoUbicacion,
            JTextField campoFecha,
            JTextField campoHoraInicio,
            JTextField campoHoraFin,
            JTextField campoCapacidad
    ) {
        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );

        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("Descripción:"));
        formulario.add(campoDescripcion);

        formulario.add(new JLabel("Ubicación:"));
        formulario.add(campoUbicacion);

        formulario.add(
                new JLabel("Fecha (AAAA-MM-DD):")
        );
        formulario.add(campoFecha);

        formulario.add(
                new JLabel("Hora de inicio (HH:MM):")
        );
        formulario.add(campoHoraInicio);

        formulario.add(
                new JLabel("Hora de finalización (HH:MM):")
        );
        formulario.add(campoHoraFin);

        formulario.add(new JLabel("Capacidad:"));
        formulario.add(campoCapacidad);

        return formulario;
    }

    private void eliminarEventoSeleccionado() {
        Evento eventoSeleccionado =
                listaEventos.getSelectedValue();

        if (eventoSeleccionado == null) {
            mostrarAvisoSeleccion();
            return;
        }

        int confirmacion =
                JOptionPane.showConfirmDialog(
                        this,
                        "¿Querés eliminar el evento \"" +
                                eventoSeleccionado.getNombre() +
                                "\"?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            gestor.eliminarEvento(
                    eventoSeleccionado.getId()
            );

            persistencia.guardarDatos(gestor);
            actualizarListaEventos();

            JOptionPane.showMessageDialog(
                    this,
                    "El evento fue eliminado correctamente."
            );

        } catch (IOException e) {
            mostrarErrorArchivos();
        }
    }

    private void mostrarAvisoSeleccion() {
        JOptionPane.showMessageDialog(
                this,
                "Seleccioná un evento de la lista.",
                "Evento no seleccionado",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void mostrarErrorArchivos() {
        JOptionPane.showMessageDialog(
                this,
                "No se pudieron actualizar los archivos.",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void mostrarErrorDatos(
            RuntimeException e
    ) {
        JOptionPane.showMessageDialog(
                this,
                "Revisá los datos ingresados.\n" +
                        e.getMessage(),
                "Datos incorrectos",
                JOptionPane.WARNING_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazPrincipal ventana =
                    new InterfazPrincipal();

            ventana.setVisible(true);
        });
    }
}