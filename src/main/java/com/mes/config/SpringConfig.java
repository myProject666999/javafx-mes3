package com.mes.config;

import com.mes.JavaFxApplication;
import com.mes.view.StageManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class SpringConfig {

    private StageManager stageManager;

    public SpringConfig(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @EventListener
    public void onStageReady(JavaFxApplication.StageReadyEvent event) {
        stageManager.setPrimaryStage(event.getStage());
    }
}
