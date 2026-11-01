package agora.vista;

import agora.modelo.Asistente;
import agora.modelo.Evento;
import agora.modelo.Recurso;
import agora.servicio.GestorEventos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
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
    private final JComboBox<String> filtroEventos;
    private final JTextField campoBusqueda;
    private final JLabel resumen;
    private final JLabel tituloDetalle;
    private final JTextArea detalle;
    private final JTabbedPane pestanas;

    private final boolean datosCargados;

    private List<Evento> eventosVisibles;

    private JButton botonEditar;
    private JButton botonAsistente;
    private JButton botonAsignar;
    private JButton botonTarea;
    private JButton botonEliminar;

    private YearMonth mesCalendario;
    private LocalDate fechaCalendario;

    private boolean actualizando;

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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        cuerpo.add(crearBarraSuperior(), BorderLayout.NORTH);

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

        JLabel ayuda = new JLabel(
                "Seleccioná un evento para ver sus asistentes, recursos y agenda"
        );
        ayuda.setForeground(GRIS);
        ayuda.setFont(new Font("SansSerif", Font.PLAIN, 12));

        pie.add(resumen, BorderLayout.WEST);
        pie.add(ayuda, BorderLayout.EAST);
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
        calendario.addActionListener(
                e -> mostrarCalendarioMensual()
        );

        JButton recursos = crearBoton(
                "Registrar recurso", Color.WHITE, AZUL
        );
        recursos.addActionListener(
                e -> registrarRecurso()
        );

        JButton nuevo = crearBoton(
                "+ Nuevo evento", DORADO, AZUL
        );
        nuevo.addActionListener(
                e -> mostrarFormularioEvento(null)
        );

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
        filtroEventos.addActionListener(
                e -> actualizarEventos()
        );
        filtro.add(filtroEventos);

        JPanel busqueda = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 6, 0)
        );
        busqueda.setOpaque(false);

        campoBusqueda.setPreferredSize(new Dimension(210, 29));
        campoBusqueda.setToolTipText(
                "Buscá por nombre del evento o ubicación"
        );

        JButton buscar = crearBoton(
                "Buscar", Color.WHITE, AZUL
        );
        JButton limpiar = crearBoton(
                "Limpiar", Color.WHITE, AZUL
        );

        buscar.addActionListener(e -> actualizarEventos());
        campoBusqueda.addActionListener(e -> actualizarEventos());

        limpiar.addActionListener(e -> {
            campoBusqueda.setText("");
            actualizarEventos();
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

        acciones.add(botonEditar);
        acciones.add(botonAsistente);
        acciones.add(botonAsignar);
        acciones.add(botonTarea);
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

        tituloDetalle.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        tituloDetalle.setForeground(AZUL);

        configurarAreaTexto(detalle);

        JTable asistentes = crearTabla(modeloAsistentes);
        JTable recursos = crearTabla(modeloRecursos);
        JTable agenda = crearTabla(modeloAgenda);

        asistentes.getColumnModel().getColumn(0)
                .setPreferredWidth(170);
        asistentes.getColumnModel().getColumn(4)
                .setPreferredWidth(200);

        agenda.getColumnModel().getColumn(0)
                .setPreferredWidth(70);
        agenda.getColumnModel().getColumn(1)
                .setPreferredWidth(800);

        pestanas.setForeground(AZUL);
        pestanas.setBackground(FONDO);
        pestanas.addTab("Detalle", crearScroll(detalle));

        pestanas.addTab(
                "Asistentes (0)",
                crearPanelConsulta(
                        asistentes,
                        "Este evento todavía no tiene asistentes."
                )
        );

        pestanas.addTab(
                "Recursos (0)",
                crearPanelConsulta(
                        recursos,
                        "Este evento todavía no tiene recursos asignados."
                )
        );

        pestanas.addTab(
                "Agenda (0)",
                crearPanelConsulta(
                        agenda,
                        "Este evento todavía no tiene tareas registradas."
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
        JPanel panel = (JPanel) pestanas.getComponentAt(indice);
        CardLayout tarjetas = (CardLayout) panel.getLayout();
        tarjetas.show(panel, tieneDatos ? "tabla" : "vacio");
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
        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tabla.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        tabla.getTableHeader().setForeground(AZUL);
        tabla.getTableHeader().setBackground(
                new Color(237, 241, 244)
        );
        tabla.getTableHeader().setPreferredSize(
                new Dimension(0, 30)
        );
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private void configurarTablaEventos() {
        aplicarEstiloTabla(tablaEventos);
        tablaEventos.setRowHeight(34);
        ajustarColumnasEventos();

        tablaEventos.setRowSorter(
                new TableRowSorter<>(modeloEventos)
        );

        tablaEventos.getSelectionModel().addListSelectionListener(e -> {
            if (!actualizando && !e.getValueIsAdjusting()) {
                actualizarDetalle();
            }
        });
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
            orden.addAll(
                    tablaEventos.getRowSorter().getSortKeys()
            );
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
                    new TableRowSorter<>(modeloEventos);

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
        return Normalizer.normalize(
                        texto.trim(), Normalizer.Form.NFD
                )
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
    }

    private void actualizarDetalle() {
        if (actualizando) {
            return;
        }

        modeloAsistentes.setRowCount(0);
        modeloRecursos.setRowCount(0);
        modeloAgenda.setRowCount(0);

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
                            + "\n\nUbicación: "
                            + evento.getUbicacion()
                            + "\nFecha: "
                            + evento.getFecha().format(FECHA)
                            + "    |    Horario: "
                            + evento.getHoraInicio().format(HORA)
                            + " a "
                            + evento.getHoraFin().format(HORA)
                            + "\nCapacidad: "
                            + evento.getCapacidad()
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

            evento.getRecursosAsignados().forEach(
                    (recurso, cantidad) ->
                            modeloRecursos.addRow(new Object[]{
                                    recurso.getNombre(),
                                    recurso.getTipo(),
                                    cantidad
                            })
            );

            evento.getAgenda().forEach(
                    (hora, tarea) ->
                            modeloAgenda.addRow(new Object[]{
                                    hora.format(HORA), tarea
                            })
            );
        }

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
    }

    private boolean confirmarFormulario(JPanel panel, String titulo) {
        return JOptionPane.showConfirmDialog(
                this,
                panel,
                titulo,
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
                            fechaEvento,
                            horaInicio,
                            horaFin,
                            cupo
                    );

                    gestor.registrarEvento(guardado);
                } else {
                    // el gestor revisa los recursos antes de editar
                    gestor.actualizarEvento(
                            evento.getId(),
                            nombre.getText().trim(),
                            descripcion.getText().trim(),
                            ubicacion.getText().trim(),
                            fechaEvento,
                            horaInicio,
                            horaFin,
                            cupo
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
            pestanas.setSelectedIndex(0);

            if (guardadoEnArchivo) {
                JOptionPane.showMessageDialog(
                        this, "El evento se guardó correctamente."
                );
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
        email.setToolTipText("Opcional. Ejemplo: nombre@dominio.com");

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setPreferredSize(new Dimension(480, 200));

        formulario.add(new JLabel("Nombre completo:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Documento (solo números):"));
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

            if (guardado) {
                JOptionPane.showMessageDialog(
                        this, "El asistente fue registrado correctamente."
                );
            }
            return;
        }
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
                JOptionPane.showMessageDialog(
                        this, "El recurso fue registrado correctamente."
                );
            }
            return;
        }
    }

    private void asignarRecurso() {
        Evento evento = obtenerEventoSeleccionado();

        if (evento == null) {
            return;
        }

        if (gestor.obtenerRecursos().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero registrá un recurso con el botón "
                            + "\"Registrar recurso\".",
                    "No hay recursos",
                    JOptionPane.INFORMATION_MESSAGE
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

        while (confirmarFormulario(
                formulario, "Asignar recurso - " + evento.getNombre()
        )) {
            try {
                Recurso recurso = (Recurso) recursos.getSelectedItem();

                if (recurso == null) {
                    return;
                }

                int unidades = Integer.parseInt(cantidad.getText().trim());

                if (unidades <= 0) {
                    throw new IllegalArgumentException(
                            "La cantidad debe ser mayor que cero."
                    );
                }

                if (!gestor.asignarRecurso(
                        evento.getId(), recurso.getId(), unidades
                )) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No hay suficiente cantidad disponible "
                                    + "para el horario de este evento.",
                            "Recurso no disponible",
                            JOptionPane.WARNING_MESSAGE
                    );
                    continue;
                }
            } catch (RuntimeException e) {
                mostrarErrorDatos(e);
                continue;
            }

            boolean guardado = guardarCambios();
            actualizarDetalle();
            pestanas.setSelectedIndex(2);

            if (guardado) {
                JOptionPane.showMessageDialog(
                        this, "El recurso fue asignado correctamente."
                );
            }
            return;
        }
    }

    private void agregarTarea() {
        Evento evento = obtenerEventoSeleccionado();

        if (evento == null) {
            return;
        }

        JTextField hora = new JTextField(
                evento.getHoraInicio().toString()
        );
        JTextField descripcion = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setPreferredSize(new Dimension(470, 80));

        formulario.add(new JLabel("Hora (HH:MM):"));
        formulario.add(hora);
        formulario.add(new JLabel("Descripción:"));
        formulario.add(descripcion);

        while (confirmarFormulario(
                formulario, "Agregar tarea - " + evento.getNombre()
        )) {
            try {
                LocalTime horario = LocalTime.parse(hora.getText().trim());

                // evita reemplazar una tarea sin avisar
                if (evento.getAgenda().containsKey(horario)) {
                    int respuesta = JOptionPane.showConfirmDialog(
                            this,
                            "Ya existe una tarea a esa hora. "
                                    + "¿Querés reemplazarla?",
                            "Horario ocupado",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (respuesta != JOptionPane.YES_OPTION) {
                        continue;
                    }
                }

                evento.agregarTarea(
                        horario, descripcion.getText().trim()
                );
            } catch (RuntimeException e) {
                mostrarErrorDatos(e);
                continue;
            }

            boolean guardado = guardarCambios();
            actualizarDetalle();
            pestanas.setSelectedIndex(3);

            if (guardado) {
                JOptionPane.showMessageDialog(
                        this, "La tarea fue guardada correctamente."
                );
            }
            return;
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

    // no repite la operación si falla el guardado
    private boolean guardarCambios() {
        try {
            gestor.guardarDatos();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "El cambio se realizó en memoria, pero no se pudo "
                            + "completar el guardado en los archivos.\n"
                            + "Revisá la carpeta datos antes de cerrar.\n\n"
                            + e.getMessage(),
                    "Error al guardar",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
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

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Revisá los datos",
                JOptionPane.WARNING_MESSAGE
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
        });
    }
}