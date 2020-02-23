package com.mageshowdown.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import static com.mageshowdown.gameclient.ClientAssetLoader.prefs;

public class DesktopClientLauncher extends Application {

    static Stage mainStage;
    static Scene mainScene;

    public static void main(String[] args) {
        launch(args);
    }

    //Launcher entry point
    @Override
    public void start(Stage primaryStage) throws IOException {
        prefs = new GamePreferences("config.xml");
        mainStage = primaryStage;
        mainStage.setTitle("Mage Showdown Launcher");
        mainStage.setResizable(false);

        mainScene = new Scene((Parent) FXMLLoader.load(new URL("file", "localhost",
                "../../core/assets/resources/MainScene.fxml")));

        mainStage.setScene(mainScene);
        mainStage.show();
    }
}
