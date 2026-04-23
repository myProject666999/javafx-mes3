package com.mes.view;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageManager {

    private final ApplicationContext applicationContext;
    private Stage primaryStage;
    private double xOffset = 0;
    private double yOffset = 0;

    public StageManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private FXMLLoader getLoader(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(applicationContext::getBean);
        return loader;
    }

    public void showLogin() {
        Platform.runLater(() -> {
            try {
                javafx.application.Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

                FXMLLoader loader = getLoader("/fxml/login.fxml");
                Parent root = loader.load();

                Scene scene = new Scene(root, 500, 400);

                root.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                root.setOnMouseDragged(event -> {
                    primaryStage.setX(event.getScreenX() - xOffset);
                    primaryStage.setY(event.getScreenY() - yOffset);
                });

                Stage loginStage = new Stage();
                loginStage.initStyle(StageStyle.UNDECORATED);
                loginStage.setScene(scene);
                loginStage.setTitle("MES System - Login");
                loginStage.setResizable(false);
                
                primaryStage.close();
                primaryStage = loginStage;
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void showMain() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = getLoader("/fxml/main.fxml");
                Parent root = loader.load();

                Scene scene = new Scene(root, 1280, 800);

                root.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                root.setOnMouseDragged(event -> {
                    if (!isMaximized) {
                        primaryStage.setX(event.getScreenX() - xOffset);
                        primaryStage.setY(event.getScreenY() - yOffset);
                    }
                });

                primaryStage.setScene(scene);
                primaryStage.setTitle("MES System");
                primaryStage.setResizable(true);
                
                savedWidth = 1280;
                savedHeight = 800;
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                savedX = (bounds.getWidth() - savedWidth) / 2;
                savedY = (bounds.getHeight() - savedHeight) / 2;
                
                primaryStage.centerOnScreen();
                isMaximized = false;
                
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void close() {
        primaryStage.close();
    }

    public void minimize() {
        primaryStage.setIconified(true);
    }

    private boolean isMaximized = false;
    private double savedX, savedY, savedWidth, savedHeight;

    public void maximize() {
        if (isMaximized) {
            primaryStage.setX(savedX);
            primaryStage.setY(savedY);
            primaryStage.setWidth(savedWidth);
            primaryStage.setHeight(savedHeight);
            isMaximized = false;
        } else {
            savedX = primaryStage.getX();
            savedY = primaryStage.getY();
            savedWidth = primaryStage.getWidth();
            savedHeight = primaryStage.getHeight();
            
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());
            isMaximized = true;
        }
    }
}
