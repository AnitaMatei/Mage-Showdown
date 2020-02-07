package com.mageshowdown.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mageshowdown.gameclient.ClientAssetLoader;
import com.mageshowdown.gameclient.ClientListener;
import com.mageshowdown.gameclient.GameClient;
import com.mageshowdown.gameclient.MageShowdownClient;
import com.mageshowdown.packets.Network;
import com.mageshowdown.utils.PrefsKeys;

import java.io.*;
import java.util.Scanner;

import static com.mageshowdown.gameclient.ClientAssetLoader.prefs;

public class MenuScreen implements Screen {
    //Singleton instantiation
    private static final MenuScreen INSTANCE = new MenuScreen();
    private static Viewport viewport;
    private static Batch batch;
    private static Stage mainMenuStage;
    private static OptionsStage menuOptionsStage;
    private static Stage creditsStage;
    private static Table root;
    private static StagePhase stagePhase;
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

    public static void setStagePhase(StagePhase stagePhase) {
        MenuScreen.stagePhase = stagePhase;
    }

    @Override
    public void show() {
        //Show method is called when the current screen becomes this
//        viewport = new FitViewport(1920, 1080);
        viewport = new ScreenViewport();
//        viewport.setUnitsPerPixel(0.75f);

        batch = new SpriteBatch();

        mainMenuStage = new Stage(viewport, batch);
        prepareMainMenuStage();

        stagePhase = StagePhase.MAIN_MENU_STAGE;
        Gdx.input.setInputProcessor(mainMenuStage);
        ClientAssetLoader.menuMusic.setVolume(prefs.getFloat(PrefsKeys.MUSICVOLUME));
        ClientAssetLoader.menuMusic.play();

        //List<InetAddress> addressList;
        new Thread(() -> /*addressList = */myClient.discoverHosts(Network.UDP_PORT, 5000)).start();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        switch (stagePhase) {
            case MAIN_MENU_STAGE:
                mainMenuStage.act();
                mainMenuStage.draw();
                break;
            case OPTIONS_STAGE:
                menuOptionsStage.act();
                menuOptionsStage.draw();
                break;
            case CREDITS_STAGE:
                creditsStage.act();
                creditsStage.draw();
                break;
        }
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
        ClientAssetLoader.menuMusic.stop();
        //hide method is called when we change to a new screen or before the Application exits
        this.dispose();
    }

    @Override
    public void dispose() {
        mainMenuStage.dispose();
        batch.dispose();
    }

    private void prepareMainMenuStage() {
        Image background = new Image(ClientAssetLoader.menuBackground);
        background.setFillParent(true);

        root = new Table();
        root.setFillParent(true);
        //root.debug();

        TextButton connectButton = new TextButton("Connect", ClientAssetLoader.uiSkin);
        TextButton optionsButton = new TextButton("Options...", ClientAssetLoader.uiSkin);
        TextButton creditsButton = new TextButton("Credits", ClientAssetLoader.uiSkin);
        TextButton quitButton = new TextButton("Quit Game", ClientAssetLoader.uiSkin);
        final TextField addressField = new TextField(prefs.getString(PrefsKeys.LASTENTEREDIP), ClientAssetLoader.uiSkin);

        root.defaults().space(20, 25, 20, 25).width(290).height(60);
        root.add(connectButton);
        root.add(addressField);
        root.row();
        root.defaults().width(605).colspan(2);
        root.add(optionsButton);
        root.row();
        root.defaults().width(290).colspan(1);
        root.add(creditsButton);
        root.add(quitButton);

        connectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent even, float x, float y) {
                ClientAssetLoader.btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                if (GameWorld.WORLD.getBodyCount() == 0) {
                    String ipAddress = addressField.getText();
                    prefs.putString(PrefsKeys.LASTENTEREDIP, ipAddress).flush();
                    clientStart(ipAddress);
                }
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ClientAssetLoader.btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                menuOptionsStage = new OptionsStage(viewport, batch, ClientAssetLoader.menuBackground);
                // Set alpha to 0 and then add fade in effect action
                menuOptionsStage.getRootTable().getColor().a = 0f;
                menuOptionsStage.getRootTable().addAction(Actions.fadeIn(0.1f));
                stagePhase = StagePhase.OPTIONS_STAGE;
                Gdx.input.setInputProcessor(menuOptionsStage);
            }
        });

        creditsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ClientAssetLoader.btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                creditsStage = new Stage(viewport, batch);
                prepareCreditsStage();
                stagePhase = StagePhase.CREDITS_STAGE;
                Gdx.input.setInputProcessor(creditsStage);
            }
        });

        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ClientAssetLoader.btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                Gdx.app.exit();
            }
        });

        mainMenuStage.addActor(background);
        mainMenuStage.addActor(root);
    }

    private void prepareCreditsStage() {
        Table lTable = new Table();
//        lTable.debug();
        boolean firstLine = true;
        try {
            Scanner scanner = new Scanner(new File("UIAssets/credits.txt"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line != null) {
                    if (firstLine) {
                        lTable.add(new Label("Credits", ClientAssetLoader.uiSkin, "menu-label"));
                        firstLine = false;
                    } else
                        lTable.add(new Label(line, ClientAssetLoader.uiSkin, "menu-label"));
                }
                lTable.row();
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ScrollPane scrollPane = new ScrollPane(lTable, ClientAssetLoader.uiSkin, "transp-scrollpane");
        TextButton backBtn = new TextButton("Back", ClientAssetLoader.uiSkin);

        Table rootTable = new Table();
        rootTable.setFillParent(true);
//        rootTable.debug();
//        rootTable.defaults().space(20, 25, 20, 25).width(290).height(60);
        rootTable.add(scrollPane).expand().width(lTable.getPrefWidth() + 40);
        rootTable.row();
        rootTable.defaults().space(20, 25, 20, 25).width(200).height(60);
        rootTable.add(backBtn).expandY();

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ClientAssetLoader.btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                stagePhase = StagePhase.MAIN_MENU_STAGE;
                Gdx.input.setInputProcessor(mainMenuStage);
                creditsStage.dispose();
            }
        });

//        creditsStage.addActor(new Image(ClientAssetLoader.menuBackground));
        creditsStage.addActor(rootTable);
    }

    private void clientStart(String ipAddress) {
        try {
            myClient.setUserName(prefs.getString(PrefsKeys.PLAYERNAME));
            myClient.start();
            GameScreen.start();
            GameScreen.setGameState(GameScreen.GameState.GAME_RUNNING);
            myClient.connect(5000, ipAddress, Network.TCP_PORT, Network.UDP_PORT);
            MageShowdownClient.getInstance().setScreen(GameScreen.getInstance());

        } catch (IOException e) {
            ClientAssetLoader.gameplayMusic.stop();

            final Dialog dialog = new Dialog("", ClientAssetLoader.uiSkin);
            dialog.text(e.toString(), ClientAssetLoader.uiSkin.get("menu-label", Label.LabelStyle.class));
            Button backBtn = new TextButton("Back", ClientAssetLoader.uiSkin);
            backBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    ClientAssetLoader.btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                    dialog.hide();
                }
            });
            dialog.button(backBtn);
            dialog.setMovable(false);
            dialog.show(mainMenuStage);
        }
        myClient.addListener(new ClientListener());
    }

    public enum StagePhase {
        MAIN_MENU_STAGE,
        OPTIONS_STAGE,
        CREDITS_STAGE
    }
}