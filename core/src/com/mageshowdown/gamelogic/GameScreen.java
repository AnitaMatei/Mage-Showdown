package com.mageshowdown.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mageshowdown.gameclient.*;
import com.mageshowdown.utils.PrefsKeys;

import static com.mageshowdown.gameclient.ClientAssetLoader.*;

public class GameScreen implements Screen {
    private static final GameScreen INSTANCE = new GameScreen();
    private static Viewport viewport;
    private static Batch batch;
    private static ScoreboardStage scoreboardStage;
    private static ClientGameStage gameStage;
    private static GameHUDStage hudStage;
    private static OptionsStage gameOptionsStage;
    private static EscMenuStage escMenuStage;
    private static RoundEndStage roundEndStage;
    private static State state;

    private GameScreen() {
    }

    public static void start() {
        viewport = new FitViewport(1920, 1080);
        batch = new SpriteBatch();

        gameStage = new ClientGameStage();
        hudStage = new GameHUDStage(viewport, batch);
        scoreboardStage = new ScoreboardStage(viewport, batch);
        escMenuStage = new EscMenuStage(viewport, batch);
        roundEndStage = new RoundEndStage(viewport, batch);
        gameOptionsStage = new OptionsStage(viewport, batch, INSTANCE);

        state = State.GAME_RUNNING;

        gameplayMusic.setVolume(prefs.getFloat(PrefsKeys.MUSICVOLUME) * 0.5f);
        gameplayMusic.play();
        gameplayMusic.setLooping(true);
    }

    public static GameScreen getInstance() {
        return INSTANCE;
    }

    public static ClientGameStage getGameStage() {
        return gameStage;
    }

    public static EscMenuStage getEscMenuStage() {
        return escMenuStage;
    }

    public static Batch getBatch() {
        return batch;
    }

    public static State getState() {
        return state;
    }

    public static void setState(State state) {
        GameScreen.state = state;
    }

    public static void addActionToScoreboard(Action action) {
        scoreboardStage.addAction(action);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (ClientRound.getInstance().isFinished()) {
            ClientRound.getInstance().act(delta);
            roundEndStage.act();
            gameStage.draw();
            roundEndStage.draw();
        } else {
            batch.enableBlending();
            gameStage.act();
            gameStage.draw();
        }

        switch (state) {
            case GAME_RUNNING:
                hudStage.act();
                hudStage.draw();
                break;
            case GAME_PAUSED:
                escMenuStage.act();
                escMenuStage.draw();
                break;
            case GAME_OPTIONS:
                gameOptionsStage.act();
                gameOptionsStage.draw();
                break;
            case SCOREBOARD:
                scoreboardStage.act();
                scoreboardStage.draw();
                break;
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        gameStage.getViewport().update(width, height, true);
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
        this.dispose();
        gameplayMusic.stop();
    }

    @Override
    public void dispose() {
        gameStage.dispose();
        escMenuStage.dispose();
        gameOptionsStage.dispose();
        scoreboardStage.dispose();
        hudStage.dispose();
        roundEndStage.dispose();
        batch.dispose();
    }

    public enum State {
        GAME_RUNNING, GAME_PAUSED, GAME_OPTIONS, SCOREBOARD
    }

    public static class EscMenuStage extends Stage {
        private Table rootTable;
        private MenuDialog dialog;

        {
            Image background = new Image(menuBackground);
            background.setColor(0, 0, 0, 0.8f);
            this.addActor(background);
            rootTable = new Table();
            rootTable.setFillParent(true);
            //root.debug();
            this.addActor(rootTable);

            TextButton resumeButton = new TextButton("Resume Game", uiSkin);
            TextButton optionsButton = new TextButton("Options...", uiSkin);
            TextButton quitButton = new TextButton("Quit Game", uiSkin);
            TextButton disconnectButton = new TextButton("Disconnect", uiSkin);

            rootTable.defaults().space(20, 25, 20, 25).width(605).height(60).colspan(2);
            rootTable.add(resumeButton);
            rootTable.row();
            rootTable.add(optionsButton);
            rootTable.row();
            rootTable.defaults().width(290).colspan(1);
            rootTable.add(disconnectButton, quitButton);

            resumeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                    state = State.GAME_RUNNING;
                    Gdx.input.setInputProcessor(gameStage.getPlayerCharacter());
                }
            });

            optionsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                    gameOptionsStage.getRootTable().addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.1f)));
                    state = State.GAME_OPTIONS;
                    Gdx.input.setInputProcessor(gameOptionsStage);
                }
            });

            disconnectButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                    GameClient.getInstance().stop();
                    ClientRound.getInstance().stop();
                    MageShowdownClient.getInstance().setScreen(MenuScreen.getInstance());
                    Gdx.input.setInputProcessor(MenuScreen.getMainMenuStage());

                }
            });

            quitButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));

                    dialog = new MenuDialog("Exit to desktop", "Exiting to desktop. Are you sure?",
                            uiSkin, "dialog");
                    dialog.button("Yes", (Runnable) () -> Gdx.app.exit());
                    dialog.button("Cancel");
                    dialog.show(escMenuStage);
                }
            });
        }

        public EscMenuStage(Viewport viewport, Batch batch) {
            super(viewport, batch);
        }

        public Table getRootTable() {
            return rootTable;
        }

        @Override
        public boolean keyDown(int keyCode) {
            if (this.getKeyboardFocus() != null && this.getKeyboardFocus() == dialog) {
                dialog.hide();
                this.setKeyboardFocus(null);
            } else if (keyCode == Input.Keys.ESCAPE) {
                state = State.GAME_RUNNING;
                Gdx.input.setInputProcessor(gameStage.getPlayerCharacter());
            }
            return super.keyDown(keyCode);
        }
    }

    private static class ScoreboardStage extends Stage {
        private Array<ClientPlayerCharacter> sortedPlayers = gameStage.getSortedPlayers();
        private Label timeLeftLabel;
        private Array<String> playerNames = new Array<>();
        private Array<Integer> playerKills = new Array<>();
        private Array<Integer> playerScore = new Array<>();
        private List<String> nameListWidget = new List<>(uiSkin);
        private List<Integer> killsListWidget = new List<>(uiSkin);
        private List<Integer> scoreListWidget = new List<>(uiSkin);

        {
            Table root = new Table(uiSkin);
            root.setBackground("default-window");
            root.setColor(0, 0, 0, 0.8f);
            //root.debug();

            timeLeftLabel = new Label("", uiSkin, "menu-label");
            root.top();
            root.add(new Label("TIME LEFT", uiSkin, "menu-label")).expandX().colspan(3);
            root.row();
            root.add(timeLeftLabel).expandX().colspan(3);
            root.row();
            root.defaults().pad(1, 1, 1, 1).center().fill();
            root.add(new Label("Player Name", uiSkin), new Label("Kills", uiSkin), new Label("Score", uiSkin));
            root.row();
            root.add(nameListWidget, killsListWidget, scoreListWidget);

            Container<Table> wrapper = new Container<>(root);
            wrapper.setFillParent(true);
            int WIDTH = 800;
            int HEIGHT = 400;
            wrapper.width(WIDTH).height(HEIGHT);

            this.addActor(wrapper);
        }

        public ScoreboardStage(Viewport viewport, Batch batch) {
            super(viewport, batch);
        }

        @Override
        public void act() {
            super.act();

            timeLeftLabel.setText((int) (Round.ROUND_LENGTH - ClientRound.getInstance().timePassed));
            playerNames.clear();
            playerKills.clear();
            playerScore.clear();
            sortedPlayers.sort((o1, o2) -> Integer.compare(o2.getScore(), o1.getScore()));
            for (ClientPlayerCharacter player : sortedPlayers) {
                playerNames.add(player.getUserName());
                playerKills.add(player.getKills());
                playerScore.add(player.getScore());
            }
            nameListWidget.setItems(playerNames);
            killsListWidget.setItems(playerKills);
            scoreListWidget.setItems(playerScore);
        }
    }

    private static class GameHUDStage extends Stage {
        private ProgressBar healthOrb;
        private ProgressBar shieldBar;
        private ProgressBar manaBar;
        private Label healthText;
        private Label shieldText;
        private Label manaText;

        private float maxCapacity;
        private float currCapacity;

        {
            Table root = new Table();
            root.setFillParent(true);
            //root.debug();

            TiledDrawable tiledDrawable = hudSkin.getTiledDrawable("health-orb-fill");
            tiledDrawable.setMinHeight(0f);
            hudSkin.get("health-orb", ProgressBar.ProgressBarStyle.class).knobBefore = tiledDrawable;
            healthOrb = new ProgressBar(0f, PlayerCharacter.getMaxHealth(), 1f, true, hudSkin, "health-orb");
            healthOrb.setAnimateDuration(0.1f);
            healthText = new Label("", uiSkin, "menu-label");
            healthText.setAlignment(Align.center);
            Stack healthStack = new Stack();
            healthStack.add(healthOrb);
            healthStack.add(healthText);

            tiledDrawable = hudSkin.getTiledDrawable("progress-bar-mana-v");
            tiledDrawable.setMinHeight(0f);
            hudSkin.get("mana-vertical", ProgressBar.ProgressBarStyle.class).knobBefore = tiledDrawable;
            shieldBar = new ProgressBar(0f, PlayerCharacter.getMaxShield(), 1f, true, hudSkin, "mana-vertical");
            shieldBar.setAnimateDuration(0.1f);
            shieldText = new Label("", uiSkin, "menu-label");
            shieldText.setAlignment(Align.center);
            Stack shieldStack = new Stack();
            shieldStack.add(shieldBar);
            shieldStack.add(shieldText);

            tiledDrawable = hudSkin.getTiledDrawable("progress-bar-mana");
            tiledDrawable.setMinWidth(0f);
            hudSkin.get("mana", ProgressBar.ProgressBarStyle.class).knobBefore = tiledDrawable;
            manaBar = new ProgressBar(0, 25, 1f, false, hudSkin, "mana");
            manaBar.setAnimateDuration(0.1f);
            manaText = new Label("", uiSkin, "menu-label");
            manaText.setAlignment(Align.center);
            Stack manaStack = new Stack();
            manaStack.add(manaBar);
            manaStack.add(manaText);

            root.left().bottom();
            root.add(healthStack).width(201).height(164).left();
            root.add(shieldStack).height(175).padLeft(20).left().expandX();
            root.add(manaStack).width(175);

            this.addActor(root);
        }

        private GameHUDStage(Viewport viewport, Batch batch) {
            super(viewport, batch);
        }

        @Override
        public void act() {
            super.act();
            if (gameStage.getPlayerCharacter() != null) {
                healthOrb.setValue(gameStage.getPlayerCharacter().getHealth());
                healthText.setText((int) gameStage.getPlayerCharacter().getHealth());

                shieldBar.setValue(gameStage.getPlayerCharacter().getEnergyShield());
                shieldText.setText((int) gameStage.getPlayerCharacter().getEnergyShield());

                maxCapacity = gameStage.getPlayerCharacter().getCurrentOrb().getMaxCapacity();
                currCapacity = gameStage.getPlayerCharacter().getCurrentOrb().getCurrentMana();
                manaBar.setRange(0f, maxCapacity);
                manaBar.setValue((int) currCapacity);
                manaText.setText((int) currCapacity + "/" + (int) maxCapacity);
            }
        }
    }

    private static class RoundEndStage extends Stage {
        private Label roundEndLabel;

        {
            Image background = new Image(menuBackground);
            background.setFillParent(true);
            background.setColor(0, 0, 0, 0.8f);

            roundEndLabel = new Label("", uiSkin, "menu-label");
            Container<Label> wrapper = new Container<>(roundEndLabel);
            wrapper.setFillParent(true);

            this.addActor(background);
            this.addActor(wrapper);
        }

        public RoundEndStage(Viewport viewport, Batch batch) {
            super(viewport, batch);
        }

        @Override
        public void act() {
            super.act();
            roundEndLabel.setText("NEW ROUND STARTS IN: " + (int) ClientRound.getInstance().getTimePassedRoundFinished());
        }
    }
}
