package agora.vista;

import agora.modelo.Asistente;
import agora.modelo.Evento;
import agora.modelo.Recurso;
import agora.servicio.GestorEventos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class InterfazPrincipal extends JFrame {

    private static final Color FONDO = new Color(245, 246, 248);
    private static final Color AZUL = new Color(18, 62, 82);
    private static final Color DORADO = new Color(196, 154, 69);
    private static final Color BORDE = new Color(225, 230, 234);
    private static final Color GRIS = new Color(108, 119, 130);
    private static final Color ROJO = new Color(157, 66, 66);
    private static final Color SELECCION = new Color(234, 242, 246);

    private static final Locale IDIOMA =
            Locale.forLanguageTag("es-AR");

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private final GestorEventos gestor;
    private final DefaultListModel<Evento> modeloEventos;
    private final JList<Evento> listaEventos;
    private final JComboBox<String> filtroEventos;
    private final JLabel resumenEventos;
    private final JLabel estadoSeleccion;

    private JButton botonEditar;
    private JButton botonDetalle;
    private JButton botonAsistente;
    private JButton botonTarea;
    private JButton botonAsignar;
    private JButton botonEliminar;

    private YearMonth mesCalendario;
    private LocalDate fechaCalendario;

    public InterfazPrincipal() {
        gestor = new GestorEventos();
        modeloEventos = new DefaultListModel<>();
        listaEventos = new JList<>(modeloEventos);
        resumenEventos = new JLabel();
        estadoSeleccion = new JLabel("Seleccioná un evento");

        filtroEventos = new JComboBox<>(
                new String[]{"Todos", "Próximos", "Pasados"}
        );

        configurarEstilo();
        configurarVentana();
        crearContenido();
        configurarListado();
        cargarDatos();
        actualizarListaEventos();
    }

    // estilo compartido por los formularios y mensajes
    private void configurarEstilo() {
        Font fuente = new Font("SansSerif", Font.PLAIN, 14);

        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", AZUL);
        UIManager.put("OptionPane.messageFont", fuente);
        UIManager.put("Label.font", fuente);
        UIManager.put("Label.foreground", AZUL);
        UIManager.put("Button.font", fuente);
        UIManager.put("TextField.font", fuente);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", AZUL);
        UIManager.put("TextField.caretForeground", AZUL);
        UIManager.put("ComboBox.font", fuente);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", AZUL);
        UIManager.put("ToolTip.font", fuente);
    }

    private void configurarVentana() {
        setTitle("ÁGORA Producciones - Gestión de eventos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
    }

    // separa el listado de las acciones para que no quede recargado
    private void crearContenido() {
        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(FONDO);
        principal.add(crearEncabezado(), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(20, 0));
        cuerpo.setBackground(FONDO);
        cuerpo.setBorder(new EmptyBorder(24, 28, 20, 28));
        cuerpo.add(crearPanelEventos(), BorderLayout.CENTER);
        cuerpo.add(crearPanelAcciones(), BorderLayout.EAST);

        principal.add(cuerpo, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(FONDO);
        pie.setBorder(new EmptyBorder(0, 30, 18, 30));

        resumenEventos.setForeground(GRIS);
        resumenEventos.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        JLabel indicacion = new JLabel(
                "Seleccioná una actividad para administrarla"
        );
        indicacion.setForeground(GRIS);
        indicacion.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        pie.add(resumenEventos, BorderLayout.WEST);
        pie.add(indicacion, BorderLayout.EAST);
        principal.add(pie, BorderLayout.SOUTH);

        setContentPane(principal);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(AZUL);
        encabezado.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 3, 0, DORADO
                        ),
                        new EmptyBorder(18, 30, 18, 30)
                )
        );

        JPanel marca = new JPanel();
        marca.setLayout(new BoxLayout(marca, BoxLayout.Y_AXIS));
        marca.setOpaque(false);

        JLabel nombre = new JLabel("ÁGORA");
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font("Serif", Font.BOLD, 30));

        JLabel subtitulo = new JLabel("P R O D U C C I O N E S");
        subtitulo.setForeground(new Color(215, 225, 230));
        subtitulo.setFont(
                new Font("SansSerif", Font.PLAIN, 10)
        );

        marca.add(nombre);
        marca.add(subtitulo);

        JLabel seccion = new JLabel("Gestión de eventos");
        seccion.setForeground(Color.WHITE);
        seccion.setFont(
                new Font("SansSerif", Font.PLAIN, 17)
        );

        encabezado.add(marca, BorderLayout.WEST);
        encabezado.add(seccion, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearPanelEventos() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 18));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDE),
                        new EmptyBorder(22, 22, 18, 22)
                )
        );

        JPanel cabecera = new JPanel(new BorderLayout(10, 16));
        cabecera.setOpaque(false);

        JPanel titulos = new JPanel();
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.setOpaque(false);

        JLabel titulo = new JLabel("Agenda de eventos");
        titulo.setFont(
                new Font("SansSerif", Font.BOLD, 24)
        );
        titulo.setForeground(AZUL);

        JLabel descripcion = new JLabel(
                "Actividades, horarios y lugares de encuentro"
        );
        descripcion.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        descripcion.setForeground(GRIS);

        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(5));
        titulos.add(descripcion);

        JPanel filtros = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );
        filtros.setOpaque(false);

        JLabel mostrar = new JLabel("Mostrar eventos  ");
        mostrar.setForeground(GRIS);
        mostrar.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        filtroEventos.setPreferredSize(new Dimension(150, 34));
        filtroEventos.addActionListener(
                e -> actualizarListaEventos()
        );

        filtros.add(mostrar);
        filtros.add(filtroEventos);

        cabecera.add(titulos, BorderLayout.NORTH);
        cabecera.add(filtros, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(listaEventos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        return tarjeta;
    }

    private JPanel crearPanelAcciones() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(245, 0));
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDE),
                        new EmptyBorder(20, 18, 20, 18)
                )
        );

        JPanel acciones = new JPanel();
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
        acciones.setOpaque(false);

        agregarTituloPanel(acciones, "ORGANIZACIÓN");

        JButton agregar = crearBoton(
                "+  Agregar evento", DORADO, AZUL
        );
        agregar.addActionListener(
                e -> mostrarFormularioNuevoEvento()
        );
        agregarBotonPanel(acciones, agregar);

        JButton calendario = crearBoton(
                "Calendario mensual", Color.WHITE, AZUL
        );
        calendario.addActionListener(
                e -> mostrarCalendarioMensual()
        );
        agregarBotonPanel(acciones, calendario);

        JButton recurso = crearBoton(
                "Registrar recurso", Color.WHITE, AZUL
        );
        recurso.addActionListener(
                e -> registrarRecurso()
        );
        agregarBotonPanel(acciones, recurso);

        acciones.add(Box.createVerticalStrut(16));
        acciones.add(new JSeparator());
        acciones.add(Box.createVerticalStrut(16));

        agregarTituloPanel(acciones, "EVENTO SELECCIONADO");

        estadoSeleccion.setForeground(GRIS);
        estadoSeleccion.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        estadoSeleccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        acciones.add(estadoSeleccion);
        acciones.add(Box.createVerticalStrut(12));

        botonDetalle = crearBoton(
                "Ver detalle", Color.WHITE, AZUL
        );
        botonEditar = crearBoton(
                "Editar evento", Color.WHITE, AZUL
        );
        botonAsistente = crearBoton(
                "Inscribir asistente", Color.WHITE, AZUL
        );
        botonTarea = crearBoton(
                "Agregar tarea", Color.WHITE, AZUL
        );
        botonAsignar = crearBoton(
                "Asignar recurso", Color.WHITE, AZUL
        );
        botonEliminar = crearBoton(
                "Eliminar evento", Color.WHITE, ROJO
        );

        botonDetalle.addActionListener(
                e -> mostrarDetalleEvento()
        );
        botonEditar.addActionListener(
                e -> editarEventoSeleccionado()
        );
        botonAsistente.addActionListener(
                e -> inscribirAsistente()
        );
        botonTarea.addActionListener(
                e -> agregarTareaEvento()
        );
        botonAsignar.addActionListener(
                e -> asignarRecursoEvento()
        );
        botonEliminar.addActionListener(
                e -> eliminarEventoSeleccionado()
        );

        agregarBotonPanel(acciones, botonDetalle);
        agregarBotonPanel(acciones, botonEditar);
        agregarBotonPanel(acciones, botonAsistente);
        agregarBotonPanel(acciones, botonTarea);
        agregarBotonPanel(acciones, botonAsignar);
        agregarBotonPanel(acciones, botonEliminar);

        panel.add(acciones, BorderLayout.NORTH);
        return panel;
    }

    private void agregarTituloPanel(JPanel panel, String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setForeground(GRIS);
        titulo.setFont(
                new Font("SansSerif", Font.BOLD, 11)
        );
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(12));
    }

    private void agregarBotonPanel(JPanel panel, JButton boton) {
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        boton.setPreferredSize(new Dimension(205, 38));
        panel.add(boton);
        panel.add(Box.createVerticalStrut(7));
    }

    private JButton crearBoton(
            String texto, Color fondo, Color colorTexto
    ) {
        JButton boton = new JButton(texto);
        boton.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        boton.setBackground(fondo);
        boton.setForeground(colorTexto);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        Color colorBorde = Color.WHITE.equals(fondo)
                ? BORDE : fondo;

        boton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(colorBorde),
                        new EmptyBorder(8, 12, 8, 12)
                )
        );
        return boton;
    }

    // el renderer cambia la presentación, no los datos del evento
    private void configurarListado() {
        listaEventos.setBackground(Color.WHITE);
        listaEventos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        listaEventos.setFixedCellHeight(98);

        // mantiene las filas dentro del ancho visible
        listaEventos.setPrototypeCellValue(
                new Evento(
                        Integer.MAX_VALUE,
                        "Evento de referencia",
                        "",
                        "Ubicación",
                        LocalDate.now(),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        1
                )
        );

        listaEventos.setCellRenderer(
                (lista, evento, indice, seleccionado, tieneFoco) -> {
                    Color fondo = seleccionado
                            ? SELECCION : Color.WHITE;

                    JPanel fila = new JPanel(new BorderLayout(14, 0));
                    fila.setBackground(fondo);
                    fila.setBorder(
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(
                                            0, seleccionado ? 3 : 0,
                                            1, 0,
                                            seleccionado ? DORADO : BORDE
                                    ),
                                    new EmptyBorder(15, 12, 15, 12)
                            )
                    );

                    JPanel fecha = new JPanel(new GridLayout(2, 1));
                    fecha.setBackground(AZUL);
                    fecha.setPreferredSize(new Dimension(54, 62));

                    JLabel dia = new JLabel(
                            String.valueOf(
                                    evento.getFecha().getDayOfMonth()
                            ),
                            SwingConstants.CENTER
                    );
                    dia.setFont(
                            new Font("SansSerif", Font.BOLD, 23)
                    );
                    dia.setForeground(Color.WHITE);

                    String mes = evento.getFecha().format(
                            DateTimeFormatter.ofPattern("MMM", IDIOMA)
                    );

                    JLabel mesTexto = new JLabel(
                            mes.toUpperCase(IDIOMA),
                            SwingConstants.CENTER
                    );
                    mesTexto.setFont(
                            new Font("SansSerif", Font.PLAIN, 10)
                    );
                    mesTexto.setForeground(Color.WHITE);

                    fecha.add(dia);
                    fecha.add(mesTexto);

                    JPanel datos = new JPanel(new GridLayout(3, 1, 0, 3));
                    datos.setOpaque(false);

                    JLabel nombre = new JLabel(evento.getNombre());
                    nombre.setFont(
                            new Font("SansSerif", Font.BOLD, 15)
                    );
                    nombre.setForeground(AZUL);

                    JLabel horario = new JLabel(
                            evento.getFecha().format(FECHA)
                                    + "  ·  "
                                    + evento.getHoraInicio().format(HORA)
                                    + " a "
                                    + evento.getHoraFin().format(HORA)
                    );
                    horario.setFont(
                            new Font("SansSerif", Font.PLAIN, 12)
                    );
                    horario.setForeground(GRIS);

                    JLabel ubicacion = new JLabel(evento.getUbicacion());
                    ubicacion.setFont(
                            new Font("SansSerif", Font.PLAIN, 12)
                    );
                    ubicacion.setForeground(GRIS);

                    datos.add(nombre);
                    datos.add(horario);
                    datos.add(ubicacion);

                    fila.add(fecha, BorderLayout.WEST);
                    fila.add(datos, BorderLayout.CENTER);
                    return fila;
                }
        );

        listaEventos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarAcciones();
            }
        });

        actualizarAcciones();
    }

    // habilita las acciones que necesitan un evento seleccionado
    private void actualizarAcciones() {
        Evento evento = listaEventos.getSelectedValue();
        boolean seleccionado = evento != null;

        botonDetalle.setEnabled(seleccionado);
        botonEditar.setEnabled(seleccionado);
        botonAsistente.setEnabled(seleccionado);
        botonTarea.setEnabled(seleccionado);
        botonAsignar.setEnabled(seleccionado);
        botonEliminar.setEnabled(seleccionado);

        estadoSeleccion.setText(
                seleccionado
                        ? "Evento #" + evento.getId()
                        : "Seleccioná un evento"
        );

        estadoSeleccion.setToolTipText(
                seleccionado ? evento.getNombre() : null
        );
    }

    private JTextArea crearAreaTexto() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(Color.WHITE);
        area.setForeground(AZUL);
        area.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );
        area.setMargin(new Insets(16, 16, 16, 16));
        return area;
    }

    // recupera los datos guardados al iniciar
    private void cargarDatos() {
        try {
            gestor.cargarDatos();
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
        Evento seleccionado = listaEventos.getSelectedValue();
        modeloEventos.clear();

        String filtro = (String) filtroEventos.getSelectedItem();
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

        if (seleccionado != null && eventos.contains(seleccionado)) {
            listaEventos.setSelectedValue(seleccionado, true);
        }

        resumenEventos.setText(
                eventos.size() + " evento(s) en la vista actual"
        );
        actualizarAcciones();
    }

    private void mostrarFormularioNuevoEvento() {
        JTextField nombre = new JTextField();
        JTextField descripcion = new JTextField();
        JTextField ubicacion = new JTextField();
        JTextField fecha = new JTextField(LocalDate.now().toString());
        JTextField inicio = new JTextField("11:00");
        JTextField fin = new JTextField("13:00");
        JTextField capacidad = new JTextField("100");

        JPanel formulario = crearFormularioEvento(
                nombre, descripcion, ubicacion,
                fecha, inicio, fin, capacidad
        );

        int resultado = JOptionPane.showConfirmDialog(
                this, formulario, "Agregar evento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Evento evento = new Evento(
                    gestor.obtenerSiguienteIdEvento(),
                    nombre.getText().trim(),
                    descripcion.getText().trim(),
                    ubicacion.getText().trim(),
                    LocalDate.parse(fecha.getText().trim()),
                    LocalTime.parse(inicio.getText().trim()),
                    LocalTime.parse(fin.getText().trim()),
                    Integer.parseInt(capacidad.getText().trim())
            );

            gestor.registrarEvento(evento);
            gestor.guardarDatos();
            actualizarListaEventos();

            JOptionPane.showMessageDialog(
                    this, "El evento fue registrado correctamente."
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

        JTextField nombre = new JTextField(evento.getNombre());
        JTextField descripcion = new JTextField(evento.getDescripcion());
        JTextField ubicacion = new JTextField(evento.getUbicacion());
        JTextField fecha = new JTextField(evento.getFecha().toString());
        JTextField inicio = new JTextField(evento.getHoraInicio().toString());
        JTextField fin = new JTextField(evento.getHoraFin().toString());
        JTextField capacidad = new JTextField(
                String.valueOf(evento.getCapacidad())
        );

        JPanel formulario = crearFormularioEvento(
                nombre, descripcion, ubicacion,
                fecha, inicio, fin, capacidad
        );

        int resultado = JOptionPane.showConfirmDialog(
                this, formulario, "Editar evento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            evento.actualizarDatos(
                    nombre.getText().trim(),
                    descripcion.getText().trim(),
                    ubicacion.getText().trim(),
                    LocalDate.parse(fecha.getText().trim()),
                    LocalTime.parse(inicio.getText().trim()),
                    LocalTime.parse(fin.getText().trim()),
                    Integer.parseInt(capacidad.getText().trim())
            );

            gestor.guardarDatos();
            actualizarListaEventos();

            JOptionPane.showMessageDialog(
                    this, "El evento fue actualizado correctamente."
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

        JTextField nombre = new JTextField();
        JTextField documento = new JTextField();
        JTextField cargo = new JTextField();
        JTextField institucion = new JTextField();
        JTextField email = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.add(new JLabel("Nombre completo:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Documento:"));
        formulario.add(documento);
        formulario.add(new JLabel("Cargo:"));
        formulario.add(cargo);
        formulario.add(new JLabel("Institución:"));
        formulario.add(institucion);
        formulario.add(new JLabel("Correo electrónico:"));
        formulario.add(email);

        int resultado = JOptionPane.showConfirmDialog(
                this, formulario,
                "Inscribir asistente en " + evento.getNombre(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Asistente asistente = new Asistente(
                    gestor.obtenerSiguienteIdAsistente(),
                    nombre.getText().trim(),
                    documento.getText().trim(),
                    cargo.getText().trim(),
                    institucion.getText().trim(),
                    email.getText().trim()
            );

            // solo guarda si se pudo realizar la inscripción
            if (!gestor.inscribirAsistente(evento.getId(), asistente)) {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo inscribir al asistente. "
                                + "El evento alcanzó su capacidad máxima.",
                        "Sin cupo disponible",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            gestor.guardarDatos();
            actualizarListaEventos();

            JOptionPane.showMessageDialog(
                    this, "El asistente fue registrado correctamente."
            );
        } catch (IOException e) {
            mostrarErrorArchivos();
        } catch (RuntimeException e) {
            mostrarErrorDatos(e);
        }
    }

    private void registrarRecurso() {
        JTextField nombre = new JTextField();
        JTextField tipo = new JTextField();
        JTextField cantidad = new JTextField("1");

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Tipo:"));
        formulario.add(tipo);
        formulario.add(new JLabel("Cantidad disponible:"));
        formulario.add(cantidad);

        int resultado = JOptionPane.showConfirmDialog(
                this, formulario, "Registrar recurso",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Recurso recurso = new Recurso(
                    gestor.obtenerSiguienteIdRecurso(),
                    nombre.getText().trim(),
                    tipo.getText().trim(),
                    Integer.parseInt(cantidad.getText().trim())
            );

            gestor.registrarRecurso(recurso);
            gestor.guardarDatos();

            JOptionPane.showMessageDialog(
                    this, "El recurso fue registrado correctamente."
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

        JComboBox<Recurso> recursos = new JComboBox<>(
                gestor.obtenerRecursos().toArray(new Recurso[0])
        );
        JTextField cantidad = new JTextField("1");

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.add(new JLabel("Recurso:"));
        formulario.add(recursos);
        formulario.add(new JLabel("Cantidad a asignar:"));
        formulario.add(cantidad);

        int resultado = JOptionPane.showConfirmDialog(
                this, formulario,
                "Asignar recurso a " + evento.getNombre(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Recurso recurso = (Recurso) recursos.getSelectedItem();

            if (recurso == null) {
                return;
            }

            boolean asignado = gestor.asignarRecurso(
                    evento.getId(),
                    recurso.getId(),
                    Integer.parseInt(cantidad.getText().trim())
            );

            if (!asignado) {
                JOptionPane.showMessageDialog(
                        this,
                        "No hay suficiente cantidad disponible "
                                + "del recurso para ese horario.",
                        "Recurso no disponible",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            gestor.guardarDatos();

            JOptionPane.showMessageDialog(
                    this, "El recurso fue asignado correctamente."
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

        JTextField hora = new JTextField(
                evento.getHoraInicio().toString()
        );
        JTextField descripcion = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.add(new JLabel("Hora (HH:MM):"));
        formulario.add(hora);
        formulario.add(new JLabel("Descripción de la tarea:"));
        formulario.add(descripcion);

        int resultado = JOptionPane.showConfirmDialog(
                this, formulario,
                "Agregar tarea a " + evento.getNombre(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            evento.agregarTarea(
                    LocalTime.parse(hora.getText().trim()),
                    descripcion.getText().trim()
            );
            gestor.guardarDatos();

            JOptionPane.showMessageDialog(
                    this, "La tarea fue agregada correctamente."
            );
        } catch (IOException e) {
            mostrarErrorArchivos();
        } catch (RuntimeException e) {
            mostrarErrorDatos(e);
        }
    }

    // abre el mes del evento seleccionado o el mes actual
    private void mostrarCalendarioMensual() {
        Evento seleccionado = listaEventos.getSelectedValue();

        fechaCalendario = seleccionado == null
                ? LocalDate.now() : seleccionado.getFecha();
        mesCalendario = YearMonth.from(fechaCalendario);

        JDialog ventana = new JDialog(
                this, "Calendario de eventos", true
        );
        ventana.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel contenido = new JPanel(new BorderLayout(12, 15));
        contenido.setBackground(FONDO);
        contenido.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel tituloMes = new JLabel("", SwingConstants.CENTER);
        tituloMes.setFont(new Font("SansSerif", Font.BOLD, 22));
        tituloMes.setForeground(AZUL);

        JButton anterior = crearBoton("<", AZUL, Color.WHITE);
        JButton siguiente = crearBoton(">", AZUL, Color.WHITE);
        JButton hoy = crearBoton("Hoy", DORADO, AZUL);

        JPanel navegacion = new JPanel(new BorderLayout(10, 0));
        navegacion.setOpaque(false);
        navegacion.add(anterior, BorderLayout.WEST);
        navegacion.add(tituloMes, BorderLayout.CENTER);
        navegacion.add(siguiente, BorderLayout.EAST);

        JPanel encabezado = new JPanel(new BorderLayout(12, 0));
        encabezado.setOpaque(false);
        encabezado.add(navegacion, BorderLayout.CENTER);
        encabezado.add(hoy, BorderLayout.EAST);

        JPanel grilla = new JPanel(new GridLayout(7, 7, 6, 6));
        grilla.setBackground(FONDO);
        grilla.setPreferredSize(new Dimension(460, 350));

        JTextArea detalleDia = crearAreaTexto();

        JScrollPane scroll = new JScrollPane(detalleDia);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.setPreferredSize(new Dimension(330, 350));

        JPanel centro = new JPanel(new BorderLayout(15, 0));
        centro.setOpaque(false);
        centro.add(grilla, BorderLayout.CENTER);
        centro.add(scroll, BorderLayout.EAST);

        JLabel leyenda = new JLabel(
                "Azul: días con eventos. Dorado: día seleccionado."
        );
        leyenda.setForeground(GRIS);
        leyenda.setFont(new Font("SansSerif", Font.PLAIN, 12));

        anterior.addActionListener(e -> {
            mesCalendario = mesCalendario.minusMonths(1);
            fechaCalendario = mesCalendario.atDay(1);
            actualizarCalendario(grilla, tituloMes, detalleDia);
        });

        siguiente.addActionListener(e -> {
            mesCalendario = mesCalendario.plusMonths(1);
            fechaCalendario = mesCalendario.atDay(1);
            actualizarCalendario(grilla, tituloMes, detalleDia);
        });

        hoy.addActionListener(e -> {
            fechaCalendario = LocalDate.now();
            mesCalendario = YearMonth.from(fechaCalendario);
            actualizarCalendario(grilla, tituloMes, detalleDia);
        });

        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(leyenda, BorderLayout.SOUTH);

        ventana.setContentPane(contenido);
        actualizarCalendario(grilla, tituloMes, detalleDia);
        ventana.setMinimumSize(new Dimension(820, 490));
        ventana.pack();
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    // arma la grilla y destaca los días con eventos
    private void actualizarCalendario(
            JPanel grilla, JLabel tituloMes, JTextArea detalleDia
    ) {
        grilla.removeAll();

        tituloMes.setText(
                mesCalendario.atDay(1).format(
                        DateTimeFormatter.ofPattern("MMMM yyyy", IDIOMA)
                )
        );

        String[] dias = {
                "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"
        };

        for (String dia : dias) {
            JLabel etiqueta = new JLabel(dia, SwingConstants.CENTER);
            etiqueta.setFont(new Font("SansSerif", Font.BOLD, 13));
            etiqueta.setForeground(AZUL);
            grilla.add(etiqueta);
        }

        int espacios = mesCalendario.atDay(1)
                .getDayOfWeek().getValue() - 1;

        for (int i = 0; i < espacios; i++) {
            grilla.add(new JLabel());
        }

        for (int numero = 1;
             numero <= mesCalendario.lengthOfMonth();
             numero++) {

            LocalDate fecha = mesCalendario.atDay(numero);
            List<Evento> eventos = gestor.obtenerEventosPorFecha(fecha);

            JButton boton = crearBoton(
                    String.valueOf(numero),
                    eventos.isEmpty() ? Color.WHITE : AZUL,
                    eventos.isEmpty() ? AZUL : Color.WHITE
            );

            if (fecha.equals(fechaCalendario)) {
                boton.setBorder(
                        BorderFactory.createLineBorder(DORADO, 3)
                );
            } else if (fecha.equals(LocalDate.now())) {
                boton.setBorder(
                        BorderFactory.createLineBorder(
                                new Color(53, 109, 140), 2
                        )
                );
            } else {
                boton.setBorder(
                        BorderFactory.createLineBorder(BORDE)
                );
            }

            boton.setToolTipText(
                    eventos.isEmpty()
                            ? "Sin eventos"
                            : eventos.size() + " evento(s)"
            );

            boton.addActionListener(e -> {
                fechaCalendario = fecha;
                actualizarCalendario(grilla, tituloMes, detalleDia);
            });

            grilla.add(boton);
        }

        // completa las celdas vacías del calendario
        while (grilla.getComponentCount() < 49) {
            grilla.add(new JLabel());
        }

        mostrarActividadesDelDia(fechaCalendario, detalleDia);
        grilla.revalidate();
        grilla.repaint();
    }

    private void mostrarActividadesDelDia(
            LocalDate fecha, JTextArea detalle
    ) {
        detalle.setText(
                "ACTIVIDADES DEL " + fecha.format(FECHA) + "\n\n"
        );

        List<Evento> eventos = gestor.obtenerEventosPorFecha(fecha);

        if (eventos.isEmpty()) {
            detalle.append(
                    "No hay eventos programados para esta fecha."
            );
        } else {
            for (Evento evento : eventos) {
                detalle.append(
                        evento.getNombre() + "\n"
                                + evento.getHoraInicio().format(HORA)
                                + " a "
                                + evento.getHoraFin().format(HORA)
                                + "\nUbicación: "
                                + evento.getUbicacion()
                                + "\n\nAGENDA\n"
                );

                if (evento.getAgenda().isEmpty()) {
                    detalle.append("Sin tareas registradas.\n");
                } else {
                    evento.getAgenda().forEach(
                            (hora, tarea) ->
                                    detalle.append(
                                            hora.format(HORA)
                                                    + " - " + tarea + "\n"
                                    )
                    );
                }

                detalle.append("\n--------------------\n\n");
            }
        }

        detalle.setCaretPosition(0);
    }

    // reúne los asistentes, la agenda y los recursos del evento
    private void mostrarDetalleEvento() {
        Evento evento = listaEventos.getSelectedValue();

        if (evento == null) {
            mostrarAvisoSeleccion();
            return;
        }

        JTextArea detalle = crearAreaTexto();
        detalle.append(
                evento.getNombre() + "\n\n"
                        + "Descripción: " + evento.getDescripcion() + "\n\n"
                        + "Ubicación: " + evento.getUbicacion() + "\n"
                        + "Fecha: " + evento.getFecha().format(FECHA) + "\n"
                        + "Horario: "
                        + evento.getHoraInicio().format(HORA)
                        + " a " + evento.getHoraFin().format(HORA) + "\n"
                        + "Capacidad: " + evento.getCapacidad() + "\n"
                        + "Inscriptos: "
                        + evento.getAsistentes().size() + "\n\n"
        );

        detalle.append("ASISTENTES\n");

        if (evento.getAsistentes().isEmpty()) {
            detalle.append("No hay asistentes registrados.\n");
        } else {
            for (Asistente asistente : evento.getAsistentes()) {
                detalle.append(
                        asistente.getNombreCompleto()
                                + " - " + asistente.getCargo()
                                + " - " + asistente.getInstitucion() + "\n"
                );
            }
        }

        detalle.append("\nAGENDA\n");

        if (evento.getAgenda().isEmpty()) {
            detalle.append("No hay tareas registradas.\n");
        } else {
            evento.getAgenda().forEach(
                    (hora, tarea) ->
                            detalle.append(
                                    hora.format(HORA)
                                            + " - " + tarea + "\n"
                            )
            );
        }

        detalle.append("\nRECURSOS\n");

        if (evento.getRecursosAsignados().isEmpty()) {
            detalle.append("No hay recursos asignados.\n");
        } else {
            evento.getRecursosAsignados().forEach(
                    (recurso, cantidad) ->
                            detalle.append(
                                    recurso.getNombre()
                                            + ": " + cantidad + "\n"
                            )
            );
        }

        detalle.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(detalle);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.setPreferredSize(new Dimension(580, 440));

        JOptionPane.showMessageDialog(
                this, scroll, "Detalle del evento",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // se usa tanto para agregar como para editar
    private JPanel crearFormularioEvento(
            JTextField nombre,
            JTextField descripcion,
            JTextField ubicacion,
            JTextField fecha,
            JTextField inicio,
            JTextField fin,
            JTextField capacidad
    ) {
        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setPreferredSize(new Dimension(480, 280));

        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Descripción:"));
        formulario.add(descripcion);
        formulario.add(new JLabel("Ubicación:"));
        formulario.add(ubicacion);
        formulario.add(new JLabel("Fecha (AAAA-MM-DD):"));
        formulario.add(fecha);
        formulario.add(new JLabel("Hora de inicio (HH:MM):"));
        formulario.add(inicio);
        formulario.add(new JLabel("Hora de finalización (HH:MM):"));
        formulario.add(fin);
        formulario.add(new JLabel("Capacidad:"));
        formulario.add(capacidad);
        return formulario;
    }

    private void eliminarEventoSeleccionado() {
        Evento evento = listaEventos.getSelectedValue();

        if (evento == null) {
            mostrarAvisoSeleccion();
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Querés eliminar el evento \""
                        + evento.getNombre() + "\"?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            gestor.eliminarEvento(evento.getId());
            gestor.guardarDatos();
            actualizarListaEventos();

            JOptionPane.showMessageDialog(
                    this, "El evento fue eliminado correctamente."
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

    private void mostrarErrorDatos(RuntimeException e) {
        JOptionPane.showMessageDialog(
                this,
                "Revisá los datos ingresados.\n" + e.getMessage(),
                "Datos incorrectos",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // muestra un aviso con los eventos próximos
    private void mostrarNotificaciones() {
        List<Evento> eventos = gestor.obtenerEventosProximos(3);

        if (eventos.isEmpty()) {
            return;
        }

        JTextArea aviso = crearAreaTexto();
        aviso.append(
                "Eventos programados para los próximos 3 días:\n\n"
        );

        for (Evento evento : eventos) {
            aviso.append(
                    "• " + evento.getNombre()
                            + "\n  " + evento.getFecha().format(FECHA)
                            + " a las "
                            + evento.getHoraInicio().format(HORA)
                            + "\n\n"
            );
        }

        aviso.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(aviso);
        scroll.setPreferredSize(new Dimension(480, 260));
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));

        JOptionPane.showMessageDialog(
                this, scroll, "Recordatorio de eventos",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazPrincipal ventana = new InterfazPrincipal();
            ventana.setVisible(true);
            ventana.mostrarNotificaciones();
        });
    }
}