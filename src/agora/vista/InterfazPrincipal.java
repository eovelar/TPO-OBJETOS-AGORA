package agora.vista;

import agora.modelo.Asistente;
import agora.modelo.Evento;
import agora.modelo.Recurso;
import agora.servicio.GestorEventos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InterfazPrincipal extends JFrame {

    private static final Color FONDO = new Color(244, 246, 248);
    private static final Color AZUL = new Color(18, 62, 82);
    private static final Color DORADO = new Color(196, 154, 69);
    private static final Color BORDE = new Color(221, 227, 232);
    private static final Color GRIS = new Color(104, 117, 128);
    private static final Color ROJO = new Color(155, 63, 63);
    private static final Color SELECCION = new Color(226, 238, 245);

    private static final Locale IDIOMA =
            Locale.forLanguageTag("es-AR");

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] COLUMNAS_EVENTOS = {
            "Evento", "Fecha", "Inicio", "Fin",
            "Ubicación", "Inscriptos / cupo"
    };

    private final GestorEventos gestor;

    private final DefaultTableModel modeloEventos;
    private final DefaultTableModel modeloAsistentes;
    private final DefaultTableModel modeloRecursos;
    private final DefaultTableModel modeloAgenda;

    private final JTable tablaEventos;
    private JTable tablaAsistentes;
    private JTable tablaRecursos;
    private JTable tablaAgenda;

    private final JComboBox<String> filtroEventos;
    private final JTextField campoBusqueda;
    private final JLabel resumen;
    private final JLabel estado;
    private final JLabel tituloDetalle;
    private final JTextArea detalle;
    private final JTabbedPane pestanas;

    private final boolean datosCargados;

    private List<Evento> eventosVisibles;
    private List<Recurso> recursosDelDetalle = List.of();
    private List<LocalTime> horasDelDetalle = List.of();

    private JButton botonEditar;
    private JButton botonAsistente;
    private JButton botonAsignar;
    private JButton botonTarea;
    private JButton botonEliminar;
    private JButton botonEditarAsistente;
    private JButton botonCancelarInscripcion;
    private JButton botonCambiarCantidad;
    private JButton botonQuitarRecurso;
    private JButton botonEditarTarea;
    private JButton botonEliminarTarea;

    private int idEventoDetalle = -1;

    private YearMonth mesCalendario;
    private LocalDate fechaCalendario;

    private boolean actualizando;
    private boolean cambiosSinGuardar;
    private JButton botonReintentar;
    private JLabel avisoConflictos;

    public InterfazPrincipal() {
        gestor = new GestorEventos();
        eventosVisibles = List.of();

        modeloEventos = new DefaultTableModel(
                COLUMNAS_EVENTOS, 0
        );

        modeloAsistentes = new DefaultTableModel(
                new String[]{
                        "Nombre", "Documento", "Cargo",
                        "Institución", "Correo"
                }, 0
        );

        modeloRecursos = new DefaultTableModel(
                new String[]{"Recurso", "Tipo", "Cantidad"}, 0
        );

        modeloAgenda = new DefaultTableModel(
                new String[]{"Hora", "Tarea"}, 0
        );

        tablaEventos = new JTable(modeloEventos);

        filtroEventos = new JComboBox<>(
                new String[]{"Todos", "Próximos", "Pasados"}
        );

        campoBusqueda = new JTextField(18);
        resumen = new JLabel();
        estado = new JLabel("Listo para trabajar");
        tituloDetalle = new JLabel("Información del evento");
        detalle = new JTextArea();
        pestanas = new JTabbedPane();

        configurarEstilo();
        configurarVentana();
        crearContenido();
        configurarTablaEventos();

        datosCargados = cargarDatos();

        if (datosCargados) {
            actualizarEventos();
        }
    }

    // estilo compartido por los formularios y mensajes
    private void configurarEstilo() {
        Font fuente = new Font("SansSerif", Font.PLAIN, 13);

        UIManager.put("Panel.background", Color.WHITE);
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
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", AZUL);
        UIManager.put("OptionPane.messageFont", fuente);
        UIManager.put("TabbedPane.font", fuente);
        UIManager.put("TabbedPane.selected", Color.WHITE);
        UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
    }

    private void configurarVentana() {
        setTitle("ÁGORA Producciones - Gestión de eventos");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarPrograma();
            }
        });
        setSize(1220, 750);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
    }

    private void crearContenido() {
        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(FONDO);
        principal.add(crearEncabezado(), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setBackground(FONDO);
        cuerpo.setBorder(new EmptyBorder(14, 20, 10, 20));
        JPanel superior = new JPanel(new BorderLayout(0, 8));
        superior.setOpaque(false);
        superior.add(crearBarraSuperior(), BorderLayout.NORTH);
        avisoConflictos = new JLabel();
        avisoConflictos.setForeground(ROJO);
        avisoConflictos.setFont(new Font("SansSerif", Font.BOLD, 12));
        avisoConflictos.setVisible(false);
        superior.add(avisoConflictos, BorderLayout.SOUTH);
        cuerpo.add(superior, BorderLayout.NORTH);

        JSplitPane division = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                crearPanelEventos(),
                crearPanelDetalle()
        );

        division.setBorder(BorderFactory.createEmptyBorder());
        division.setDividerSize(7);
        division.setContinuousLayout(true);
        division.setResizeWeight(0.50);
        division.setBackground(FONDO);

        cuerpo.add(division, BorderLayout.CENTER);
        principal.add(cuerpo, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(FONDO);
        pie.setBorder(new EmptyBorder(0, 22, 11, 22));

        resumen.setForeground(GRIS);
        resumen.setFont(new Font("SansSerif", Font.PLAIN, 12));

        estado.setForeground(GRIS);
        estado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        estado.setBorder(new EmptyBorder(0, 18, 0, 0));

        pie.add(resumen, BorderLayout.WEST);
        pie.add(estado, BorderLayout.CENTER);
        botonReintentar = crearBoton("Reintentar guardado", Color.WHITE, ROJO);
        botonReintentar.setVisible(false);
        botonReintentar.addActionListener(e -> {
            if (guardarCambios()) {
                mostrarEstado("Todos los cambios se guardaron correctamente.");
            }
        });
        pie.add(botonReintentar, BorderLayout.EAST);
        pie.setToolTipText(
                "Seleccioná un evento para ver sus asistentes, recursos y agenda"
        );

        principal.add(pie, BorderLayout.SOUTH);
        setContentPane(principal);
        actualizarAcciones();
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout(20, 0));
        encabezado.setBackground(AZUL);
        encabezado.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 2, 0, DORADO
                        ),
                        new EmptyBorder(10, 23, 10, 23)
                )
        );

        JLabel marca = new JLabel("ÁGORA");
        marca.setFont(new Font("Serif", Font.BOLD, 26));
        marca.setForeground(Color.WHITE);

        JLabel sistema = new JLabel(
                "PRODUCCIONES  ·  GESTIÓN DE EVENTOS"
        );
        sistema.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sistema.setForeground(new Color(216, 227, 234));

        encabezado.add(marca, BorderLayout.WEST);
        encabezado.add(sistema, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout(15, 0));
        barra.setOpaque(false);

        JLabel titulo = new JLabel("Administración de eventos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 21));
        titulo.setForeground(AZUL);

        JPanel herramientas = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 7, 0)
        );
        herramientas.setOpaque(false);

        JButton calendario = crearBoton(
                "Calendario", Color.WHITE, AZUL
        );
        calendario.addActionListener(e -> mostrarCalendarioMensual());

        JButton recursos = crearBoton(
                "Inventario de recursos", Color.WHITE, AZUL
        );
        recursos.addActionListener(e -> mostrarInventario());

        JButton nuevo = crearBoton(
                "+ Nuevo evento", DORADO, AZUL
        );
        nuevo.addActionListener(e -> mostrarFormularioEvento(null));

        herramientas.add(calendario);
        herramientas.add(recursos);
        herramientas.add(nuevo);

        barra.add(titulo, BorderLayout.CENTER);
        barra.add(herramientas, BorderLayout.EAST);
        return barra;
    }

    private JPanel crearPanelEventos() {
        JPanel panel = crearTarjeta();
        panel.setPreferredSize(new Dimension(900, 280));
        panel.setMinimumSize(new Dimension(600, 190));

        JPanel filtros = new JPanel(new BorderLayout(12, 0));
        filtros.setOpaque(false);

        JPanel filtro = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 6, 0)
        );
        filtro.setOpaque(false);
        filtro.add(new JLabel("Mostrar:"));

        filtroEventos.setPreferredSize(new Dimension(130, 29));
        filtroEventos.addActionListener(e -> actualizarEventos());
        filtro.add(filtroEventos);

        JPanel busqueda = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 6, 0)
        );
        busqueda.setOpaque(false);

        campoBusqueda.setPreferredSize(new Dimension(210, 29));
        campoBusqueda.setToolTipText(
                "Buscá por nombre del evento o ubicación"
        );

        JButton buscar = crearBoton("Buscar", Color.WHITE, AZUL);
        JButton limpiar = crearBoton("Limpiar", Color.WHITE, AZUL);

        buscar.addActionListener(e -> actualizarEventos());
        campoBusqueda.addActionListener(e -> actualizarEventos());

        // filtra también al escribir, pegar o borrar texto
        campoBusqueda.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        actualizarEventos();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        actualizarEventos();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        actualizarEventos();
                    }
                }
        );

        limpiar.addActionListener(e -> {
            campoBusqueda.setText("");
            campoBusqueda.requestFocusInWindow();
        });

        busqueda.add(new JLabel("Nombre o ubicación:"));
        busqueda.add(campoBusqueda);
        busqueda.add(buscar);
        busqueda.add(limpiar);

        filtros.add(filtro, BorderLayout.WEST);
        filtros.add(busqueda, BorderLayout.EAST);

        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 6, 0)
        );
        acciones.setOpaque(false);

        botonEditar = crearBoton("Editar", Color.WHITE, AZUL);
        botonAsistente = crearBoton(
                "Inscribir asistente", Color.WHITE, AZUL
        );
        botonAsignar = crearBoton(
                "Asignar recurso", Color.WHITE, AZUL
        );
        botonTarea = crearBoton(
                "Agregar tarea", Color.WHITE, AZUL
        );
        botonEliminar = crearBoton(
                "Eliminar", Color.WHITE, ROJO
        );

        botonEditar.addActionListener(e -> {
            Evento evento = obtenerEventoSeleccionado();
            if (evento != null) {
                mostrarFormularioEvento(evento);
            }
        });

        botonAsistente.addActionListener(e -> inscribirAsistente());
        botonAsignar.addActionListener(e -> asignarRecurso());
        botonTarea.addActionListener(e -> agregarTarea());
        botonEliminar.addActionListener(e -> eliminarEvento());

        // las demás acciones están dentro de sus pestañas
        acciones.add(botonEditar);
        acciones.add(botonEliminar);

        panel.add(filtros, BorderLayout.NORTH);
        panel.add(crearScroll(tablaEventos), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelDetalle() {
        JPanel panel = crearTarjeta();
        panel.setPreferredSize(new Dimension(900, 280));
        panel.setMinimumSize(new Dimension(500, 180));

        tituloDetalle.setFont(new Font("SansSerif", Font.BOLD, 14));
        tituloDetalle.setForeground(AZUL);
        configurarAreaTexto(detalle);

        tablaAsistentes = crearTabla(modeloAsistentes);
        tablaAsistentes.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        actualizarAccionesAsistentes();
                    }
                });

        botonEditarAsistente = crearBoton(
                "Editar datos", Color.WHITE, AZUL
        );
        botonCancelarInscripcion = crearBoton(
                "Cancelar inscripción", Color.WHITE, ROJO
        );

        botonEditarAsistente.addActionListener(e -> editarAsistente());
        botonCancelarInscripcion.addActionListener(
                e -> cancelarInscripcion()
        );

        tablaRecursos = crearTabla(modeloRecursos);
        tablaRecursos.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        actualizarAccionesRecursos();
                    }
                });

        botonCambiarCantidad = crearBoton(
                "Cambiar cantidad", Color.WHITE, AZUL
        );
        botonQuitarRecurso = crearBoton(
                "Quitar asignación", Color.WHITE, ROJO
        );

        botonCambiarCantidad.addActionListener(
                e -> cambiarCantidadRecurso()
        );
        botonQuitarRecurso.addActionListener(
                e -> quitarRecursoAsignado()
        );

        tablaAgenda = crearTabla(modeloAgenda);
        tablaAgenda.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        actualizarAccionesAgenda();
                    }
                });

        botonEditarTarea = crearBoton(
                "Editar tarea", Color.WHITE, AZUL
        );
        botonEliminarTarea = crearBoton(
                "Eliminar tarea", Color.WHITE, ROJO
        );

        botonEditarTarea.addActionListener(e -> editarTarea());
        botonEliminarTarea.addActionListener(e -> eliminarTarea());

        tablaAsistentes.getColumnModel().getColumn(0)
                .setPreferredWidth(170);
        tablaAsistentes.getColumnModel().getColumn(4)
                .setPreferredWidth(200);

        tablaAgenda.getColumnModel().getColumn(0)
                .setPreferredWidth(70);
        tablaAgenda.getColumnModel().getColumn(1)
                .setPreferredWidth(800);

        pestanas.setForeground(AZUL);
        pestanas.setBackground(FONDO);
        pestanas.addTab("Detalle", crearScroll(detalle));

        pestanas.addTab(
                "Asistentes (0)",
                crearSeccion(
                        tablaAsistentes,
                        "Este evento todavía no tiene asistentes.",
                        botonAsistente,
                        botonEditarAsistente,
                        botonCancelarInscripcion
                )
        );

        pestanas.addTab(
                "Recursos (0)",
                crearSeccion(
                        tablaRecursos,
                        "Este evento todavía no tiene recursos asignados.",
                        botonAsignar,
                        botonCambiarCantidad,
                        botonQuitarRecurso
                )
        );

        pestanas.addTab(
                "Agenda (0)",
                crearSeccion(
                        tablaAgenda,
                        "Este evento todavía no tiene tareas registradas.",
                        botonTarea,
                        botonEditarTarea,
                        botonEliminarTarea
                )
        );

        panel.add(tituloDetalle, BorderLayout.NORTH);
        panel.add(pestanas, BorderLayout.CENTER);
        return panel;
    }

    // alterna entre la tabla y el mensaje cuando no hay datos
    private JPanel crearPanelConsulta(JTable tabla, String mensaje) {
        JPanel panel = new JPanel(new CardLayout());
        panel.setBackground(Color.WHITE);

        JLabel vacio = new JLabel(mensaje, SwingConstants.CENTER);
        vacio.setForeground(GRIS);
        vacio.setFont(new Font("SansSerif", Font.PLAIN, 13));

        panel.add(crearScroll(tabla), "tabla");
        panel.add(vacio, "vacio");
        return panel;
    }

    private void mostrarPanelConsulta(int indice, boolean tieneDatos) {
        JPanel seccion = (JPanel) pestanas.getComponentAt(indice);
        JPanel panel = (JPanel) seccion.getClientProperty("consulta");
        CardLayout tarjetas = (CardLayout) panel.getLayout();

        tarjetas.show(panel, tieneDatos ? "tabla" : "vacio");
    }

    // cada pestaña reúne su información y sus acciones
    private JPanel crearSeccion(
            JTable tabla, String mensaje, JButton... botones
    ) {
        JPanel seccion = new JPanel(new BorderLayout(0, 8));
        seccion.setBackground(Color.WHITE);

        JPanel consulta = crearPanelConsulta(tabla, mensaje);

        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 6, 4)
        );
        acciones.setOpaque(false);

        for (JButton boton : botones) {
            acciones.add(boton);
        }

        seccion.putClientProperty("consulta", consulta);
        seccion.add(consulta, BorderLayout.CENTER);
        seccion.add(acciones, BorderLayout.SOUTH);
        return seccion;
    }

    private JPanel crearTarjeta() {
        JPanel panel = new JPanel(new BorderLayout(0, 9));
        panel.setBackground(Color.WHITE);
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDE),
                        new EmptyBorder(11, 13, 11, 13)
                )
        );
        return panel;
    }

    private JButton crearBoton(
            String texto, Color fondo, Color colorTexto
    ) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBackground(fondo);
        boton.setForeground(colorTexto);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        Color borde = fondo.equals(Color.WHITE) ? BORDE : fondo;
        boton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borde),
                        new EmptyBorder(7, 10, 7, 10)
                )
        );
        return boton;
    }

    private JScrollPane crearScroll(Component componente) {
        JScrollPane scroll = new JScrollPane(componente);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        aplicarEstiloTabla(tabla);
        return tabla;
    }

    // las tablas son de consulta y los cambios se hacen con formularios
    private void aplicarEstiloTabla(JTable tabla) {
        tabla.setDefaultEditor(Object.class, null);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabla.setForeground(AZUL);
        tabla.setBackground(Color.WHITE);
        tabla.setSelectionBackground(SELECCION);
        tabla.setSelectionForeground(AZUL);
        tabla.setRowHeight(30);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(BORDE);
        tabla.setIntercellSpacing(new Dimension(10, 1));
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabla.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        tabla.getTableHeader().setForeground(AZUL);
        tabla.getTableHeader().setBackground(
                new Color(237, 241, 244)
        );
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private void configurarTablaEventos() {
        aplicarEstiloTabla(tablaEventos);
        tablaEventos.setRowHeight(34);
        ajustarColumnasEventos();

        tablaEventos.setRowSorter(crearOrdenadorEventos());

        tablaEventos.getSelectionModel().addListSelectionListener(e -> {
            if (!actualizando && !e.getValueIsAdjusting()) {
                actualizarDetalle();
            }
        });
    }

    private TableRowSorter<DefaultTableModel> crearOrdenadorEventos() {
        TableRowSorter<DefaultTableModel> ordenador =
                new TableRowSorter<>(modeloEventos);

        // ordena fechas y horarios por su valor, no por su texto
        ordenador.setComparator(
                1, (a, b) -> ((LocalDate) a).compareTo((LocalDate) b)
        );
        ordenador.setComparator(
                2, (a, b) -> ((LocalTime) a).compareTo((LocalTime) b)
        );
        ordenador.setComparator(
                3, (a, b) -> ((LocalTime) a).compareTo((LocalTime) b)
        );
        return ordenador;
    }

    private void ajustarColumnasEventos() {
        int[] anchos = {260, 100, 70, 70, 290, 130};

        for (int i = 0; i < anchos.length; i++) {
            tablaEventos.getColumnModel().getColumn(i)
                    .setPreferredWidth(anchos[i]);
        }
    }

    private void configurarAreaTexto(JTextArea area) {
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        area.setForeground(AZUL);
        area.setBackground(Color.WHITE);
        area.setMargin(new Insets(10, 12, 10, 12));
    }

    // si falla la carga, no permite guardar sobre datos incompletos
    private boolean cargarDatos() {
        try {
            gestor.cargarDatos();
            return true;
        } catch (IOException | RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudieron cargar los datos guardados.\n"
                            + e.getMessage()
                            + "\n\nRevisá los archivos de la carpeta datos "
                            + "antes de volver a iniciar.",
                    "Error al cargar",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    // combina el filtro de fechas con la búsqueda
    private void actualizarEventos() {
        Evento seleccionado = obtenerEventoSeleccionado();

        int idSeleccionado = seleccionado == null
                ? -1 : seleccionado.getId();

        List<RowSorter.SortKey> orden = new ArrayList<>();
        if (tablaEventos.getRowSorter() != null) {
            orden.addAll(tablaEventos.getRowSorter().getSortKeys());
        }

        String filtro = (String) filtroEventos.getSelectedItem();
        List<Evento> base;

        if ("Próximos".equals(filtro)) {
            base = gestor.obtenerEventosFuturos();
        } else if ("Pasados".equals(filtro)) {
            base = gestor.obtenerEventosPasados();
        } else {
            base = gestor.obtenerEventosOrdenados();
        }

        String busqueda = normalizar(campoBusqueda.getText());

        List<Evento> nuevaLista = base.stream()
                .filter(evento ->
                        normalizar(evento.getNombre()).contains(busqueda)
                                || normalizar(evento.getUbicacion())
                                .contains(busqueda)
                )
                .toList();

        Object[][] filas = new Object[nuevaLista.size()][6];

        for (int i = 0; i < nuevaLista.size(); i++) {
            Evento evento = nuevaLista.get(i);

            filas[i] = new Object[]{
                    evento.getNombre(),
                    evento.getFecha(),
                    evento.getHoraInicio(),
                    evento.getHoraFin(),
                    evento.getUbicacion(),
                    evento.getAsistentes().size()
                            + " / " + evento.getCapacidad()
            };
        }

        actualizando = true;

        try {
            // desconecta el ordenador antes de reemplazar las filas
            tablaEventos.clearSelection();
            tablaEventos.setRowSorter(null);

            eventosVisibles = nuevaLista;
            modeloEventos.setDataVector(filas, COLUMNAS_EVENTOS);
            ajustarColumnasEventos();

            TableRowSorter<DefaultTableModel> ordenador =
                    crearOrdenadorEventos();

            ordenador.setSortKeys(orden);
            tablaEventos.setRowSorter(ordenador);

            seleccionarEvento(idSeleccionado);

            if (tablaEventos.getSelectedRow() < 0
                    && tablaEventos.getRowCount() > 0) {
                tablaEventos.setRowSelectionInterval(0, 0);
            }

            resumen.setText(
                    eventosVisibles.isEmpty()
                            ? "No se encontraron eventos con estos filtros."
                            : eventosVisibles.size()
                            + " evento(s) en la vista actual"
            );
        } finally {
            actualizando = false;
        }

        actualizarDetalle();
    }

    // busca sin distinguir tildes ni mayúsculas
    private String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    private void seleccionarEvento(int id) {
        for (int i = 0; i < eventosVisibles.size(); i++) {
            if (eventosVisibles.get(i).getId() == id) {
                int fila = tablaEventos.convertRowIndexToView(i);

                if (fila >= 0) {
                    tablaEventos.setRowSelectionInterval(fila, fila);
                    tablaEventos.scrollRectToVisible(
                            tablaEventos.getCellRect(fila, 0, true)
                    );
                }
                return;
            }
        }
    }

    // respeta la selección aunque se ordenen las columnas
    private Evento obtenerEventoSeleccionado() {
        if (actualizando) {
            return null;
        }

        int fila = tablaEventos.getSelectedRow();
        if (fila < 0 || fila >= tablaEventos.getRowCount()) {
            return null;
        }

        int indice = tablaEventos.convertRowIndexToModel(fila);
        if (indice < 0 || indice >= eventosVisibles.size()) {
            return null;
        }

        return eventosVisibles.get(indice);
    }

    private void actualizarAcciones() {
        boolean seleccionado = obtenerEventoSeleccionado() != null;

        botonEditar.setEnabled(seleccionado);
        botonAsistente.setEnabled(seleccionado);
        botonAsignar.setEnabled(seleccionado);
        botonTarea.setEnabled(seleccionado);
        botonEliminar.setEnabled(seleccionado);

        actualizarAccionesAsistentes();
        actualizarAccionesRecursos();
        actualizarAccionesAgenda();
    }

    private void actualizarDetalle() {
        if (actualizando) {
            return;
        }

        Evento eventoActual = obtenerEventoSeleccionado();
        String documentoSeleccionado = null;
        int idRecursoSeleccionado = -1;
        LocalTime horaSeleccionada = null;

        if (eventoActual != null
                && eventoActual.getId() == idEventoDetalle) {
            Asistente asistenteAnterior = obtenerAsistenteSeleccionado();

            if (asistenteAnterior != null) {
                documentoSeleccionado =
                        asistenteAnterior.getDocumento();
            }

            Recurso recursoAnterior = obtenerRecursoSeleccionado();

            if (recursoAnterior != null) {
                idRecursoSeleccionado = recursoAnterior.getId();
            }

            horaSeleccionada = obtenerHoraTareaSeleccionada();
        }

        modeloAsistentes.setRowCount(0);
        modeloRecursos.setRowCount(0);
        recursosDelDetalle = List.of();
        modeloAgenda.setRowCount(0);
        horasDelDetalle = List.of();

        Evento evento = obtenerEventoSeleccionado();
        actualizarAcciones();

        if (evento == null) {
            tituloDetalle.setText("Información del evento");
            detalle.setText(
                    "No hay un evento seleccionado.\n\n"
                            + "Podés cambiar los filtros o crear uno nuevo."
            );
        } else {
            tituloDetalle.setText(evento.getNombre());

            detalle.setText(
                    evento.getDescripcion()
                            + "\n\nUbicación: " + evento.getUbicacion()
                            + "\nFecha: " + evento.getFecha().format(FECHA)
                            + "    |    Horario: "
                            + evento.getHoraInicio().format(HORA)
                            + " a " + evento.getHoraFin().format(HORA)
                            + "\nCapacidad: " + evento.getCapacidad()
                            + "    |    Inscriptos: "
                            + evento.getAsistentes().size()
                            + "    |    Lugares disponibles: "
                            + (evento.getCapacidad()
                            - evento.getAsistentes().size())
            );

            for (Asistente asistente : evento.getAsistentes()) {
                modeloAsistentes.addRow(new Object[]{
                        asistente.getNombreCompleto(),
                        asistente.getDocumento(),
                        asistente.getCargo(),
                        asistente.getInstitucion(),
                        asistente.getEmail()
                });
            }

            recursosDelDetalle = new ArrayList<>(
                    evento.getRecursosAsignados().keySet()
            );

            evento.getRecursosAsignados().forEach(
                    (recurso, cantidad) ->
                            modeloRecursos.addRow(new Object[]{
                                    recurso.getNombre(),
                                    recurso.getTipo(),
                                    cantidad
                            })
            );

            horasDelDetalle = new ArrayList<>(
                    evento.getAgenda().keySet()
            );

            evento.getAgenda().forEach(
                    (hora, tarea) ->
                            modeloAgenda.addRow(new Object[]{
                                    hora.format(HORA), tarea
                            })
            );
        }

        idEventoDetalle = evento == null ? -1 : evento.getId();

        if (documentoSeleccionado != null) {
            seleccionarAsistente(documentoSeleccionado);
        }
        if (tablaAsistentes.getSelectedRow() < 0
                && tablaAsistentes.getRowCount() > 0) {
            tablaAsistentes.setRowSelectionInterval(0, 0);
        }

        if (idRecursoSeleccionado >= 0) {
            seleccionarRecurso(idRecursoSeleccionado);
        }
        if (tablaRecursos.getSelectedRow() < 0
                && tablaRecursos.getRowCount() > 0) {
            tablaRecursos.setRowSelectionInterval(0, 0);
        }

        if (horaSeleccionada != null) {
            seleccionarTarea(horaSeleccionada);
        }
        if (tablaAgenda.getSelectedRow() < 0
                && tablaAgenda.getRowCount() > 0) {
            tablaAgenda.setRowSelectionInterval(0, 0);
        }

        actualizarAccionesAsistentes();
        actualizarAccionesRecursos();
        actualizarAccionesAgenda();
        detalle.setCaretPosition(0);

        pestanas.setTitleAt(
                1, "Asistentes (" + modeloAsistentes.getRowCount() + ")"
        );
        pestanas.setTitleAt(
                2, "Recursos (" + modeloRecursos.getRowCount() + ")"
        );
        pestanas.setTitleAt(
                3, "Agenda (" + modeloAgenda.getRowCount() + ")"
        );

        mostrarPanelConsulta(1, modeloAsistentes.getRowCount() > 0);
        mostrarPanelConsulta(2, modeloRecursos.getRowCount() > 0);
        mostrarPanelConsulta(3, modeloAgenda.getRowCount() > 0);
        actualizarAvisoConflictos();
    }

    private boolean confirmarFormulario(JPanel panel, String titulo) {
        return JOptionPane.showConfirmDialog(
                this, panel, titulo,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        ) == JOptionPane.OK_OPTION;
    }

    // conserva los campos cuando hay un error de validación
    private void mostrarFormularioEvento(Evento evento) {
        boolean nuevo = evento == null;

        JTextField nombre = new JTextField(
                nuevo ? "" : evento.getNombre()
        );
        JTextField descripcion = new JTextField(
                nuevo ? "" : evento.getDescripcion()
        );
        JTextField ubicacion = new JTextField(
                nuevo ? "" : evento.getUbicacion()
        );
        JTextField fecha = new JTextField(
                nuevo ? LocalDate.now().toString()
                        : evento.getFecha().toString()
        );
        JTextField inicio = new JTextField(
                nuevo ? "11:00" : evento.getHoraInicio().toString()
        );
        JTextField fin = new JTextField(
                nuevo ? "13:00" : evento.getHoraFin().toString()
        );
        JTextField capacidad = new JTextField(
                nuevo ? "100" : String.valueOf(evento.getCapacidad())
        );

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setPreferredSize(new Dimension(510, 265));

        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Descripción:"));
        formulario.add(descripcion);
        formulario.add(new JLabel("Ubicación:"));
        formulario.add(ubicacion);
        formulario.add(new JLabel("Fecha (AAAA-MM-DD):"));
        formulario.add(fecha);
        formulario.add(new JLabel("Inicio (HH:MM):"));
        formulario.add(inicio);
        formulario.add(new JLabel("Finalización (HH:MM):"));
        formulario.add(fin);
        formulario.add(new JLabel("Capacidad:"));
        formulario.add(capacidad);

        while (confirmarFormulario(
                formulario, nuevo ? "Nuevo evento" : "Editar evento"
        )) {
            Evento guardado;

            try {
                LocalDate fechaEvento =
                        LocalDate.parse(fecha.getText().trim());
                LocalTime horaInicio =
                        LocalTime.parse(inicio.getText().trim());
                LocalTime horaFin =
                        LocalTime.parse(fin.getText().trim());
                int cupo = Integer.parseInt(capacidad.getText().trim());

                if (nuevo) {
                    guardado = new Evento(
                            gestor.obtenerSiguienteIdEvento(),
                            nombre.getText().trim(),
                            descripcion.getText().trim(),
                            ubicacion.getText().trim(),
                            fechaEvento, horaInicio, horaFin, cupo
                    );
                    gestor.registrarEvento(guardado);
                } else {
                    // el gestor revisa los recursos antes de editar
                    gestor.actualizarEvento(
                            evento.getId(),
                            nombre.getText().trim(),
                            descripcion.getText().trim(),
                            ubicacion.getText().trim(),
                            fechaEvento, horaInicio, horaFin, cupo
                    );
                    guardado = evento;
                }
            } catch (RuntimeException e) {
                mostrarErrorDatos(e);
                continue;
            }

            boolean guardadoEnArchivo = guardarCambios();

            actualizarEventos();
            seleccionarEvento(guardado.getId());
            actualizarDetalle();

            if (nuevo) {
                pestanas.setSelectedIndex(0);
            }
            if (guardadoEnArchivo) {
                mostrarEstado("El evento se guardó correctamente.");
            }
            return;
        }
    }

    private void inscribirAsistente() {
        Evento evento = obtenerEventoSeleccionado();

        if (evento == null) {
            return;
        }

        JTextField nombre = new JTextField();
        JTextField documento = new JTextField();
        JTextField cargo = new JTextField();
        JTextField institucion = new JTextField();
        JTextField email = new JTextField();

        documento.setToolTipText("Solo números, sin puntos ni espacios");
        nombre.setToolTipText("Obligatorio. Ejemplo: Ana Pérez");
        email.setToolTipText("Opcional. Ejemplo: nombre@dominio.com");

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setPreferredSize(new Dimension(480, 200));

        formulario.add(new JLabel("Nombre completo *:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Documento * (solo números):"));
        formulario.add(documento);
        formulario.add(new JLabel("Cargo:"));
        formulario.add(cargo);
        formulario.add(new JLabel("Institución:"));
        formulario.add(institucion);
        formulario.add(new JLabel("Correo electrónico (opcional):"));
        formulario.add(email);

        while (confirmarFormulario(
                formulario, "Inscribir asistente - " + evento.getNombre()
        )) {
            try {
                Asistente asistente = new Asistente(
                        gestor.obtenerSiguienteIdAsistente(),
                        nombre.getText().trim(),
                        documento.getText().trim(),
                        cargo.getText().trim(),
                        institucion.getText().trim(),
                        email.getText().trim()
                );

                if (!gestor.inscribirAsistente(evento.getId(), asistente)) {
                    JOptionPane.showMessageDialog(
                            this,
                            "El evento alcanzó su capacidad máxima.",
                            "Sin cupo disponible",
                            JOptionPane.WARNING_MESSAGE
                    );
                    continue;
                }
            } catch (RuntimeException e) {
                mostrarErrorDatos(e);
                continue;
            }

            boolean guardado = guardarCambios();

            actualizarEventos();
            pestanas.setSelectedIndex(1);
            seleccionarAsistente(documento.getText().trim());

            if (guardado) {
                mostrarEstado("El asistente fue registrado correctamente.");
            }
            return;
        }
    }

    // identifica al asistente por documento y no por la posición visual
    private Asistente obtenerAsistenteSeleccionado() {
        if (tablaAsistentes == null) {
            return null;
        }

        Evento evento = obtenerEventoSeleccionado();
        int fila = tablaAsistentes.getSelectedRow();

        if (evento == null || fila < 0
                || fila >= tablaAsistentes.getRowCount()) {
            return null;
        }

        int indice = tablaAsistentes.convertRowIndexToModel(fila);
        String documento = String.valueOf(
                modeloAsistentes.getValueAt(indice, 1)
        );

        return evento.getAsistentes().stream()
                .filter(a -> a.getDocumento().equals(documento))
                .findFirst()
                .orElse(null);
    }

    private void seleccionarAsistente(String documento) {
        for (int i = 0; i < modeloAsistentes.getRowCount(); i++) {
            if (documento.equals(modeloAsistentes.getValueAt(i, 1))) {
                int fila = tablaAsistentes.convertRowIndexToView(i);

                if (fila >= 0) {
                    tablaAsistentes.setRowSelectionInterval(fila, fila);
                    tablaAsistentes.scrollRectToVisible(
                            tablaAsistentes.getCellRect(fila, 0, true)
                    );
                }
                return;
            }
        }
    }

    private void actualizarAccionesAsistentes() {
        if (botonEditarAsistente == null
                || botonCancelarInscripcion == null) {
            return;
        }

        boolean seleccionado = obtenerAsistenteSeleccionado() != null;
        botonEditarAsistente.setEnabled(seleccionado);
        botonCancelarInscripcion.setEnabled(seleccionado);
    }

    private void editarAsistente() {
        Evento evento = obtenerEventoSeleccionado();
        Asistente asistente = obtenerAsistenteSeleccionado();

        if (evento == null || asistente == null) {
            return;
        }

        JTextField nombre = new JTextField(asistente.getNombreCompleto());
        JTextField documento = new JTextField(asistente.getDocumento());
        JTextField cargo = new JTextField(asistente.getCargo());
        JTextField institucion = new JTextField(asistente.getInstitucion());
        JTextField email = new JTextField(asistente.getEmail());

        // conserva el documento con el que se hizo la inscripción
        documento.setEditable(false);
        documento.setBackground(FONDO);
        documento.setToolTipText(
                "El documento no se modifica desde este formulario."
        );

        JPanel campos = new JPanel(new GridLayout(0, 2, 10, 10));
        campos.add(new JLabel("Nombre completo *:"));
        campos.add(nombre);
        campos.add(new JLabel("Documento:"));
        campos.add(documento);
        campos.add(new JLabel("Cargo:"));
        campos.add(cargo);
        campos.add(new JLabel("Institución:"));
        campos.add(institucion);
        campos.add(new JLabel("Correo (opcional):"));
        campos.add(email);

        JPanel formulario = new JPanel(new BorderLayout(0, 12));
        formulario.add(campos, BorderLayout.CENTER);

        JLabel ayuda = new JLabel(
                "* campo obligatorio. El documento se conserva."
        );
        ayuda.setForeground(GRIS);
        formulario.add(ayuda, BorderLayout.SOUTH);
        formulario.setPreferredSize(new Dimension(500, 220));

        while (confirmarFormulario(
                formulario, "Editar asistente - " + evento.getNombre()
        )) {
            try {
                gestor.actualizarAsistente(
                        evento.getId(), asistente.getId(),
                        nombre.getText(), cargo.getText(),
                        institucion.getText(), email.getText()
                );
            } catch (RuntimeException e) {
                mostrarErrorDatos(e);
                continue;
            }

            boolean guardado = guardarCambios();

            actualizarDetalle();
            seleccionarAsistente(asistente.getDocumento());

            if (guardado) {
                mostrarEstado("Datos del asistente actualizados.");
            }
            return;
        }
    }

    private void cancelarInscripcion() {
        Evento evento = obtenerEventoSeleccionado();
        Asistente asistente = obtenerAsistenteSeleccionado();

        if (evento == null || asistente == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Querés cancelar la inscripción de "
                        + asistente.getNombreCompleto()
                        + " en " + evento.getNombre()
                        + "?\nSe liberará un lugar en este evento.",
                "Cancelar inscripción",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (!gestor.cancelarInscripcion(
                    evento.getId(), asistente.getId()
            )) {
                mostrarEstado("La inscripción ya no está registrada.");
                return;
            }
        } catch (RuntimeException e) {
            mostrarErrorDatos(e);
            return;
        }

        boolean guardado = guardarCambios();
        actualizarEventos();

        if (guardado) {
            mostrarEstado("Inscripción cancelada. Se liberó un lugar.");
        }
    }

    private void mostrarEstado(String mensaje) {
        if (cambiosSinGuardar) {
            estado.setForeground(ROJO);
            estado.setText("Hay cambios sin guardar. Usá Reintentar guardado.");
            return;
        }
        estado.setForeground(AZUL);
        estado.setText(mensaje);
    }

    private void registrarRecurso() {
        JTextField nombre = new JTextField();
        JTextField tipo = new JTextField();
        JTextField cantidad = new JTextField("1");

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setPreferredSize(new Dimension(430, 115));

        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Tipo:"));
        formulario.add(tipo);
        formulario.add(new JLabel("Cantidad total:"));
        formulario.add(cantidad);

        while (confirmarFormulario(formulario, "Registrar recurso")) {
            try {
                Recurso recurso = new Recurso(
                        gestor.obtenerSiguienteIdRecurso(),
                        nombre.getText().trim(),
                        tipo.getText().trim(),
                        Integer.parseInt(cantidad.getText().trim())
                );
                gestor.registrarRecurso(recurso);
            } catch (RuntimeException e) {
                mostrarErrorDatos(e);
                continue;
            }

            if (guardarCambios()) {
                mostrarEstado("El recurso fue registrado correctamente.");
            }
            return;
        }
    }

    private Recurso obtenerRecursoSeleccionado() {
        if (tablaRecursos == null) {
            return null;
        }

        Evento evento = obtenerEventoSeleccionado();
        int fila = tablaRecursos.getSelectedRow();

        if (evento == null || fila < 0
                || fila >= tablaRecursos.getRowCount()) {
            return null;
        }

        int indice = tablaRecursos.convertRowIndexToModel(fila);

        if (indice < 0 || indice >= recursosDelDetalle.size()) {
            return null;
        }

        Recurso recurso = recursosDelDetalle.get(indice);

        return evento.obtenerCantidadAsignada(recurso) > 0
                ? recurso : null;
    }

    private void seleccionarRecurso(int idRecurso) {
        for (int i = 0; i < recursosDelDetalle.size(); i++) {
            if (recursosDelDetalle.get(i).getId() == idRecurso) {
                int fila = tablaRecursos.convertRowIndexToView(i);

                if (fila >= 0) {
                    tablaRecursos.setRowSelectionInterval(fila, fila);
                    tablaRecursos.scrollRectToVisible(
                            tablaRecursos.getCellRect(fila, 0, true)
                    );
                }
                return;
            }
        }
    }

    private void actualizarAccionesRecursos() {
        if (botonCambiarCantidad == null || botonQuitarRecurso == null) {
            return;
        }

        boolean seleccionado = obtenerRecursoSeleccionado() != null;
        botonCambiarCantidad.setEnabled(seleccionado);
        botonQuitarRecurso.setEnabled(seleccionado);
    }

    private void asignarRecurso() {
        Evento evento = obtenerEventoSeleccionado();

        if (evento == null) {
            return;
        }

        if (gestor.obtenerRecursos().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero registrá un recurso desde Inventario de recursos.",
                    "No hay recursos",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        mostrarFormularioRecurso(evento, null);
    }

    private void cambiarCantidadRecurso() {
        Evento evento = obtenerEventoSeleccionado();
        Recurso recurso = obtenerRecursoSeleccionado();

        if (evento != null && recurso != null) {
            mostrarFormularioRecurso(evento, recurso);
        }
    }

    // la cantidad ingresada reemplaza el total reservado por el evento
    private void mostrarFormularioRecurso(
            Evento evento, Recurso recursoEditar
    ) {
        JComboBox<Recurso> recursos = new JComboBox<>(
                gestor.obtenerRecursos().toArray(new Recurso[0])
        );

        JTextField cantidad = new JTextField("1");
        JLabel reservaActual = new JLabel();
        JLabel disponible = new JLabel();

        if (recursoEditar != null) {
            recursos.setSelectedItem(recursoEditar);
            recursos.setEnabled(false);
        }

        Runnable actualizarDisponibilidad = () -> {
            Recurso recurso = (Recurso) recursos.getSelectedItem();

            if (recurso == null) {
                return;
            }

            int asignada = evento.obtenerCantidadAsignada(recurso);
            int maximo = gestor.obtenerCantidadDisponible(
                    evento.getId(), recurso.getId()
            );

            cantidad.setText(
                    String.valueOf(asignada > 0 ? asignada : 1)
            );
            reservaActual.setText("Reserva actual: " + asignada);
            disponible.setText(
                    "Máximo total para este evento: " + maximo
            );
        };

        recursos.addActionListener(
                e -> actualizarDisponibilidad.run()
        );
        actualizarDisponibilidad.run();

        JPanel campos = new JPanel(new GridLayout(0, 2, 10, 10));
        campos.add(new JLabel("Recurso:"));
        campos.add(recursos);
        campos.add(new JLabel("Cantidad total a reservar *:"));
        campos.add(cantidad);

        JPanel ayuda = new JPanel(new GridLayout(0, 1, 0, 5));
        ayuda.setOpaque(false);
        ayuda.add(reservaActual);
        ayuda.add(disponible);

        JLabel aclaracion = new JLabel(
                "El máximo incluye la reserva actual. La cantidad nueva la reemplaza."
        );
        aclaracion.setForeground(GRIS);
        aclaracion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ayuda.add(aclaracion);

        JButton verReservas = crearBoton(
                "Ver reservas del horario", Color.WHITE, AZUL
        );
        verReservas.addActionListener(e -> {
            Recurso elegido = (Recurso) recursos.getSelectedItem();
            if (elegido == null) {
                return;
            }
            try {
                int unidades = Integer.parseInt(cantidad.getText().trim());
                mostrarMensaje(
                        gestor.obtenerDetalleDisponibilidad(
                                evento.getId(), elegido.getId(), unidades
                        ),
                        "Disponibilidad de " + elegido.getNombre(),
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (RuntimeException error) {
                mostrarErrorDatos(error);
            }
        });
        JPanel consulta = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        consulta.setOpaque(false);
        consulta.add(verReservas);

        JPanel formulario = new JPanel(new BorderLayout(0, 12));
        formulario.add(campos, BorderLayout.NORTH);
        formulario.add(ayuda, BorderLayout.CENTER);
        formulario.add(consulta, BorderLayout.SOUTH);
        formulario.setPreferredSize(new Dimension(570, 220));

        String titulo = recursoEditar == null
                ? "Asignar recurso" : "Cambiar cantidad";

        while (confirmarFormulario(
                formulario, titulo + " - " + evento.getNombre()
        )) {
            Recurso recurso = (Recurso) recursos.getSelectedItem();

            if (recurso == null) {
                return;
            }

            try {
                int unidades = Integer.parseInt(
                        cantidad.getText().trim()
                );

                if (unidades <= 0) {
                    throw new IllegalArgumentException(
                            "La cantidad debe ser mayor que cero. "
                                    + "Para liberar la reserva, usá Quitar asignación."
                    );
                }

                int maximo = gestor.obtenerCantidadDisponible(
                        evento.getId(), recurso.getId()
                );

                if (unidades > maximo) {
                    throw new IllegalArgumentException(
                            "No se puede confirmar la reserva.\n\n"
                                    + gestor.obtenerDetalleDisponibilidad(
                                    evento.getId(), recurso.getId(), unidades
                            )
                                    + "\n\nPodés reducir la cantidad, elegir otro "
                                    + "recurso o revisar el horario del evento.\n"
                                    + "La reserva anterior no se modificó."
                    );
                }

                if (!gestor.asignarRecurso(
                        evento.getId(), recurso.getId(), unidades
                )) {
                    throw new IllegalArgumentException(
                            "No se pudo confirmar la reserva. "
                                    + "Revisá la disponibilidad."
                    );
                }
            } catch (RuntimeException e) {
                mostrarErrorDatos(e);
                continue;
            }

            boolean guardado = guardarCambios();

            actualizarDetalle();
            seleccionarRecurso(recurso.getId());
            pestanas.setSelectedIndex(2);

            if (guardado) {
                mostrarEstado(
                        "Reserva de " + recurso.getNombre() + " actualizada."
                );
            }
            return;
        }
    }

    private void quitarRecursoAsignado() {
        Evento evento = obtenerEventoSeleccionado();
        Recurso recurso = obtenerRecursoSeleccionado();

        if (evento == null || recurso == null) {
            return;
        }

        int cantidad = evento.obtenerCantidadAsignada(recurso);

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Querés quitar la reserva de " + cantidad
                        + " unidades de " + recurso.getNombre()
                        + " en " + evento.getNombre() + "?\n"
                        + "El recurso seguirá en el inventario.",
                "Quitar asignación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        if (!gestor.quitarRecursoAsignado(
                evento.getId(), recurso.getId()
        )) {
            mostrarEstado("La reserva ya no está registrada.");
            actualizarDetalle();
            return;
        }

        boolean guardado = guardarCambios();
        actualizarDetalle();

        if (guardado) {
            mostrarEstado(
                    "Reserva retirada. El recurso sigue en el inventario."
            );
        }
    }

    // conserva la hora como clave aunque cambie el orden visual
    private LocalTime obtenerHoraTareaSeleccionada() {
        if (tablaAgenda == null) {
            return null;
        }

        Evento evento = obtenerEventoSeleccionado();
        int fila = tablaAgenda.getSelectedRow();

        if (evento == null || fila < 0
                || fila >= tablaAgenda.getRowCount()) {
            return null;
        }

        int indice = tablaAgenda.convertRowIndexToModel(fila);

        if (indice < 0 || indice >= horasDelDetalle.size()) {
            return null;
        }

        LocalTime hora = horasDelDetalle.get(indice);

        return evento.getAgenda().containsKey(hora)
                ? hora : null;
    }

    private void seleccionarTarea(LocalTime hora) {
        for (int i = 0; i < horasDelDetalle.size(); i++) {
            if (horasDelDetalle.get(i).equals(hora)) {
                int fila = tablaAgenda.convertRowIndexToView(i);

                if (fila >= 0) {
                    tablaAgenda.setRowSelectionInterval(fila, fila);
                    tablaAgenda.scrollRectToVisible(
                            tablaAgenda.getCellRect(fila, 0, true)
                    );
                }
                return;
            }
        }
    }

    private void actualizarAccionesAgenda() {
        if (botonEditarTarea == null || botonEliminarTarea == null) {
            return;
        }

        boolean seleccionada =
                obtenerHoraTareaSeleccionada() != null;

        botonEditarTarea.setEnabled(seleccionada);
        botonEliminarTarea.setEnabled(seleccionada);
    }

    private void agregarTarea() {
        Evento evento = obtenerEventoSeleccionado();

        if (evento != null) {
            mostrarFormularioTarea(evento, null);
        }
    }

    private void editarTarea() {
        Evento evento = obtenerEventoSeleccionado();
        LocalTime hora = obtenerHoraTareaSeleccionada();

        if (evento != null && hora != null) {
            mostrarFormularioTarea(evento, hora);
        }
    }

    private void mostrarFormularioTarea(
            Evento evento, LocalTime horaOriginal
    ) {
        boolean nueva = horaOriginal == null;

        JTextField hora = new JTextField(
                nueva
                        ? evento.getHoraInicio().toString()
                        : horaOriginal.toString()
        );

        JTextField descripcion = new JTextField(
                nueva
                        ? ""
                        : evento.getAgenda().get(horaOriginal)
        );

        JPanel campos = new JPanel(new GridLayout(0, 2, 10, 10));
        campos.add(new JLabel("Hora * (HH:MM):"));
        campos.add(hora);
        campos.add(new JLabel("Descripción *:"));
        campos.add(descripcion);

        JLabel ayuda = new JLabel(
                "Horario del evento: "
                        + evento.getHoraInicio().format(HORA)
                        + " a "
                        + evento.getHoraFin().format(HORA)
                        + ". Ambos campos son obligatorios."
        );
        ayuda.setForeground(GRIS);
        ayuda.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel formulario = new JPanel(new BorderLayout(0, 12));
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(ayuda, BorderLayout.SOUTH);
        formulario.setPreferredSize(new Dimension(550, 115));

        String titulo = nueva ? "Agregar tarea" : "Editar tarea";

        while (confirmarFormulario(
                formulario, titulo + " - " + evento.getNombre()
        )) {
            LocalTime horario;

            try {
                horario = LocalTime.parse(hora.getText().trim());
                String texto = descripcion.getText().trim();

                if (nueva) {
                    // valida antes de preguntar si se reemplaza otra tarea
                    if (texto.isBlank()) {
                        throw new IllegalArgumentException(
                                "La descripción de la tarea es obligatoria."
                        );
                    }

                    if (horario.isBefore(evento.getHoraInicio())
                            || horario.isAfter(evento.getHoraFin())) {
                        throw new IllegalArgumentException(
                                "La tarea debe realizarse dentro "
                                        + "del horario del evento."
                        );
                    }

                    if (evento.getAgenda().containsKey(horario)) {
                        int respuesta = JOptionPane.showConfirmDialog(
                                this,
                                "Ya existe una tarea a las "
                                        + horario.format(HORA)
                                        + ". ¿Querés reemplazarla?",
                                "Horario ocupado",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                        );

                        if (respuesta != JOptionPane.YES_OPTION) {
                            continue;
                        }
                    }

                    evento.agregarTarea(horario, texto);
                } else {
                    // evita pisar otra tarea al cambiar la hora
                    evento.actualizarTarea(
                            horaOriginal,
                            horario,
                            texto
                    );
                }
            } catch (RuntimeException e) {
                mostrarErrorDatos(e);
                continue;
            }

            boolean guardado = guardarCambios();

            actualizarDetalle();
            seleccionarTarea(horario);
            pestanas.setSelectedIndex(3);

            if (guardado) {
                mostrarEstado(
                        nueva
                                ? "Tarea agregada."
                                : "Tarea actualizada."
                );
            }
            return;
        }
    }

    private void eliminarTarea() {
        Evento evento = obtenerEventoSeleccionado();
        LocalTime hora = obtenerHoraTareaSeleccionada();

        if (evento == null || hora == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Querés eliminar la tarea de las "
                        + hora.format(HORA)
                        + "?\n"
                        + evento.getAgenda().get(hora),
                "Eliminar tarea",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        evento.quitarTarea(hora);

        boolean guardado = guardarCambios();
        actualizarDetalle();

        if (guardado) {
            mostrarEstado("Tarea eliminada de la agenda.");
        }
    }

    private void eliminarEvento() {
        Evento evento = obtenerEventoSeleccionado();

        if (evento == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Querés eliminar el evento \""
                        + evento.getNombre()
                        + "\"?\nTambién se eliminarán sus inscripciones, "
                        + "asignaciones y tareas.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        gestor.eliminarEvento(evento.getId());

        boolean guardado = guardarCambios();
        actualizarEventos();

        if (guardado) {
            JOptionPane.showMessageDialog(
                    this, "El evento fue eliminado correctamente."
            );
        }
    }

    // permite reintentar sin repetir la operación que cambió los datos
    private boolean guardarCambios() {
        cambiosSinGuardar = true;
        try {
            gestor.guardarDatos();
            cambiosSinGuardar = false;
            botonReintentar.setVisible(false);
            return true;
        } catch (IOException e) {
            botonReintentar.setVisible(true);
            estado.setForeground(ROJO);
            estado.setText("Hay cambios sin guardar. Usá Reintentar guardado.");
            mostrarMensaje(
                    "El cambio está en memoria, pero no se pudo completar "
                            + "el guardado.\n\n"
                            + e.getMessage()
                            + "\n\nPodés corregir el problema y usar "
                            + "Reintentar guardado. No borres carpetas "
                            + "de recuperación.",
                    "Error al guardar", JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    private void cerrarPrograma() {
        if (!cambiosSinGuardar) {
            dispose();
            return;
        }
        Object[] opciones = {"Guardar y salir", "Salir sin guardar", "Volver"};
        int respuesta = JOptionPane.showOptionDialog(
                this,
                "Hay cambios que todavía no se guardaron.\n"
                        + "Si salís sin guardar, esos cambios se perderán.",
                "Cambios pendientes",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, opciones, opciones[0]
        );
        if (respuesta == 0) {
            if (guardarCambios()) {
                dispose();
            }
        } else if (respuesta == 1) {
            dispose();
        }
    }

    private void mostrarCalendarioMensual() {
        Evento evento = obtenerEventoSeleccionado();

        fechaCalendario = evento == null
                ? LocalDate.now() : evento.getFecha();

        mesCalendario = YearMonth.from(fechaCalendario);

        JDialog ventana = new JDialog(
                this, "Calendario de eventos", true
        );
        ventana.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBackground(FONDO);
        contenido.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel tituloMes = new JLabel("", SwingConstants.CENTER);
        tituloMes.setForeground(AZUL);
        tituloMes.setFont(new Font("SansSerif", Font.BOLD, 21));

        JButton anterior = crearBoton("<", AZUL, Color.WHITE);
        JButton siguiente = crearBoton(">", AZUL, Color.WHITE);
        JButton hoy = crearBoton("Hoy", DORADO, AZUL);

        JPanel navegacion = new JPanel(new BorderLayout(10, 0));
        navegacion.setOpaque(false);
        navegacion.add(anterior, BorderLayout.WEST);
        navegacion.add(tituloMes, BorderLayout.CENTER);
        navegacion.add(siguiente, BorderLayout.EAST);

        JPanel superior = new JPanel(new BorderLayout(10, 0));
        superior.setOpaque(false);
        superior.add(navegacion, BorderLayout.CENTER);
        superior.add(hoy, BorderLayout.EAST);

        JPanel grilla = new JPanel(new GridLayout(7, 7, 5, 5));
        grilla.setBackground(FONDO);
        grilla.setPreferredSize(new Dimension(450, 330));

        JTextArea actividades = new JTextArea();
        configurarAreaTexto(actividades);

        JScrollPane scroll = crearScroll(actividades);
        scroll.setPreferredSize(new Dimension(330, 330));

        JPanel centro = new JPanel(new BorderLayout(12, 0));
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
            actualizarCalendario(grilla, tituloMes, actividades);
        });

        siguiente.addActionListener(e -> {
            mesCalendario = mesCalendario.plusMonths(1);
            fechaCalendario = mesCalendario.atDay(1);
            actualizarCalendario(grilla, tituloMes, actividades);
        });

        hoy.addActionListener(e -> {
            fechaCalendario = LocalDate.now();
            mesCalendario = YearMonth.from(fechaCalendario);
            actualizarCalendario(grilla, tituloMes, actividades);
        });

        contenido.add(superior, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(leyenda, BorderLayout.SOUTH);

        ventana.setContentPane(contenido);
        actualizarCalendario(grilla, tituloMes, actividades);
        ventana.setMinimumSize(new Dimension(810, 470));
        ventana.pack();
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    private void actualizarCalendario(
            JPanel grilla, JLabel tituloMes, JTextArea actividades
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
            etiqueta.setForeground(AZUL);
            etiqueta.setFont(new Font("SansSerif", Font.BOLD, 12));
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
            List<Evento> eventos =
                    gestor.obtenerEventosPorFecha(fecha);

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
                actualizarCalendario(grilla, tituloMes, actividades);
            });

            grilla.add(boton);
        }

        // completa las celdas vacías del mes
        while (grilla.getComponentCount() < 49) {
            grilla.add(new JLabel());
        }

        mostrarActividadesDelDia(actividades);
        grilla.revalidate();
        grilla.repaint();
    }

    private void mostrarActividadesDelDia(JTextArea area) {
        area.setText(
                "ACTIVIDADES DEL "
                        + fechaCalendario.format(FECHA) + "\n\n"
        );

        List<Evento> eventos =
                gestor.obtenerEventosPorFecha(fechaCalendario);

        if (eventos.isEmpty()) {
            area.append("No hay eventos programados para esta fecha.");
        } else {
            for (Evento evento : eventos) {
                area.append(
                        evento.getNombre() + "\n"
                                + evento.getHoraInicio().format(HORA)
                                + " a "
                                + evento.getHoraFin().format(HORA)
                                + "\n" + evento.getUbicacion()
                                + "\n\nAGENDA\n"
                );

                if (evento.getAgenda().isEmpty()) {
                    area.append("Sin tareas registradas.\n");
                } else {
                    evento.getAgenda().forEach(
                            (hora, tarea) ->
                                    area.append(
                                            hora.format(HORA)
                                                    + " - " + tarea + "\n"
                                    )
                    );
                }

                area.append("\n--------------------\n\n");
            }
        }

        area.setCaretPosition(0);
    }

    private void mostrarNotificaciones() {
        List<Evento> eventos = gestor.obtenerEventosProximos(3);

        if (eventos.isEmpty()) {
            return;
        }

        JTextArea aviso = new JTextArea();
        configurarAreaTexto(aviso);
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

        JScrollPane scroll = crearScroll(aviso);
        scroll.setPreferredSize(new Dimension(470, 240));

        JOptionPane.showMessageDialog(
                this, scroll, "Recordatorio de eventos",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    // reúne el inventario sin mezclarlo con las reservas de un evento
    private void mostrarInventario() {
        JDialog ventana = new JDialog(this, "Inventario de recursos", true);
        ventana.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{
                        "Id", "Recurso", "Tipo", "Total",
                        "Fuera de servicio", "Operativos", "Eventos a revisar"
                }, 0
        );
        JTable tabla = crearTabla(modelo);

        Runnable actualizar = () -> {
            int idSeleccionado = -1;
            if (tabla.getSelectedRow() >= 0) {
                int indice = tabla.convertRowIndexToModel(tabla.getSelectedRow());
                idSeleccionado = (Integer) modelo.getValueAt(indice, 0);
            }
            tabla.clearSelection();
            tabla.setRowSorter(null);
            modelo.setRowCount(0);

            for (Recurso recurso : gestor.obtenerRecursos()) {
                modelo.addRow(new Object[]{
                        recurso.getId(), recurso.getNombre(), recurso.getTipo(),
                        recurso.getCantidadTotal(),
                        recurso.getCantidadFueraServicio(),
                        recurso.getCantidadOperativa(),
                        gestor.obtenerEventosConConflictoDeRecurso(
                                recurso.getId()
                        ).size()
                });
            }

            TableRowSorter<DefaultTableModel> ordenador =
                    new TableRowSorter<>(modelo);
            java.util.Comparator<Object> numeros = (a, b) ->
                    Integer.compare(
                            ((Number) a).intValue(), ((Number) b).intValue()
                    );
            for (int columna : new int[]{0, 3, 4, 5, 6}) {
                ordenador.setComparator(columna, numeros);
            }
            tabla.setRowSorter(ordenador);

            for (int i = 0; i < modelo.getRowCount(); i++) {
                if ((Integer) modelo.getValueAt(i, 0) == idSeleccionado) {
                    int vista = tabla.convertRowIndexToView(i);
                    tabla.setRowSelectionInterval(vista, vista);
                    break;
                }
            }
            if (tabla.getSelectedRow() < 0 && tabla.getRowCount() > 0) {
                tabla.setRowSelectionInterval(0, 0);
            }
        };

        JButton registrar = crearBoton("Registrar recurso", DORADO, AZUL);
        JButton estadoRecurso = crearBoton(
                "Actualizar fuera de servicio", Color.WHITE, AZUL
        );
        JButton revisar = crearBoton(
                "Ver eventos a revisar", Color.WHITE, AZUL
        );
        JButton cerrar = crearBoton("Cerrar", Color.WHITE, AZUL);

        registrar.addActionListener(e -> {
            registrarRecurso();
            actualizar.run();
            actualizarDetalle();
        });
        estadoRecurso.addActionListener(e -> {
            Recurso recurso = obtenerRecursoInventario(tabla, modelo);
            if (recurso != null) {
                modificarFueraServicio(recurso);
                actualizar.run();
            }
        });
        revisar.addActionListener(e -> {
            Recurso recurso = obtenerRecursoInventario(tabla, modelo);
            if (recurso != null) {
                mostrarMensaje(
                        gestor.obtenerResumenConflictosRecurso(recurso.getId()),
                        "Revisión de reservas", JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
        cerrar.addActionListener(e -> ventana.dispose());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            boolean seleccionado = tabla.getSelectedRow() >= 0;
            estadoRecurso.setEnabled(seleccionado);
            revisar.setEnabled(seleccionado);
        });

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        acciones.setOpaque(false);
        acciones.add(registrar);
        acciones.add(estadoRecurso);
        acciones.add(revisar);
        acciones.add(cerrar);

        JLabel ayuda = new JLabel(
                "<html>Operativos = total menos unidades fuera de servicio."
                        + "<br>Las reservas se conservan. Los eventos a revisar "
                        + "son los que aún no terminaron y tienen un conflicto.</html>"
        );
        ayuda.setForeground(GRIS);

        JPanel pie = new JPanel(new BorderLayout(0, 10));
        pie.setOpaque(false);
        pie.add(ayuda, BorderLayout.NORTH);
        pie.add(acciones, BorderLayout.SOUTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBackground(FONDO);
        contenido.setBorder(new EmptyBorder(15, 15, 15, 15));
        contenido.add(crearScroll(tabla), BorderLayout.CENTER);
        contenido.add(pie, BorderLayout.SOUTH);

        ventana.setContentPane(contenido);
        ventana.setSize(1000, 440);
        ventana.setMinimumSize(new Dimension(920, 390));
        ventana.setLocationRelativeTo(this);
        actualizar.run();
        estadoRecurso.setEnabled(tabla.getSelectedRow() >= 0);
        revisar.setEnabled(tabla.getSelectedRow() >= 0);
        ventana.setVisible(true);
    }

    private Recurso obtenerRecursoInventario(
            JTable tabla, DefaultTableModel modelo
    ) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        int indice = tabla.convertRowIndexToModel(fila);
        int id = (Integer) modelo.getValueAt(indice, 0);
        return gestor.buscarRecursoPorId(id);
    }

    private void modificarFueraServicio(Recurso recurso) {
        JTextField cantidad = new JTextField(
                String.valueOf(recurso.getCantidadFueraServicio())
        );
        JPanel campos = new JPanel(new GridLayout(0, 2, 10, 10));
        campos.add(new JLabel("Recurso:"));
        campos.add(new JLabel(recurso.getNombre()));
        campos.add(new JLabel("Total en inventario:"));
        campos.add(new JLabel(String.valueOf(recurso.getCantidadTotal())));
        campos.add(new JLabel("Fuera de servicio:"));
        campos.add(cantidad);

        JLabel ayuda = new JLabel(
                "<html>Ingresá el total fuera de servicio, "
                        + "no una cantidad para sumar."
                        + "<br>Para devolver todas las unidades al servicio, ingresá 0."
                        + "<br>Las reservas existentes no se eliminarán.</html>"
        );
        ayuda.setForeground(GRIS);

        JPanel formulario = new JPanel(new BorderLayout(0, 14));
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(ayuda, BorderLayout.SOUTH);
        formulario.setPreferredSize(new Dimension(530, 190));

        while (confirmarFormulario(
                formulario, "Estado de " + recurso.getNombre()
        )) {
            int unidades;
            try {
                unidades = Integer.parseInt(cantidad.getText().trim());
                if (unidades < 0 || unidades > recurso.getCantidadTotal()) {
                    throw new IllegalArgumentException(
                            "La cantidad fuera de servicio debe estar entre 0 y "
                                    + recurso.getCantidadTotal() + "."
                    );
                }
            } catch (RuntimeException e) {
                mostrarErrorDatos(e);
                continue;
            }

            if (unidades == recurso.getCantidadFueraServicio()) {
                mostrarEstado("El recurso conserva su estado actual.");
                return;
            }

            int operativas = recurso.getCantidadTotal() - unidades;
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "Quedarán " + operativas + " unidades operativas de "
                            + recurso.getNombre() + ".\n"
                            + "Se conservarán las reservas y se señalarán "
                            + "los eventos afectados.\n¿Confirmás el cambio?",
                    "Confirmar estado del recurso",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
            );
            if (respuesta != JOptionPane.YES_OPTION) {
                continue;
            }

            List<Evento> afectados;
            try {
                afectados = gestor.actualizarCantidadFueraServicio(
                        recurso.getId(), unidades
                );
            } catch (RuntimeException e) {
                mostrarErrorDatos(e);
                continue;
            }

            boolean guardado = guardarCambios();
            actualizarDetalle();
            if (guardado) {
                mostrarEstado("Estado de " + recurso.getNombre() + " actualizado.");
            }

            String resultado = guardado
                    ? "El estado del recurso se guardó.\n\n"
                    : "ATENCIÓN: el cambio está en memoria, pero falta guardarlo.\n\n";
            mostrarMensaje(
                    resultado
                            + gestor.obtenerResumenConflictosRecurso(recurso.getId()),
                    afectados.isEmpty()
                            ? "Estado del recurso"
                            : "Reservas que necesitan revisión",
                    afectados.isEmpty()
                            ? JOptionPane.INFORMATION_MESSAGE
                            : JOptionPane.WARNING_MESSAGE
            );
            return;
        }
    }

    private void actualizarAvisoConflictos() {
        if (avisoConflictos == null) {
            return;
        }
        long cantidad = gestor.obtenerEventosFuturos().stream()
                .filter(evento -> gestor.tieneConflictosDeRecursos(evento.getId()))
                .count();

        avisoConflictos.setText(
                cantidad + " evento(s) necesitan revisar sus recursos. "
                        + "Consultá Inventario de recursos."
        );
        avisoConflictos.setVisible(cantidad > 0);

        Evento evento = obtenerEventoSeleccionado();
        if (evento != null && !evento.esPasado()
                && gestor.tieneConflictosDeRecursos(evento.getId())) {
            tituloDetalle.setForeground(ROJO);
            tituloDetalle.setText(evento.getNombre() + " · recursos a revisar");
        } else {
            tituloDetalle.setForeground(AZUL);
        }
    }

    private void mostrarConflictosPendientes() {
        StringBuilder mensaje = new StringBuilder();
        for (Recurso recurso : gestor.obtenerRecursos()) {
            if (!gestor.obtenerEventosConConflictoDeRecurso(
                    recurso.getId()
            ).isEmpty()) {
                if (!mensaje.isEmpty()) {
                    mensaje.append("\n\n--------------------\n\n");
                }
                mensaje.append(
                        gestor.obtenerResumenConflictosRecurso(recurso.getId())
                );
            }
        }
        if (!mensaje.isEmpty()) {
            mostrarMensaje(
                    "Hay reservas pendientes de reorganización.\n\n" + mensaje,
                    "Recursos a revisar", JOptionPane.WARNING_MESSAGE
            );
        }
    }

    // permite recorrer mensajes extensos sin agrandar toda la ventana
    private void mostrarMensaje(String mensaje, String titulo, int tipo) {
        String texto = mensaje == null || mensaje.isBlank()
                ? "No se pudo completar la operación." : mensaje;

        if (texto.length() <= 300 && texto.lines().count() <= 5) {
            JOptionPane.showMessageDialog(this, texto, titulo, tipo);
            return;
        }
        JTextArea area = new JTextArea(texto);
        configurarAreaTexto(area);
        area.setCaretPosition(0);
        JScrollPane scroll = crearScroll(area);
        scroll.setPreferredSize(new Dimension(590, 300));
        JOptionPane.showMessageDialog(this, scroll, titulo, tipo);
    }

    private void mostrarErrorDatos(RuntimeException e) {
        String mensaje;

        if (e instanceof NumberFormatException) {
            mensaje = "Ingresá un número entero válido "
                    + "en los campos de capacidad o cantidad.";
        } else if (e instanceof DateTimeParseException) {
            mensaje = "Revisá la fecha y los horarios.\n"
                    + "Fecha: AAAA-MM-DD. Hora: HH:MM.";
        } else {
            mensaje = e.getMessage();
        }

        mostrarMensaje(
                mensaje, "Revisá los datos", JOptionPane.WARNING_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazPrincipal ventana = new InterfazPrincipal();

            // abre solamente si la carga terminó correctamente
            if (!ventana.datosCargados) {
                ventana.dispose();
                return;
            }

            ventana.setVisible(true);
            ventana.mostrarNotificaciones();
            ventana.mostrarConflictosPendientes();
        });
    }
}
