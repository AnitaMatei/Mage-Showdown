package com.mageshowdown.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mageshowdown.gameclient.MageShowdownClient;
import com.mageshowdown.utils.PrefsKeys;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static com.mageshowdown.gameclient.ClientAssetLoader.prefs;

public class MainSceneController implements Initializable {

    @FXML
    private Button playButton;
    @FXML
    private Button configButton;
    @FXML
    private Button exitButton;

    @FXML
    void playBtnClicked(ActionEvent actionEvent) {
        boolean isFullscreen = prefs.getBoolean(PrefsKeys.FULLSCREEN);
        int width = prefs.getInteger(PrefsKeys.WIDTH);
        int height = prefs.getInteger(PrefsKeys.HEIGHT);
        int rate = prefs.getInteger(PrefsKeys.REFRESHRATE);

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = width;
        config.height = height;
        config.fullscreen = isFullscreen;
        config.resizable = false;
        config.foregroundFPS = prefs.getInteger(PrefsKeys.FOREGROUNDFPS);
        config.backgroundFPS = prefs.getInteger(PrefsKeys.BACKGROUNDFPS);
        config.vSyncEnabled = prefs.getBoolean(PrefsKeys.VSYNC);
        config.useGL30 = prefs.getBoolean(PrefsKeys.USEGL30);
        config.samples = 4;
        config.title = "Mage Showdown";
        config.addIcon("icon32.png", Files.FileType.Internal);
        config.addIcon("icon64.png", Files.FileType.Internal);

        //System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");

        new LwjglApplication(MageShowdownClient.getInstance(), config);
        DesktopClientLauncher.mainStage.close();

        /*
         *After the game is launched, we set the display mode using the runtime graphics class for the correct refresh
         * rate, dute to conifguration class' lack of refresh rate field
         */
        if (isFullscreen)
            for (Graphics.DisplayMode mode : Gdx.graphics.getDisplayModes()) {
                if (mode.width == width && mode.height == height && mode.refreshRate == rate) {
                    Gdx.graphics.setFullscreenMode(mode);
                    break;
                }
            }
        else Gdx.graphics.setWindowedMode(width, height);
    }

    @FXML
    void configBtnClicked(ActionEvent actionEvent) throws IOException {
        DesktopClientLauncher.mainStage.setScene(new Scene((Parent) FXMLLoader.load(new URL("file", "localhost",
                "../../core/assets/resources/ConfigScene.fxml"))));
    }

    @FXML
    void exitBtnClicked(ActionEvent actionEvent) {
        DesktopClientLauncher.mainStage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
