package com.mageshowdown.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mageshowdown.gameclient.ClientListener;
import com.mageshowdown.gameclient.GameClient;
import com.mageshowdown.gameclient.MageShowdownClient;
import com.mageshowdown.packets.Network;
import com.mageshowdown.utils.PrefsKeys;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static com.mageshowdown.gameclient.ClientAssetLoader.*;

public class MenuScreen implements Screen {

    private static final MenuScreen INSTANCE = new MenuScreen();
    private static Viewport viewport;
    private static Batch batch;
    private static Stage mainMenuStage;
    private static OptionsStage menuOptionsStage;
    private static CreditsStage creditsStage;
    private static Stage currentStage;
    private static Table root;
    private GameClient myClient = GameClient.getInstance();

    public static MenuScreen getInstance() {
        return INSTANCE;
    }

    public static Stage getMainMenuStage() {
        return mainMenuStage;
    }

    public static Table getRootTable() {
        return root;
    }

    public static Batch getBatch() {
        return batch;
    }

    public static Stage getCurrentStage() {
        return currentStage;
    }

    public static void setCurrentStage(Stage currentStage) {
        MenuScreen.currentStage = currentStage;
    }

    @Override
    public void show() {
        //Show method is called when the current screen becomes this
//        viewport = new FitViewport(1920, 1080);
        viewport = new ScreenViewport();
        ((ScreenViewport) viewport).setUnitsPerPixel(1f);
        batch = new SpriteBatch();

        mainMenuStage = new Stage(viewport, batch);
        prepareMainMenuStage();
        menuOptionsStage = new OptionsStage(viewport, batch, INSTANCE);

        Gdx.input.setInputProcessor(currentStage = mainMenuStage);

        menuMusic.setVolume(prefs.getFloat(PrefsKeys.MUSICVOLUME));
        menuMusic.play();

        /*List<InetAddress> addressList;
        new Thread(() -> *//*addressList = *//*myClient.discoverHosts(Network.UDP_PORT, 5000)).start();*/
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        currentStage.act();
        currentStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        menuMusic.stop();
        //hide method is called when we change to a new screen or before the Application exits
        this.dispose();
    }

    @Override
    public void dispose() {
        mainMenuStage.dispose();
        menuOptionsStage.dispose();
        batch.dispose();
    }

    private void prepareMainMenuStage() {
        Image background = new Image(menuBackground);
        background.setFillParent(true);

        root = new Table();
        root.setFillParent(true);
        //root.debug();

        TextButton connectButton = new TextButton("Connect", uiSkin);
        TextButton optionsButton = new TextButton("Options...", uiSkin);
        TextButton creditsButton = new TextButton("Credits", uiSkin);
        TextButton quitButton = new TextButton("Quit Game", uiSkin);
        final TextField addressField = new TextField(prefs.getString(PrefsKeys.LASTENTEREDIP), uiSkin);

        root.defaults().space(20, 25, 20, 25).width(290).height(60);
        root.add(connectButton, addressField);
        root.row();
        root.defaults().width(605).colspan(2);
        root.add(optionsButton);
        root.row();
        root.defaults().width(290).colspan(1);
        root.add(creditsButton, quitButton);

        mainMenuStage.addActor(background);
        mainMenuStage.addActor(root);

        connectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent even, float x, float y) {
                btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                String ipAddress = addressField.getText();
                prefs.putString(PrefsKeys.LASTENTEREDIP, ipAddress).flush();
                startClient(ipAddress);
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                // Set alpha to 0 and then add fade in effect action
                menuOptionsStage.getRootTable().addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.1f)));
                Gdx.input.setInputProcessor(currentStage = menuOptionsStage);
            }
        });

        creditsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                creditsStage = new CreditsStage(viewport, batch);
                Gdx.input.setInputProcessor(currentStage = creditsStage);
            }
        });

        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                Gdx.app.exit();
            }
        });
    }

    private void startClient(String ipAddress) {
        try {
            myClient.setUserName(prefs.getString(PrefsKeys.PLAYERNAME));
            myClient.start();
            //GameScreen stages need to be initialized before connect, including music, else black screen....
            //Fucking Matei wtf. Like wtf why does music initialized after connect result in black screen!?!?!

            GameScreen.start();
            myClient.connect(5000, ipAddress, Network.TCP_PORT, Network.UDP_PORT);
            MageShowdownClient.getInstance().setScreen(GameScreen.getInstance());

            myClient.addListener(new ClientListener());
        } catch (IOException e) {
            gameplayMusic.stop();

            MenuDialog dialog = new MenuDialog("Connection Error", e.toString(), uiSkin, "dialog");
            dialog.button("Back");
            dialog.key(Input.Keys.ESCAPE, null);
            dialog.show(mainMenuStage);
        }
    }

    private static class CreditsStage extends Stage {
        private final float duration = 3f;
        private boolean isFirstLine, isNearEnd;
        private Table scrolledTable;

        {
            scrolledTable = new Table();

            ScrollPane scrollPane = new ScrollPane(scrolledTable, uiSkin, "transparent");
            scrollPane.setFillParent(true);
            scrollPane.clearListeners();
            scrollPane.setSmoothScrolling(true);
            this.addActor(scrollPane);
//            scrollPane.debugAll();

            isFirstLine = true;
            isNearEnd = false;
            try {
                fillScrolledTable();
            } catch (IOException e) {
                e.printStackTrace();
            }

            TemporalAction scrollAction = new TemporalAction() {
                @Override
                protected void update(float percent) {
                    if (actor instanceof ScrollPane) {
                        ((ScrollPane) actor).layout();
                        ((ScrollPane) actor).setScrollPercentY(percent);
                        if (this.getDuration() - this.getTime() <= duration && !isNearEnd) {
                            scrollPane.addAction(
                                    Actions.sequence(Actions.fadeOut(duration), Actions.run(() -> returnToMenu()))
                            );
                            isNearEnd = true;
                        }
                    }
                }

            };
            scrollAction.setDuration(15);
            scrollPane.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration), scrollAction));
        }

        public CreditsStage(Viewport viewport, Batch batch) {
            super(viewport, batch);
        }

        private void fillScrolledTable() throws IOException {
            /*We read the credits file line by line and put each one in a label. All the labels are added to a table,
             which is scrolled by a scrollpane*/

            Scanner scanner = new Scanner(new File("UIAssets/credits.txt"));
            int emptyLinesNr = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Label label;
                if (isFirstLine) {
                    // We do this because of bugged char in the font file
                    isFirstLine = false;
                    continue;
                }
                if (line.startsWith("# ")) {
                    label = new Label(line.replace("# ", ""),
                            new Label.LabelStyle(hugeSizeFont, Color.YELLOW));
                    scrolledTable.add(label).padBottom(Gdx.graphics.getHeight() * 0.25f).padTop(Gdx.graphics.getHeight() * 0.5f);
                } else if (line.startsWith("## ")) {
                    label = new Label(line.replace("## ", ""),
                            new Label.LabelStyle(bigSizeFont, Color.GRAY));
                    scrolledTable.add(label).left().padTop(emptyLinesNr * label.getPrefHeight());
                    emptyLinesNr = 0;
                } else if (line.isEmpty()) emptyLinesNr++;
                else {
                    label = new Label(line, uiSkin, "menu-label");
                    scrolledTable.add(label);
                }
                scrolledTable.row();
            }
            scanner.close();
            scrolledTable.add(new Image(libgdxLogo)).padBottom(40).padTop(40);
            scrolledTable.row();
            scrolledTable.add(new Image(kryonetLogo)).padBottom(Gdx.graphics.getHeight() / 1.25f);
        }

        private void returnToMenu() {
            Gdx.input.setInputProcessor(currentStage = mainMenuStage);
            this.dispose();
        }

        @Override
        public boolean keyDown(int keyCode) {
            if (keyCode == Input.Keys.ESCAPE)
                returnToMenu();
            return true;
        }
    }
}