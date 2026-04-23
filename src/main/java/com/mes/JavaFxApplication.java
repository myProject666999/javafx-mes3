package com.mes;

import com.mes.view.StageManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(MesApplication.class).run();
    }

    @Override
    public void start(Stage stage) {
        applicationContext.publishEvent(new StageReadyEvent(stage));
        StageManager stageManager = applicationContext.getBean(StageManager.class);
        stageManager.showLogin();
    }

    @Override
    public void stop() {
        applicationContext.close();
    }

    public static class StageReadyEvent {
        private final Stage stage;

        public StageReadyEvent(Stage stage) {
            this.stage = stage;
        }

        public Stage getStage() {
            return stage;
        }
    }
}
