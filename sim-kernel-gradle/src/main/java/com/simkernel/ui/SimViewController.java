package com.simkernel.ui;



import com.simkernel.ai.GroqHelper;
import com.simkernel.core.Archivo;
import com.simkernel.core.EstrategiaMemoria;
import com.simkernel.core.FileSystem;
import com.simkernel.core.Impresora;
import com.simkernel.core.RecursoCompartido;
import com.simkernel.memory.Memoria;
import com.simkernel.process.Proceso;
import com.simkernel.security.Usuario;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SimViewController {

    @FXML private TableView<Proceso> tablaProcesos;
    @FXML private TableColumn<Proceso, String> colPid;
    @FXML private TableColumn<Proceso, String> colNombre;
    @FXML private TableColumn<Proceso, String> colEstado;
    @FXML private TableColumn<Proceso, String> colTiempo;
    @FXML private TableColumn<Proceso, String> colUsuario;
    @FXML private ProgressBar barraMemoria;
    @FXML private Button btnSimular;
    @FXML private TableView<Proceso> tablaBloqueados;
    @FXML private TableColumn<Proceso, String> colBloqPid;
    @FXML private TableColumn<Proceso, String> colBloqNombre;
    @FXML private TableColumn<Proceso, String> colBloqMotivo;
    @FXML private TableColumn<Proceso, String> colBloqUsuario;
    @FXML private ComboBox<String> comboEstrategia;
    @FXML private ListView<String> listaProcesosEnEspera;
    @FXML private ListView<String> listaSeguridad;
    @FXML private ComboBox<String> comboPlanificador;
    @FXML private TextField txtQuantum;
    @FXML private TextField txtNombreProceso;
    @FXML private TextField txtTiempoProceso;
    @FXML private TextField txtMemoriaProceso;
    @FXML private TextField txtPrioridadProceso;
    @FXML private Button btnAgregarProceso;
    @FXML private Label lblImpresora;
    @FXML private ListView<String> listaColaImpresora;
    @FXML private Button btnLiberarImpresora;
    @FXML private Label lblArchivo;
    @FXML private ListView<String> listaColaArchivo;
    @FXML private Button btnLiberarArchivo;
    @FXML private Label lblEstadoSemaforoImpresora;
    @FXML private Label lblEstadoSemaforoArchivo;
    @FXML private ListView<String> listaBloquesMemoria;
    @FXML private ComboBox<String> comboArchivoProceso;
    @FXML private ListView<String> listaEstadoArchivos;
    @FXML private TableView<ObservableList<String>> tablaGantt;
    @FXML private TableColumn<ObservableList<String>, String> colGanttProceso;
    @FXML private TableView<ObservableList<String>> tablaMemoriaGantt;
    @FXML private TableColumn<ObservableList<String>, String> colMemoriaProceso;
    @FXML private TableColumn<ObservableList<String>, String> colMemoriaInicio;
    @FXML private TableColumn<ObservableList<String>, String> colMemoriaTamanio;
    @FXML private TableColumn<ObservableList<String>, String> colMemoriaAlgoritmo;
    @FXML private Button btnSugerirProcesosIA;
    @FXML private Button btnModoOscuro; // Botón para modo oscuro
    @FXML private ListView<MensajeChat> chatListView;
    @FXML private Button btnLimpiarChat;
    @FXML private TextField inputPregunta;
    @FXML private TextArea logArea;

    private GroqHelper aiHelper;
    private final ObservableList<Proceso> listaProcesos = FXCollections.observableArrayList();
    private final ObservableList<Proceso> listaBloqueados = FXCollections.observableArrayList();
    private final ObservableList<Proceso> procesosUsuario = FXCollections.observableArrayList();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private Memoria memoria;
    private Impresora impresora = new Impresora();
    private Archivo archivo = new Archivo();
    private FileSystem fileSystem;
    private int nextPid = 1;
    private final ObservableList<ObservableList<String>> datosGantt = FXCollections.observableArrayList();
    private List<TableColumn<ObservableList<String>, String>> columnasTiempoGantt = FXCollections.observableArrayList();
    private List<List<String>> historiaGantt = new java.util.ArrayList<>();
    private final ObservableList<ObservableList<String>> datosMemoriaGantt = FXCollections.observableArrayList();
    private final ObservableList<MensajeChat> mensajesChat = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuración de columnas
        Tooltip.install(btnSimular, new Tooltip("Haz clic para iniciar la simulación del sistema operativo"));
        Tooltip.install(comboEstrategia, new Tooltip("Selecciona cómo se asignará la memoria a los procesos"));
        colPid.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getPid())));
        colNombre.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombre()));
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstado().toString()));
        colTiempo.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getTiempoCpu())));
        colUsuario.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsuario().getNombre()));
        colBloqPid.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPid())));
        colBloqNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colBloqMotivo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado().name()));
        colBloqUsuario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsuario().getNombre()));
        //comboEstrategia.getItems().addAll("First Fit", "Best Fit");
        comboEstrategia.setValue("First Fit");

        tablaProcesos.setItems(listaProcesos);

        // Acción del botón
        btnSimular.setOnAction(event -> simularProcesos());
        btnAgregarProceso.setOnAction(event -> agregarProcesoUsuario());
        btnLiberarImpresora.setOnAction(event -> liberarImpresoraManual());
        btnLiberarArchivo.setOnAction(event -> liberarArchivoManual());
        btnSugerirProcesosIA.setOnAction(event -> sugerirProcesosIA());
        // Botón modo oscuro
        btnModoOscuro.setOnAction(e -> toggleModoOscuro());
        Tooltip.install(btnModoOscuro, new Tooltip("Activa o desactiva el modo oscuro"));

        // Animación de aparición suave
        animarInicio();

        // Initialize aiHelper with environment variable
        // En initialize(), cambia OpenAIHelper por GroqHelper:
        String apiKey = System.getenv("GROQ_API_KEY");  // O ponla directamente: "tu-api-key-aquí"
        if (apiKey == null || apiKey.isEmpty()) {
            agregarLog("⚠️ Error: GROQ_API_KEY no configurada.");
        } else {
            aiHelper = new GroqHelper(apiKey);  // ¡Aquí usas GroqHelper!
        }

        // Ejemplo: al presionar enter en el campo de texto
        inputPregunta.setOnAction(e -> {
            if (aiHelper == null) {
                agregarLog("⚠️ Error: AI no está inicializada.");
                return;
            }
            String pregunta = inputPregunta.getText();
            if (!pregunta.isEmpty()) {
                try {
                    String respuesta = aiHelper.generarRespuesta("Explícale a un estudiante qué está pasando con los procesos: " + pregunta);
                    agregarLog("🧠 AI: " + respuesta);
                } catch (IOException ex) {
                    agregarLog("⚠️ Error al conectar con la API de OpenAI: " + ex.getMessage() + "\n");
                }
                inputPregunta.clear();
            }
        });
        FadeTransition fadeChat = new FadeTransition(Duration.millis(400), chatListView);
        fadeChat.setFromValue(0.6);
        fadeChat.setToValue(1.0);
        fadeChat.play();

       // btnEnviar.setOnAction(e -> procesarPregunta());
        inputPregunta.setOnAction(e -> procesarPregunta());


        memoria = new Memoria(512); // Por ejemplo, 512 MB de RAM simulada

        // Crear usuarios de ejemplo y asignar permisos
        Usuario admin = new Usuario("admin");
        admin.agregarPermiso("Impresora");
        admin.agregarPermiso("Archivo");
        Usuario user1 = new Usuario("user1");
        user1.agregarPermiso("Impresora");
        Usuario user2 = new Usuario("user2");
        user2.agregarPermiso("Archivo");
        usuarios.addAll(admin, user1, user2);
        listaBloquesMemoria.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Libre")) {
                        setStyle("-fx-background-color: #d4fcd4; -fx-text-fill: #228B22;"); // verde claro
                    } else {
                        setStyle("-fx-background-color: #ffd6d6; -fx-text-fill: #b22222;"); // rojo claro
                    }
                }
            }
        });
        // Inicializar sistema de archivos simulado con 3 archivos
        fileSystem = new FileSystem(List.of("archivo1.txt", "archivo2.txt", "datos.db"));
        comboArchivoProceso.getItems().setAll(fileSystem.getArchivos().stream().map(FileSystem.ArchivoSimulado::getNombre).toList());
        if (!comboArchivoProceso.getItems().isEmpty())
            comboArchivoProceso.setValue(comboArchivoProceso.getItems().get(0));
        // Configuración visual del chat tipo burbujas
        chatListView.setItems(mensajesChat);
        chatListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(MensajeChat item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox();
                    hbox.setSpacing(8);
                    Text txt = new Text(item.getTexto());
                    txt.wrappingWidthProperty().bind(chatListView.widthProperty().subtract(60));
                    txt.getStyleClass().add("chat-bubble-text");
                    Label bubble = new Label();
                    bubble.setGraphic(txt);
                    bubble.getStyleClass().add("chat-bubble");
                    if (item.getAutor() == MensajeChat.Autor.USUARIO) {
                        bubble.getStyleClass().add("chat-bubble-user");
                        hbox.getChildren().addAll(new Region(), bubble);
                        HBox.setHgrow(hbox.getChildren().get(0), Priority.ALWAYS);
                    } else {
                        bubble.getStyleClass().add("chat-bubble-ia");
                        hbox.getChildren().addAll(bubble, new Region());
                        HBox.setHgrow(hbox.getChildren().get(1), Priority.ALWAYS);
                    }
                    setGraphic(hbox);
                }
            }
        });
        // Botón limpiar chat
        btnLimpiarChat.setOnAction(e -> mensajesChat.clear());
    }

    private void agregarProcesoUsuario() {
        String nombre = txtNombreProceso.getText().trim();
        String tiempoStr = txtTiempoProceso.getText().trim();
        String memoriaStr = txtMemoriaProceso.getText().trim();
        String prioridadStr = txtPrioridadProceso.getText().trim();
        // Selección de usuario (por simplicidad, alternar entre los usuarios de ejemplo)
        Usuario usuario = usuarios.get((procesosUsuario.size()) % usuarios.size());
        if (nombre.isEmpty() || tiempoStr.isEmpty() || memoriaStr.isEmpty() || prioridadStr.isEmpty()) {
            agregarLog("Debes completar todos los campos para agregar un proceso.");
            mostrarToast("Completa todos los campos del formulario.");
            return;
        }
        try {
            int tiempo = Integer.parseInt(tiempoStr);
            int memoria = Integer.parseInt(memoriaStr);
            int prioridad = Integer.parseInt(prioridadStr);
            Proceso nuevo = new Proceso(nextPid++, nombre, tiempo, memoria, prioridad, usuario);
            // Guardar el nombre del archivo en el nombre del proceso si prioridad==2
            String archivoSeleccionado = comboArchivoProceso.getValue();
            if (prioridad == 2 && archivoSeleccionado != null) {
                nuevo.setEstado(Proceso.Estado.NUEVO);
                nuevo.setArchivoDestino(archivoSeleccionado);
            }
            procesosUsuario.add(nuevo);
            agregarLog("Proceso agregado: " + nombre + " (Usuario: " + usuario.getNombre() + ", Tiempo: " + tiempo + ", Memoria: " + memoria + ", Prioridad: " + prioridad + ")");
            mostrarToast("Proceso agregado correctamente.");
            txtNombreProceso.clear();
            txtTiempoProceso.clear();
            txtMemoriaProceso.clear();
            txtPrioridadProceso.clear();
        } catch (NumberFormatException e) {
            agregarLog("Tiempo, memoria y prioridad deben ser números enteros.");
            mostrarToast("Verifica los valores numéricos del formulario.");
        }
    }

    private void simularProcesos() {
        listaProcesos.clear();
        listaBloqueados.clear();
        listaSeguridad.getItems().clear();
        listaProcesosEnEspera.getItems().clear();
        impresora = new Impresora(); // Reinicia la impresora
        archivo = new Archivo(); // Reinicia el archivo
        memoria = new Memoria(512); // Reinicia la memoria simulada
        List<Proceso> procesos;
        if (!procesosUsuario.isEmpty()) {
            procesos = List.copyOf(procesosUsuario);
        } else {
            Usuario admin = new Usuario("admin");
            admin.agregarPermiso("Impresora");
            admin.agregarPermiso("Archivo");
            procesos = List.of(
                new Proceso(1, "Editor", 10, 100, 1, admin),
                new Proceso(2, "Compilador", 15, 80, 2, admin),
                new Proceso(3, "Navegador", 20, 120, 3, admin),
                new Proceso(4, "Terminal", 5, 90, 1, admin),
                new Proceso(5, "Updater", 12, 70, 2, admin),
                new Proceso(6, "Antivirus", 30, 200, 1, admin),
                new Proceso(7, "Backup", 40, 300, 2, admin),
                new Proceso(8, "Juego", 25, 150, 3, admin)
            );
        }
        EstrategiaMemoria estrategia = getEstrategiaSeleccionada();
        // Asignación de memoria y filtrado de bloqueados
        for (Proceso p : procesos) {
            boolean asignado = (estrategia == EstrategiaMemoria.FIRST_FIT)
                    ? memoria.asignarFirstFit(p)
                    : memoria.asignarBestFit(p);
            if (!asignado) {
                registrarAccesoDenegado(p.getNombre() + " no pudo ser cargado en memoria (sin espacio disponible)");
                p.setEstado(Proceso.Estado.BLOQUEADO);
                listaBloqueados.add(p);
                agregarLog("Proceso " + p.getNombre() + " bloqueado por falta de memoria");
                continue;
            }
            // Seguridad: verificar permisos antes de acceder a recursos
            if (p.getPrioridad() == 1) {
                if (!p.getUsuario().tienePermiso("Impresora")) {
                    registrarAccesoDenegado("Usuario '" + p.getUsuario().getNombre() + "' no tiene permiso para la impresora (" + p.getNombre() + ")");
                    p.setEstado(Proceso.Estado.BLOQUEADO);
                    listaBloqueados.add(p);
                    agregarLog("[SEGURIDAD] Acceso denegado a impresora para " + p.getNombre());
                    continue;
                }
                boolean acceso = impresora.solicitar(p);
                if (acceso) {
                    agregarLog("[IMPRESORA] Proceso " + p.getNombre() + " está usando la impresora.");
                    p.setEstado(Proceso.Estado.EJECUTANDO);
                } else {
                    agregarLog("[IMPRESORA] Proceso " + p.getNombre() + " esperando la impresora.");
                    p.setEstado(Proceso.Estado.BLOQUEADO);
                    listaBloqueados.add(p);
                    continue;
                }
            } else if (p.getPrioridad() == 2) {
                if (!p.getUsuario().tienePermiso("Archivo")) {
                    registrarAccesoDenegado("Usuario '" + p.getUsuario().getNombre() + "' no tiene permiso para el archivo (" + p.getNombre() + ")");
                    p.setEstado(Proceso.Estado.BLOQUEADO);
                    listaBloqueados.add(p);
                    agregarLog("[SEGURIDAD] Acceso denegado a archivo para " + p.getNombre());
                    continue;
                }
                String archivoDestino = p.getArchivoDestino();
                FileSystem.ArchivoSimulado archivoSim = fileSystem.getArchivo(archivoDestino);
                if (archivoSim == null) {
                    agregarLog("[ARCHIVO] Archivo no encontrado: " + archivoDestino);
                    p.setEstado(Proceso.Estado.BLOQUEADO);
                    listaBloqueados.add(p);
                    continue;
                }
                boolean acceso = archivoSim.solicitar(p);
                if (acceso) {
                    agregarLog("[ARCHIVO] Proceso " + p.getNombre() + " está usando el archivo " + archivoDestino + ".");
                    p.setEstado(Proceso.Estado.EJECUTANDO);
                } else {
                    agregarLog("[ARCHIVO] Proceso " + p.getNombre() + " esperando el archivo " + archivoDestino + ".");
                    p.setEstado(Proceso.Estado.BLOQUEADO);
                    listaBloqueados.add(p);
                    continue;
                }
            } else if (p.getNombre().equalsIgnoreCase("Updater") || p.getNombre().equalsIgnoreCase("Backup")) {
                registrarAccesoDenegado(p.getNombre() + " intentó acceder a un recurso exclusivo.");
                p.setEstado(Proceso.Estado.BLOQUEADO);
                listaBloqueados.add(p);
                agregarLog("Proceso " + p.getNombre() + " bloqueado por acceso restringido");
                continue;
            }
            p.setEstado(Proceso.Estado.LISTO);
            listaProcesos.add(p);
            agregarLog("Proceso " + p.getNombre() + " creado con estado: " + p.getEstado());
        }
        // Simulación: liberar recursos tras uso (solo para demo)
        if (impresora.getProcesoActual() != null) {
            agregarLog("[IMPRESORA] Proceso " + impresora.getProcesoActual().getNombre() + " libera la impresora.");
            impresora.liberar();
        }
        if (archivo.getProcesoActual() != null) {
            agregarLog("[ARCHIVO] Proceso " + archivo.getProcesoActual().getNombre() + " libera el archivo.");
            archivo.liberar();
        }
        actualizarImpresoraUI();
        actualizarArchivoUI();
        actualizarEstadoMemoriaUI();
        actualizarEstadoArchivosUI();
        // Planificación
        String planificadorSel = comboPlanificador.getValue();
        int quantum = 4;
        try {
            quantum = Integer.parseInt(txtQuantum.getText().isEmpty() ? "4" : txtQuantum.getText());
        } catch (NumberFormatException e) {
            agregarLog("Quantum inválido, usando valor por defecto: 4");
        }
        if ("Round Robin".equals(planificadorSel)) {
            simularRoundRobin(listaProcesos, quantum);
        } else {
            simularFIFO(listaProcesos);
        }
        tablaProcesos.setItems(listaProcesos);
        tablaBloqueados.setItems(listaBloqueados);
        tablaProcesos.refresh();
        tablaBloqueados.refresh();
        actualizarResumen(listaProcesos);
        double uso = 1.0 - (memoria.getMemoriaLibre() / 512.0);
        barraMemoria.setProgress(uso);
        if (uso < 0.5) {
            barraMemoria.setStyle("-fx-accent: #2ecc40;"); // verde
        } else if (uso < 0.8) {
            barraMemoria.setStyle("-fx-accent: #ffdc00;"); // amarillo
        } else {
            barraMemoria.setStyle("-fx-accent: #ff4136;"); // rojo
        }
        procesosUsuario.clear(); // Limpia la lista para la próxima simulación
        // Mostrar asignación de memoria en Gantt
        registrarMemoriaGantt(listaProcesos, estrategia);
    }

    private void simularFIFO(List<Proceso> procesos) {
        agregarLog("[FIFO] Ejecutando procesos en orden de llegada...");
        // --- Gantt para FIFO ---
        historiaGantt.clear();
        int tiempoTotal = 0;
        for (Proceso p : procesos) {
            if (p.getEstado() == Proceso.Estado.LISTO) {
                List<String> fila = new java.util.ArrayList<>();
                // Añade 'W' para el tiempo que estuvo esperando
                for (int i = 0; i < tiempoTotal; i++) fila.add("W");
                // Añade 'E' para el tiempo que ejecuta
                for (int i = 0; i < p.getTiempoCpu(); i++) fila.add("E");
                historiaGantt.add(fila);
                tiempoTotal += p.getTiempoCpu();
            } else {
                // Proceso bloqueado o no ejecutado
                historiaGantt.add(new java.util.ArrayList<>());
            }
        }
        // Ejecuta los procesos realmente
        int idx = 0;
        for (Proceso p : procesos) {
            if (p.getEstado() == Proceso.Estado.LISTO) {
                agregarLog("[FIFO] Ejecutando: " + p.getNombre() + " (PID: " + p.getPid() + ")");
                p.setEstado(Proceso.Estado.TERMINADO);
                memoria.liberarMemoria(p); // Libera la memoria al terminar
                agregarLog("[FIFO] Proceso " + p.getNombre() + " terminado y memoria liberada.");
                actualizarResumen(procesos); // Actualiza la lista tras cada cambio
            }
            idx++;
        }
        actualizarEstadoMemoriaUI();
        // Llenar el Gantt
        registrarGantt(procesos, historiaGantt);
    }

    private void limpiarGantt() {
        datosGantt.clear();
        tablaGantt.getColumns().setAll(colGanttProceso);
        columnasTiempoGantt.clear();
    }

    private void registrarGantt(List<Proceso> procesos, List<List<String>> historia) {
        limpiarGantt();
        int ciclos = historia.isEmpty() ? 0 : historia.get(0).size();
        // Crear columnas de tiempo
        for (int i = 0; i < ciclos; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>("T" + (i+1));
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(idx+1)));
            columnasTiempoGantt.add(col);
        }
        tablaGantt.getColumns().addAll(columnasTiempoGantt);
        // Llenar filas
        for (int p = 0; p < procesos.size(); p++) {
            ObservableList<String> fila = FXCollections.observableArrayList();
            fila.add(procesos.get(p).getNombre());
            if (!historia.isEmpty()) fila.addAll(historia.get(p));
            datosGantt.add(fila);
        }
        tablaGantt.setItems(datosGantt);
    }

    private void simularRoundRobin(List<Proceso> procesos, int quantum) {
        agregarLog("[Round Robin] Ejecutando procesos con quantum=" + quantum + "...");
        List<Proceso> cola = procesos.stream().filter(p -> p.getEstado() == Proceso.Estado.LISTO).collect(java.util.stream.Collectors.toList());
        boolean hayPendientes = true;
        int ciclo = 1;
        historiaGantt.clear();
        for (int i = 0; i < cola.size(); i++) historiaGantt.add(new java.util.ArrayList<>());
        while (hayPendientes) {
            hayPendientes = false;
            for (int idx = 0; idx < cola.size(); idx++) {
                Proceso p = cola.get(idx);
                if (p.getEstado() != Proceso.Estado.TERMINADO) {
                    hayPendientes = true;
                    int tiempo = p.getTiempoCpu();
                    int ejecutado = Math.min(quantum, tiempo);
                    agregarLog("[RR][Ciclo " + ciclo + "] Ejecutando: " + p.getNombre() + " (PID: " + p.getPid() + ") por " + ejecutado + " unidades");
                    p.setTiempoRestante(tiempo - ejecutado);
                    // Registrar en Gantt
                    for (int q = 0; q < ejecutado; q++) historiaGantt.get(idx).add("E");
                    if (p.getTiempoCpu() <= 0) {
                        p.setEstado(Proceso.Estado.TERMINADO);
                        memoria.liberarMemoria(p);
                        agregarLog("[RR] Proceso " + p.getNombre() + " terminado y memoria liberada.");
                    }
                    // Los otros procesos en espera
                    for (int j = 0; j < cola.size(); j++) {
                        if (j != idx && cola.get(j).getEstado() != Proceso.Estado.TERMINADO) {
                            for (int q = 0; q < ejecutado; q++) historiaGantt.get(j).add("W");
                        }
                    }
                    actualizarResumen(procesos);
                }
            }
            ciclo++;
        }
        actualizarEstadoMemoriaUI();
        registrarGantt(cola, historiaGantt);
    }
    public void registrarAccesoDenegado(String mensaje) {
        Platform.runLater(() -> listaSeguridad.getItems().add("❌ Acceso denegado: " + mensaje));
    }

    public EstrategiaMemoria getEstrategiaSeleccionada() {
        String seleccion = comboEstrategia.getValue();
        return "Best Fit".equals(seleccion) ? EstrategiaMemoria.BEST_FIT : EstrategiaMemoria.FIRST_FIT;
    }

    public void animarInicio() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), tablaProcesos);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
        FadeTransition fadeMem = new FadeTransition(Duration.millis(800), barraMemoria);
        fadeMem.setFromValue(0.0);
        fadeMem.setToValue(1.0);
        fadeMem.setDelay(Duration.millis(300));
        fadeMem.play();
    }
    private String generarResumenEstadoSistema() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("[Resumen del sistema actual]:\n");
        resumen.append("Procesos activos: ").append(listaProcesos.size()).append("\n");
        long bloqueados = listaBloqueados.stream().filter(p -> p.getEstado() == Proceso.Estado.BLOQUEADO).count();
        resumen.append("Procesos bloqueados: ").append(bloqueados).append("\n");
        resumen.append("Procesos en espera: ").append(listaProcesos.stream().filter(p -> p.getEstado() == Proceso.Estado.LISTO).count()).append("\n");
        resumen.append("Memoria libre: ").append(memoria.getMemoriaLibre()).append(" MB de 512 MB\n");
        resumen.append("Uso de memoria: ").append(100 - (memoria.getMemoriaLibre() * 100 / 512)).append("%\n");
        // Recursos compartidos
        resumen.append("Impresora: ").append(impresora.getProcesoActual() != null ? "Ocupada por " + impresora.getProcesoActual().getNombre() : "Libre").append("\n");
        resumen.append("Archivo: ").append(archivo.getProcesoActual() != null ? "Ocupado por " + archivo.getProcesoActual().getNombre() : "Libre").append("\n");
        return resumen.toString();
    }

    public void agregarLog(String mensaje) {
        Platform.runLater(() -> {
            // Agregar a la consola de logs
            logArea.appendText(mensaje + "\n");
            // También agregar como mensaje de chat (IA)
            mensajesChat.add(new MensajeChat(MensajeChat.Autor.IA, mensaje));
            chatListView.scrollTo(mensajesChat.size() - 1);
        });
    }
    private void procesarPregunta() {
        if (aiHelper == null) {
            mensajesChat.add(new MensajeChat(MensajeChat.Autor.IA, "⚠️ Error: AI no está inicializada."));
            return;
        }
        String pregunta = inputPregunta.getText().trim();
        if (!pregunta.isEmpty()) {
            mensajesChat.add(new MensajeChat(MensajeChat.Autor.USUARIO, pregunta));
            inputPregunta.clear();
            String resumen = generarResumenEstadoSistema();
            String prompt = resumen +
                "\n[Pregunta del usuario]: " + pregunta +
                "\nPor favor, responde de forma clara y educativa para un estudiante: " +
                "1. Indica qué proceso está usando la impresora y cuál el archivo, usando sus nombres. " +
                "2. Lista los procesos bloqueados, con su nombre y el motivo del bloqueo. " +
                "3. Explica brevemente por qué ocurre cada situación.";
            mensajesChat.add(new MensajeChat(MensajeChat.Autor.IA, "Pensando..."));
            new Thread(() -> {
                try {
                    String respuesta = aiHelper.generarRespuesta(prompt);
                    Platform.runLater(() -> {
                        // Reemplaza el "Pensando..." por la respuesta real
                        for (int i = mensajesChat.size() - 1; i >= 0; i--) {
                            if (mensajesChat.get(i).getAutor() == MensajeChat.Autor.IA && mensajesChat.get(i).getTexto().equals("Pensando...")) {
                                mensajesChat.set(i, new MensajeChat(MensajeChat.Autor.IA, respuesta));
                                break;
                            }
                        }
                        chatListView.scrollTo(mensajesChat.size() - 1);
                    });
                } catch (IOException ex) {
                    Platform.runLater(() -> {
                        for (int i = mensajesChat.size() - 1; i >= 0; i--) {
                            if (mensajesChat.get(i).getAutor() == MensajeChat.Autor.IA && mensajesChat.get(i).getTexto().equals("Pensando...")) {
                                mensajesChat.set(i, new MensajeChat(MensajeChat.Autor.IA, "⚠️ Error de conexión: " + ex.getMessage()));
                                break;
                            }
                        }
                        chatListView.scrollTo(mensajesChat.size() - 1);
                    });
                }
            }).start();
            chatListView.scrollTo(mensajesChat.size() - 1);
        }
    }
    public void actualizarResumen(List<Proceso> procesos) {
        // Combina procesos en espera y bloqueados
        List<Proceso> todos = FXCollections.observableArrayList();
        todos.addAll(listaProcesos);
        todos.addAll(listaBloqueados);
        List<String> resumen = todos.stream()
                .filter(p -> p.getEstado() == Proceso.Estado.LISTO || p.getEstado() == Proceso.Estado.BLOQUEADO || p.getEstado() == Proceso.Estado.NUEVO)
                .map(p -> p.getPid() + " - " + p.getNombre() + " (" + p.getEstado() + ")")
                .collect(Collectors.toList());
        Platform.runLater(() -> listaProcesosEnEspera.getItems().setAll(resumen));
    }
    private void actualizarImpresoraUI() {
        Platform.runLater(() -> {
            Proceso actual = impresora.getProcesoActual();
            int valorSemaforo = impresora.getSemaforo().getValue();
            if (actual != null) {
                lblImpresora.setText("Impresora: " + actual.getNombre() + " (PID: " + actual.getPid() + ")");
                lblEstadoSemaforoImpresora.setText("Semáforo: " + valorSemaforo + " (Ocupado)");
            } else {
                lblImpresora.setText("Impresora: Libre");
                lblEstadoSemaforoImpresora.setText("Semáforo: " + valorSemaforo + " (Libre)");
            }
            listaColaImpresora.getItems().setAll(
                impresora.getColaEspera().stream().map(p -> p.getPid() + " - " + p.getNombre()).collect(Collectors.toList())
            );
        });
    }
    private void liberarImpresoraManual() {
        Proceso actual = impresora.getProcesoActual();
        if (actual != null) {
            agregarLog("[IMPRESORA] Proceso " + actual.getNombre() + " libera la impresora (manual).");
        }
        impresora.liberar();
        Proceso nuevo = impresora.getProcesoActual();
        if (nuevo != null) {
            agregarLog("[IMPRESORA] Proceso " + nuevo.getNombre() + " toma la impresora.");
            // Cambia el estado en la lista principal
            nuevo.setEstado(Proceso.Estado.EJECUTANDO);
            // Refresca la tabla para mostrar el cambio
            tablaProcesos.refresh();
        }
        actualizarImpresoraUI();
    }
    private void actualizarArchivoUI() {
        Platform.runLater(() -> {
            Proceso actual = archivo.getProcesoActual();
            int valorSemaforo = archivo.getSemaforo().getValue();
            if (actual != null) {
                lblArchivo.setText("Archivo: " + actual.getNombre() + " (PID: " + actual.getPid() + ")");
                lblEstadoSemaforoArchivo.setText("Semáforo: " + valorSemaforo + " (Ocupado)");
            } else {
                lblArchivo.setText("Archivo: Libre");
                lblEstadoSemaforoArchivo.setText("Semáforo: " + valorSemaforo + " (Libre)");
            }
            listaColaArchivo.getItems().setAll(
                archivo.getColaEspera().stream().map(p -> p.getPid() + " - " + p.getNombre()).collect(Collectors.toList())
            );
        });
    }
    private void liberarArchivoManual() {
        Proceso actual = archivo.getProcesoActual();
        if (actual != null) {
            agregarLog("[ARCHIVO] Proceso " + actual.getNombre() + " libera el archivo (manual).");
        }
        archivo.liberar();
        Proceso nuevo = archivo.getProcesoActual();
        if (nuevo != null) {
            agregarLog("[ARCHIVO] Proceso " + nuevo.getNombre() + " toma el archivo.");
            // Cambia el estado en la lista principal
            nuevo.setEstado(Proceso.Estado.EJECUTANDO);
            // Refresca la tabla para mostrar el cambio
            tablaProcesos.refresh();
        }
        actualizarArchivoUI();
    }
    private void actualizarEstadoMemoriaUI() {
        Platform.runLater(() -> {
            listaBloquesMemoria.getItems().setAll(memoria.getEstadoBloques());
            // Animación de barra de memoria
            FadeTransition ft = new FadeTransition(Duration.millis(700), barraMemoria);
            ft.setFromValue(0.6);
            ft.setToValue(1.0);
            ft.play();
        });
    }
    private void actualizarEstadoArchivosUI() {
        Platform.runLater(() -> {
            listaEstadoArchivos.getItems().setAll(
                fileSystem.getArchivos().stream().map(a -> {
                    String actual = a.getProcesoActual() != null ? "Ocupado por PID " + a.getProcesoActual().getPid() : "Libre";
                    String cola = a.getColaEspera().isEmpty() ? "" : ", Cola: " + a.getColaEspera().stream().map(p -> p.getPid() + "").toList();
                    return a.getNombre() + ": " + actual + cola;
                }).toList()
            );
        });
    }
    private void registrarMemoriaGantt(List<Proceso> procesos, EstrategiaMemoria estrategia) {
        datosMemoriaGantt.clear();
        // Configurar columnas si es necesario
        colMemoriaProceso.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        colMemoriaInicio.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        colMemoriaTamanio.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        colMemoriaAlgoritmo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        // Llenar filas
        for (Proceso p : procesos) {
            // Buscar el bloque asignado a este proceso
            String inicio = "-";
            String tamanio = "-";
            for (var b : memoria.getEstadoBloques()) {
                if (b.contains("Ocupado por PID "+p.getPid())) {
                    // Extraer inicio y tamaño del string
                    String[] partes = b.split(",");
                    if (partes.length >= 2) {
                        inicio = partes[0].replace("Inicio:", "").trim();
                        tamanio = partes[1].replace("Tamaño:", "").trim();
                    }
                }
            }
            ObservableList<String> fila = FXCollections.observableArrayList(
                p.getNombre(), inicio, tamanio, estrategia == EstrategiaMemoria.FIRST_FIT ? "First Fit" : "Best Fit"
            );
            datosMemoriaGantt.add(fila);
        }
        tablaMemoriaGantt.setItems(datosMemoriaGantt);
    }

    private void sugerirProcesosIA() {
        if (aiHelper == null) {
            agregarLog("⚠️ Error: AI no está inicializada.");
            mostrarAlerta("Error", "La IA no está inicializada. Verifica la API KEY.", Alert.AlertType.ERROR);
            return;
        }
        agregarLog("Solicitando sugerencia de procesos a la IA...");
        String archivosDisponibles = String.join(", ", fileSystem.getArchivos().stream().map(a -> a.getNombre()).toList());
        String prompt = "Sugiere 3 procesos de ejemplo para un sistema operativo simulado. Devuelve SOLO en formato JSON, con los campos: nombre, tiempo, memoria, prioridad. La memoria debe ser un número entre 10 y 100. Si prioridad es 2, agrega el campo archivo con uno de estos nombres: [" + archivosDisponibles + "]";
        new Thread(() -> {
            try {
                String respuesta = aiHelper.generarRespuesta(prompt);
                Platform.runLater(() -> {
                    agregarLog("Respuesta IA: " + respuesta);
                    // Intentar parsear el JSON
                    try {
                        int start = respuesta.indexOf('[');
                        int end = respuesta.lastIndexOf(']');
                        if (start == -1 || end == -1) throw new Exception("No se encontró JSON válido en la respuesta");
                        String json = respuesta.substring(start, end+1);
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        java.util.List<java.util.Map<String,Object>> procesos = mapper.readValue(json, java.util.List.class);
                        int count = 0;
                        for (java.util.Map<String,Object> proc : procesos) {
                            String nombre = String.valueOf(proc.get("nombre"));
                            int tiempo = Integer.parseInt(String.valueOf(proc.get("tiempo")));
                            int memoria = Integer.parseInt(String.valueOf(proc.get("memoria")));
                            int prioridad = Integer.parseInt(String.valueOf(proc.get("prioridad")));
                            Usuario usuario = usuarios.get((procesosUsuario.size()) % usuarios.size());
                            // Validar y ajustar memoria sugerida
                            if (memoria < 10) memoria = 10;
                            if (memoria > 100) memoria = 100;
                            Proceso nuevo = new Proceso(nextPid++, nombre, tiempo, memoria, prioridad, usuario);
                            if (prioridad == 2 && proc.containsKey("archivo")) {
                                nuevo.setArchivoDestino(String.valueOf(proc.get("archivo")));
                            }
                            procesosUsuario.add(nuevo);
                            agregarLog("Proceso IA agregado: " + nombre + " (Tiempo: " + tiempo + ", Memoria: " + memoria + ", Prioridad: " + prioridad + ")");
                            count++;
                        }
                        actualizarResumen(procesosUsuario);
                        mostrarAlerta("Procesos IA agregados", "Se agregaron " + count + " procesos sugeridos por la IA.", Alert.AlertType.INFORMATION);
                        // Ejecutar simulación automáticamente y limpiar la lista de procesos sugeridos
                        simularProcesos();
                    } catch (Exception ex) {
                        agregarLog("⚠️ Error al analizar la respuesta de la IA: " + ex.getMessage());
                        mostrarAlerta("Error IA", "No se pudieron analizar los procesos sugeridos.\n" + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    agregarLog("⚠️ Error de conexión: " + ex.getMessage());
                    mostrarAlerta("Error de conexión", "No se pudo conectar con la IA.\n" + ex.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // --- MÉTODOS FALTANTES PARA MODO OSCURO Y TOAST ---
    private void toggleModoOscuro() {
        var scene = btnModoOscuro.getScene();
        if (scene != null) {
            var root = scene.getRoot();
            if (root.getStyleClass().contains("dark-mode")) {
                root.getStyleClass().remove("dark-mode");
            } else {
                root.getStyleClass().add("dark-mode");
            }
        }
    }

    private void mostrarToast(String mensaje) {
        Platform.runLater(() -> {
            Label toast = new Label(mensaje);
            toast.getStyleClass().add("toast");
            var root = btnSimular.getScene().getRoot();
            if (root instanceof javafx.scene.layout.Pane pane) {
                pane.getChildren().add(toast);
                toast.setLayoutX((pane.getWidth() - 300) / 2);
                toast.setLayoutY(40);
                FadeTransition ft = new FadeTransition(Duration.seconds(2), toast);
                ft.setFromValue(0.95);
                ft.setToValue(0);
                ft.setOnFinished(e -> pane.getChildren().remove(toast));
                ft.play();
            }
        });
    }

    public static class MensajeChat {
        public enum Autor { USUARIO, IA }
        private final Autor autor;
        private final String texto;
        public MensajeChat(Autor autor, String texto) {
            this.autor = autor;
            this.texto = texto;
        }
        public Autor getAutor() { return autor; }
        public String getTexto() { return texto; }
    }

}
