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

    private static final Color FONDO = new Color(247, 243, 234);
    private static final Color AZUL = new Color(18, 62, 82);
    private static final Color DORADO = new Color(196, 154, 69);
    private static final Color BORDE = new Color(218, 222, 224);
    private static final Color GRIS = new Color(100, 110, 118);
    private static final Color ROJO = new Color(145, 55, 55);
    private static final Color SELECCION = new Color(232, 240, 243);

    private final GestorEventos gestor;
    private final DefaultListModel<Evento> modeloEventos;
    private final JList<Evento> listaEventos;
    private final JComboBox<String> filtroEventos;
    private final JLabel resumenEventos;

    private YearMonth mesCalendario;
    private LocalDate fechaCalendario;

    public InterfazPrincipal() {
        gestor = new GestorEventos();
        modeloEventos = new DefaultListModel<>();
        listaEventos = new JList<>(modeloEventos);
        resumenEventos = new JLabel();

        filtroEventos = new JComboBox<>(
                new String[]{"Todos", "Próximos", "Pasados"}
        );

        configurarEstiloGeneral();
        configurarVentana();
        crearContenido();
        configurarListado();
        cargarDatos();
        actualizarListaEventos();
    }

    // colores y fuentes de los formularios
    private void configurarEstiloGeneral() {
        Font fuente = new Font("SansSerif", Font.PLAIN, 14);

        UIManager.put("Panel.background", FONDO);
        UIManager.put("OptionPane.background", FONDO);
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
    }

    private void configurarVentana() {
        setTitle("ÁGORA Producciones - Gestión de eventos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
    }

    // pantalla principal y botones
    private void crearContenido() {
        JPanel principal = new JPanel(new BorderLayout(0, 20));
        principal.setBackground(FONDO);
        principal.setBorder(new EmptyBorder(24, 28, 20, 28));

        JPanel encabezado = new JPanel(new BorderLayout(20, 0));
        encabezado.setBackground(FONDO);
        encabezado.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 2, 0, DORADO
                        ),
                        new EmptyBorder(0, 0, 18, 0)
                )
        );

        JPanel marca = new JPanel();
        marca.setLayout(new BoxLayout(marca, BoxLayout.Y_AXIS));
        marca.setBackground(FONDO);

        JLabel nombre = new JLabel("ÁGORA");
        nombre.setFont(new Font("Serif", Font.BOLD, 34));
        nombre.setForeground(AZUL);

        JLabel productora = new JLabel("P R O D U C C I O N E S");
        productora.setFont(new Font("SansSerif", Font.PLAIN, 12));
        productora.setForeground(GRIS);

        marca.add(nombre);
        marca.add(productora);

        JLabel descripcion = new JLabel("Gestión de eventos");
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 20));
        descripcion.setForeground(AZUL);

        encabezado.add(marca, BorderLayout.WEST);
        encabezado.add(descripcion, BorderLayout.EAST);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 16));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDE),
                        new EmptyBorder(20, 20, 20, 20)
                )
        );

        JPanel cabeceraLista = new JPanel(new BorderLayout(15, 0));
        cabeceraLista.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("Eventos programados");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setForeground(AZUL);

        JPanel filtro = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        filtro.setBackground(Color.WHITE);
        filtro.add(new JLabel("Mostrar:"));

        filtroEventos.setPreferredSize(new Dimension(145, 34));
        filtro.add(filtroEventos);

        filtroEventos.addActionListener(
                e -> actualizarListaEventos()
        );

        cabeceraLista.add(titulo, BorderLayout.WEST);
        cabeceraLista.add(filtro, BorderLayout.EAST);

        listaEventos.setBackground(Color.WHITE);
        listaEventos.setForeground(AZUL);
        listaEventos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scroll = new JScrollPane(listaEventos);
        scroll.setBorder(
                BorderFactory.createLineBorder(BORDE)
        );
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel acciones = new JPanel(new GridLayout(3, 3, 10, 10));
        acciones.setBackground(Color.WHITE);

        JButton agregar = crearBoton(
                "Agregar evento", DORADO, AZUL
        );
        JButton editar = crearBoton(
                "Editar evento", Color.WHITE, AZUL
        );
        JButton detalle = crearBoton(
                "Ver detalle", Color.WHITE, AZUL
        );
        JButton asistente = crearBoton(
                "Inscribir asistente", Color.WHITE, AZUL
        );
        JButton tarea = crearBoton(
                "Agregar tarea", Color.WHITE, AZUL
        );
        JButton recurso = crearBoton(
                "Registrar recurso", Color.WHITE, AZUL
        );
        JButton asignar = crearBoton(
                "Asignar recurso", Color.WHITE, AZUL
        );
        JButton calendario = crearBoton(
                "Calendario mensual", Color.WHITE, AZUL
        );
        JButton eliminar = crearBoton(
                "Eliminar evento", Color.WHITE, ROJO
        );

        agregar.addActionListener(
                e -> mostrarFormularioNuevoEvento()
        );
        editar.addActionListener(
                e -> editarEventoSeleccionado()
        );
        detalle.addActionListener(
                e -> mostrarDetalleEvento()
        );
        asistente.addActionListener(
                e -> inscribirAsistente()
        );
        tarea.addActionListener(
                e -> agregarTareaEvento()
        );
        recurso.addActionListener(
                e -> registrarRecurso()
        );
        asignar.addActionListener(
                e -> asignarRecursoEvento()
        );
        calendario.addActionListener(
                e -> mostrarCalendarioMensual()
        );
        eliminar.addActionListener(
                e -> eliminarEventoSeleccionado()
        );

        acciones.add(agregar);
        acciones.add(editar);
        acciones.add(detalle);
        acciones.add(asistente);
        acciones.add(tarea);
        acciones.add(recurso);
        acciones.add(asignar);
        acciones.add(calendario);
        acciones.add(eliminar);

        tarjeta.add(cabeceraLista, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        tarjeta.add(acciones, BorderLayout.SOUTH);

        resumenEventos.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        resumenEventos.setForeground(GRIS);

        principal.add(encabezado, BorderLayout.NORTH);
        principal.add(tarjeta, BorderLayout.CENTER);
        principal.add(resumenEventos, BorderLayout.SOUTH);

        setContentPane(principal);
    }

    // mismo estilo para los botones de toda la aplicación
    private JButton crearBoton(
            String texto,
            Color fondo,
            Color colorTexto
    ) {
        JButton boton = new JButton(texto);
        boton.setFont(
                new Font("SansSerif", Font.BOLD, 13)
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
                ? BORDE
                : fondo;

        boton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(colorBorde),
                        new EmptyBorder(11, 14, 11, 14)
                )
        );

        return boton;
    }

    // separa el nombre, la fecha y la ubicación de cada evento
    private void configurarListado() {
        listaEventos.setFixedCellHeight(90);
        listaEventos.setBorder(new EmptyBorder(0, 0, 0, 0));

        listaEventos.setCellRenderer(
                (lista, evento, indice, seleccionado, tieneFoco) -> {
                    Color fondoFila = seleccionado
                            ? SELECCION
                            : Color.WHITE;

                    JPanel fila = new JPanel(
                            new BorderLayout(18, 0)
                    );
                    fila.setBackground(fondoFila);
                    fila.setBorder(
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(
                                            0, 0, 1, 0, BORDE
                                    ),
                                    new EmptyBorder(14, 16, 14, 16)
                            )
                    );

                    JPanel fecha = new JPanel(
                            new GridLayout(2, 1)
                    );
                    fecha.setBackground(AZUL);
                    fecha.setPreferredSize(new Dimension(58, 58));

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

                    Locale idioma = Locale.forLanguageTag("es-AR");

                    String mes = evento.getFecha().format(
                            DateTimeFormatter.ofPattern("MMM", idioma)
                    );

                    JLabel mesTexto = new JLabel(
                            mes.toUpperCase(idioma),
                            SwingConstants.CENTER
                    );
                    mesTexto.setFont(
                            new Font("SansSerif", Font.PLAIN, 11)
                    );
                    mesTexto.setForeground(Color.WHITE);

                    fecha.add(dia);
                    fecha.add(mesTexto);

                    JPanel informacion = new JPanel(
                            new GridLayout(2, 1, 0, 5)
                    );
                    informacion.setBackground(fondoFila);

                    JLabel nombre = new JLabel(evento.getNombre());
                    nombre.setFont(
                            new Font("SansSerif", Font.BOLD, 16)
                    );
                    nombre.setForeground(AZUL);

                    JLabel horario = new JLabel(
                            evento.getFecha().format(
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy"
                                    )
                            )
                                    + "  ·  "
                                    + evento.getHoraInicio()
                                    + " a "
                                    + evento.getHoraFin()
                                    + "  ·  "
                                    + evento.getUbicacion()
                    );
                    horario.setFont(
                            new Font("SansSerif", Font.PLAIN, 12)
                    );
                    horario.setForeground(GRIS);

                    informacion.add(nombre);
                    informacion.add(horario);

                    fila.add(fecha, BorderLayout.WEST);
                    fila.add(informacion, BorderLayout.CENTER);

                    return fila;
                }
        );
    }

    // se reutiliza en detalles, calendario y avisos
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
        area.setMargin(new Insets(15, 15, 15, 15));
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

    // actualiza el listado según el filtro
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

        resumenEventos.setText(
                eventos.size() + " evento(s) en la vista actual"
        );
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

        JTextField nombre =
                new JTextField(evento.getNombre());
        JTextField descripcion =
                new JTextField(evento.getDescripcion());
        JTextField ubicacion =
                new JTextField(evento.getUbicacion());
        JTextField fecha =
                new JTextField(evento.getFecha().toString());
        JTextField inicio =
                new JTextField(evento.getHoraInicio().toString());
        JTextField fin =
                new JTextField(evento.getHoraFin().toString());
        JTextField capacidad =
                new JTextField(String.valueOf(evento.getCapacidad()));

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

        JTextField nombre = new JTextField();
        JTextField documento = new JTextField();
        JTextField cargo = new JTextField();
        JTextField institucion = new JTextField();
        JTextField email = new JTextField();

        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );
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

            boolean registrado = gestor.inscribirAsistente(
                    evento.getId(), asistente
            );

            // solo guarda si se pudo realizar la inscripción
            if (!registrado) {
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
        JTextField nombre = new JTextField();
        JTextField tipo = new JTextField();
        JTextField cantidad = new JTextField("1");

        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );
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

        JComboBox<Recurso> recursos = new JComboBox<>(
                gestor.obtenerRecursos().toArray(new Recurso[0])
        );
        JTextField cantidad = new JTextField("1");

        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );
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
            Recurso recurso =
                    (Recurso) recursos.getSelectedItem();

            if (recurso == null) {
                return;
            }

            boolean asignado = gestor.asignarRecurso(
                    evento.getId(),
                    recurso.getId(),
                    Integer.parseInt(cantidad.getText().trim())
            );

            // el gestor verifica la disponibilidad antes de asignar
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

        JTextField hora = new JTextField(
                evento.getHoraInicio().toString()
        );
        JTextField descripcion = new JTextField();

        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );
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
                    this,
                    "La tarea fue agregada correctamente."
            );
        } catch (IOException e) {
            mostrarErrorArchivos();
        } catch (RuntimeException e) {
            mostrarErrorDatos(e);
        }
    }

    // abre el calendario en la fecha del evento seleccionado
    private void mostrarCalendarioMensual() {
        Evento seleccionado = listaEventos.getSelectedValue();

        fechaCalendario = seleccionado == null
                ? LocalDate.now()
                : seleccionado.getFecha();

        mesCalendario = YearMonth.from(fechaCalendario);

        JDialog ventana = new JDialog(
                this, "Calendario de eventos", true
        );
        ventana.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE
        );

        JPanel contenido = new JPanel(
                new BorderLayout(12, 15)
        );
        contenido.setBackground(FONDO);
        contenido.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        JLabel tituloMes = new JLabel(
                "", SwingConstants.CENTER
        );
        tituloMes.setFont(
                new Font("SansSerif", Font.BOLD, 22)
        );
        tituloMes.setForeground(AZUL);

        JButton anterior = crearBoton(
                "<", AZUL, Color.WHITE
        );
        JButton siguiente = crearBoton(
                ">", AZUL, Color.WHITE
        );
        JButton hoy = crearBoton(
                "Hoy", DORADO, AZUL
        );

        JPanel navegacion = new JPanel(
                new BorderLayout(10, 0)
        );
        navegacion.setBackground(FONDO);
        navegacion.add(anterior, BorderLayout.WEST);
        navegacion.add(tituloMes, BorderLayout.CENTER);
        navegacion.add(siguiente, BorderLayout.EAST);

        JPanel encabezado = new JPanel(
                new BorderLayout(12, 0)
        );
        encabezado.setBackground(FONDO);
        encabezado.add(navegacion, BorderLayout.CENTER);
        encabezado.add(hoy, BorderLayout.EAST);

        JPanel grilla = new JPanel(
                new GridLayout(7, 7, 6, 6)
        );
        grilla.setBackground(FONDO);
        grilla.setPreferredSize(new Dimension(460, 350));

        JTextArea detalleDia = crearAreaTexto();

        JScrollPane scroll = new JScrollPane(detalleDia);
        scroll.setBorder(
                BorderFactory.createLineBorder(BORDE)
        );
        scroll.setPreferredSize(new Dimension(330, 350));

        JPanel centro = new JPanel(
                new BorderLayout(15, 0)
        );
        centro.setBackground(FONDO);
        centro.add(grilla, BorderLayout.CENTER);
        centro.add(scroll, BorderLayout.EAST);

        JLabel leyenda = new JLabel(
                "Azul: días con eventos. "
                        + "Borde dorado: día seleccionado."
        );
        leyenda.setForeground(GRIS);
        leyenda.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

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
            JPanel grilla,
            JLabel tituloMes,
            JTextArea detalleDia
    ) {
        grilla.removeAll();

        DateTimeFormatter formatoMes =
                DateTimeFormatter.ofPattern(
                        "MMMM yyyy",
                        Locale.forLanguageTag("es-AR")
                );

        tituloMes.setText(
                mesCalendario.atDay(1).format(formatoMes)
        );

        String[] dias = {
                "Lun", "Mar", "Mié", "Jue",
                "Vie", "Sáb", "Dom"
        };

        for (String dia : dias) {
            JLabel etiqueta = new JLabel(
                    dia, SwingConstants.CENTER
            );
            etiqueta.setFont(
                    new Font("SansSerif", Font.BOLD, 13)
            );
            etiqueta.setForeground(AZUL);
            grilla.add(etiqueta);
        }

        // ubica el primer día en la columna que corresponde
        int espacios = mesCalendario.atDay(1)
                .getDayOfWeek().getValue() - 1;

        for (int i = 0; i < espacios; i++) {
            grilla.add(new JLabel());
        }

        for (int numero = 1;
             numero <= mesCalendario.lengthOfMonth();
             numero++) {

            LocalDate fecha = mesCalendario.atDay(numero);

            List<Evento> eventos =
                    gestor.obtenerEventosPorFecha(fecha);

            JButton boton = crearBoton(
                    String.valueOf(numero),
                    eventos.isEmpty() ? Color.WHITE : AZUL,
                    eventos.isEmpty() ? AZUL : Color.WHITE
            );

            boton.setMargin(new Insets(0, 0, 0, 0));

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
                actualizarCalendario(
                        grilla, tituloMes, detalleDia
                );
            });

            grilla.add(boton);
        }

        // completa las celdas vacías del calendario
        while (grilla.getComponentCount() < 49) {
            grilla.add(new JLabel());
        }

        mostrarActividadesDelDia(
                fechaCalendario, detalleDia
        );

        grilla.revalidate();
        grilla.repaint();
    }

    // muestra los eventos y las tareas del día elegido
    private void mostrarActividadesDelDia(
            LocalDate fecha,
            JTextArea detalle
    ) {
        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        detalle.setText(
                "ACTIVIDADES DEL "
                        + fecha.format(formato) + "\n\n"
        );

        List<Evento> eventos =
                gestor.obtenerEventosPorFecha(fecha);

        if (eventos.isEmpty()) {
            detalle.append(
                    "No hay eventos programados para esta fecha."
            );
        } else {
            for (Evento evento : eventos) {
                detalle.append(
                        evento.getNombre() + "\n"
                                + evento.getHoraInicio()
                                + " a " + evento.getHoraFin()
                                + "\nUbicación: "
                                + evento.getUbicacion()
                                + "\n\nAGENDA\n"
                );

                if (evento.getAgenda().isEmpty()) {
                    detalle.append(
                            "Sin tareas registradas.\n"
                    );
                } else {
                    evento.getAgenda().forEach(
                            (hora, tarea) ->
                                    detalle.append(
                                            hora + " - "
                                                    + tarea + "\n"
                                    )
                    );
                }

                detalle.append(
                        "\n--------------------\n\n"
                );
            }
        }

        detalle.setCaretPosition(0);
    }

    // reúne la información del evento en una sola vista
    private void mostrarDetalleEvento() {
        Evento evento = listaEventos.getSelectedValue();

        if (evento == null) {
            mostrarAvisoSeleccion();
            return;
        }

        JTextArea detalle = crearAreaTexto();

        detalle.append(
                "Nombre: " + evento.getNombre() + "\n\n"
                        + "Descripción: "
                        + evento.getDescripcion() + "\n\n"
                        + "Ubicación: "
                        + evento.getUbicacion() + "\n"
                        + "Fecha: " + evento.getFecha() + "\n"
                        + "Horario: "
                        + evento.getHoraInicio() + " a "
                        + evento.getHoraFin() + "\n"
                        + "Capacidad: "
                        + evento.getCapacidad() + "\n"
                        + "Inscriptos: "
                        + evento.getAsistentes().size() + "\n\n"
        );

        detalle.append("ASISTENTES\n");

        if (evento.getAsistentes().isEmpty()) {
            detalle.append(
                    "No hay asistentes registrados.\n"
            );
        } else {
            for (Asistente asistente : evento.getAsistentes()) {
                detalle.append(
                        asistente.getNombreCompleto()
                                + " - " + asistente.getCargo()
                                + " - " + asistente.getInstitucion()
                                + "\n"
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
                                    hora + " - " + tarea + "\n"
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
        scroll.setBorder(
                BorderFactory.createLineBorder(BORDE)
        );
        scroll.setPreferredSize(
                new Dimension(580, 440)
        );

        JOptionPane.showMessageDialog(
                this, scroll, "Detalle del evento",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // reutiliza el formulario para agregar y editar
    private JPanel crearFormularioEvento(
            JTextField nombre,
            JTextField descripcion,
            JTextField ubicacion,
            JTextField fecha,
            JTextField inicio,
            JTextField fin,
            JTextField capacidad
    ) {
        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );
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
        formulario.add(
                new JLabel("Hora de finalización (HH:MM):")
        );
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

    private void mostrarErrorDatos(RuntimeException e) {
        JOptionPane.showMessageDialog(
                this,
                "Revisá los datos ingresados.\n"
                        + e.getMessage(),
                "Datos incorrectos",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // avisa si hay eventos dentro de los próximos tres días
    private void mostrarNotificaciones() {
        List<Evento> eventos =
                gestor.obtenerEventosProximos(3);

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
                            + "\n  " + evento.getFecha()
                            + " a las "
                            + evento.getHoraInicio()
                            + "\n\n"
            );
        }

        aviso.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(aviso);
        scroll.setPreferredSize(
                new Dimension(480, 260)
        );
        scroll.setBorder(
                BorderFactory.createLineBorder(BORDE)
        );

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Recordatorio de eventos",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazPrincipal ventana =
                    new InterfazPrincipal();

            ventana.setVisible(true);
            ventana.mostrarNotificaciones();
        });
    }
}