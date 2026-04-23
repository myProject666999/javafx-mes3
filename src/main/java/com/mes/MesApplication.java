package com.mes;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MesApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
}
