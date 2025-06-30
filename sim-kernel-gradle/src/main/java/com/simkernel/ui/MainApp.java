package com.simkernel.ui;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        System.err.println(">> [DEBUG] Iniciando método start de MainApp");
        try {
            var introUrl = getClass().getResource("/fxml/intro.fxml");
            if (introUrl == null) {
                System.err.println("[ERROR] No se encontró el archivo intro.fxml en /fxml/");
                System.err.println("Asegúrate de que el archivo src/main/resources/fxml/intro.fxml existe y está bien escrito.");
                System.exit(1);
            }
            FXMLLoader introLoader = new FXMLLoader(introUrl);
            Parent introRoot;
            try {
                introRoot = introLoader.load();
            } catch (Exception e) {
                System.err.println("[ERROR] Excepción al cargar intro.fxml:");
                e.printStackTrace();
                System.exit(1);
                return;
            }

            IntroController controller = introLoader.getController();
            if (controller == null) {
                System.err.println("[ERROR] No se pudo obtener el controlador IntroController. Verifica fx:controller en intro.fxml.");
                System.exit(1);
            }
            controller.setStage(stage);

            Scene introScene = new Scene(introRoot);
            stage.setTitle("Booting Sim-Kernel...");
            stage.setScene(introScene);
            stage.show();

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                try {
                    var mainUrl = getClass().getResource("/fxml/sim_view.fxml");
                    if (mainUrl == null) {
                        System.err.println("[ERROR] No se encontró el archivo sim_view.fxml en /fxml/");
                        System.err.println("Asegúrate de que el archivo src/main/resources/fxml/sim_view.fxml existe y está bien escrito.");
                        System.exit(1);
                    }
                    FXMLLoader mainLoader = new FXMLLoader(mainUrl);
                    Parent mainRoot;
                    try {
                        mainRoot = mainLoader.load();
                    } catch (Exception e) {
                        System.err.println("[ERROR] Excepción al cargar sim_view.fxml:");
                        e.printStackTrace();
                        System.exit(1);
                        return;
                    }

                    Scene mainScene = new Scene(mainRoot);
                    var cssUrl = getClass().getResource("/styles/styles.css");
                    if (cssUrl != null) {
                        mainScene.getStylesheets().add(cssUrl.toExternalForm());
                    } else {
                        System.err.println("Advertencia: No se encontró styles.css en /styles/");
                    }

                    stage.setScene(mainScene);
                    stage.setTitle("Sim-Kernel: Sistema Operativo Simulado");
                    stage.show();
                } catch (Exception e) {
                    System.err.println("[ERROR] Error inesperado al cargar sim_view.fxml:");
                    e.printStackTrace();
                    System.exit(1);
                }
            });
            pause.play();
        } catch (Exception e) {
            System.err.println("[ERROR] Error inesperado al cargar intro.fxml o al inicializar la aplicación:");
            e.printStackTrace();
            System.err.println("Sugerencia: Ejecuta con --stacktrace para más detalles.");
            System.exit(1);
        }
        System.err.println(">> [DEBUG] Finalizó método start de MainApp");
    }

    public static void main(String[] args) {
        try {
            System.err.println(">> [DEBUG] Lanzando aplicación JavaFX");
            launch(args);
        } catch (Throwable t) {
            System.err.println("[ERROR] Error crítico al iniciar la aplicación:");
            t.printStackTrace();
            System.exit(1);
        }
    }
}
