package com.mageshowdown.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mageshowdown.gameclient.MageShowdownClient;
import com.mageshowdown.utils.PrefsKeys;

import java.util.TreeSet;
import java.util.stream.Stream;

import static com.mageshowdown.gameclient.ClientAssetLoader.*;

public class OptionsStage extends Stage {

    private final MageShowdownClient game = MageShowdownClient.getInstance();
    private final Screen parentScreen;
    private final Graphics.DisplayMode[] displayModes;
    private final int samples = 22100;
    private final short[] micData = new short[samples * 5];
    private Table root;
    private MenuDialog discDialog;
    private TextField playerNameField;
    private TextButton vsyncCheckBox, showFPSCheckBox,
            backButton, applyButton, testMic, playMic;
    private SelectBox<String> resSelectBox, modeSelectBox;
    private SelectBox<Integer> refreshSelectBox;
    private Slider soundVolumeSlider;
    private Slider musicVolumeSlider;
    private Label[] labels;
    private boolean isAnyChange = false;
    private AudioRecorder audioRecorder;
    private AudioDevice audioDevice;

    public OptionsStage(Viewport viewport, Batch batch, Screen parentScreen) {
        super(viewport, batch);

        this.parentScreen = parentScreen;
        Image background = new Image(menuBackground);
        background.setFillParent(true);
        if (parentScreen instanceof GameScreen) background.setColor(0, 0, 0, 0.8f);

        displayModes = Gdx.graphics.getDisplayModes();

        setupLayoutView();
        createDialog();
        setupWidgetData();
        handleWidgetEvents();

        this.addActor(background);
        this.addActor(root);
    }

    public Table getRootTable() {
        return root;
    }

    private void setupLayoutView() {
        root = new Table();
        root.setFillParent(true);
        Table contextTable = new Table();
        ScrollPane contScroll = new ScrollPane(contextTable, uiSkin, "transparent");
        contScroll.setFadeScrollBars(false);

        labels = new Label[7];
        labels[0] = new Label("Options Menu", uiSkin, "menu-label");
        labels[1] = new Label("Resolution", uiSkin, "menu-label");
        labels[2] = new Label("Refresh Rate", uiSkin, "menu-label");
        labels[3] = new Label("Display Mode", uiSkin, "menu-label");
        labels[4] = new Label("Player name", uiSkin, "menu-label");
        labels[5] = new Label("Sound", uiSkin, "menu-label");
        labels[6] = new Label("Music", uiSkin, "menu-label");

        resSelectBox = new SelectBox<>(uiSkin);
        modeSelectBox = new SelectBox<>(uiSkin);
        refreshSelectBox = new SelectBox<>(uiSkin);
        vsyncCheckBox = new TextButton("", uiSkin);
        showFPSCheckBox = new TextButton("", uiSkin);
        playerNameField = new TextField("", uiSkin);
        playerNameField.setMessageText("Enter your name...");
        soundVolumeSlider = new Slider(0f, 1f, 0.05f, false, uiSkin);
        musicVolumeSlider = new Slider(0f, 1f, 0.05f, false, uiSkin);
        testMic = new TextButton("Record microphone", uiSkin);
        playMic = new TextButton("Play Recording", uiSkin);

        Stream.of(labels).forEach(label -> label.setAlignment(Align.center));
        Stream.of(resSelectBox, modeSelectBox, refreshSelectBox).forEach(selectBox -> {
            selectBox.setAlignment(Align.center);
            selectBox.getList().setAlignment(Align.center);
        });

        backButton = new TextButton("Back", uiSkin);
        applyButton = new TextButton("Apply", uiSkin);
        applyButton.setVisible(false);

        //Here we position the widgets in the context table
        contextTable.defaults().space(20, 25, 20, 25).width(350).height(60);
        contextTable.row();
        contextTable.add(labels[1], resSelectBox);
        contextTable.row();
        contextTable.add(labels[2], refreshSelectBox);
        contextTable.row();
        contextTable.add(labels[3], modeSelectBox);
        contextTable.row();
        contextTable.add(labels[4], playerNameField);
        contextTable.row();
        contextTable.add(vsyncCheckBox, showFPSCheckBox);
        contextTable.row();
        contextTable.add(labels[5], soundVolumeSlider);
        contextTable.row();
        contextTable.add(labels[6], musicVolumeSlider);
        contextTable.row();
        contextTable.add(testMic, playMic);

        Table bottomTable = new Table();
        //And here we position the back and apply buttons in the bottom table
        bottomTable.defaults().space(0, 25, 0, 25).width(200).height(60);
        bottomTable.add(backButton, applyButton);

        //Finally, here we position the scrollpane and bottomtable in the root one
        root.add(labels[0]).pad(20, 0, 20, 0);
        root.row();
        root.add(contScroll).expand().width(contScroll.getPrefWidth() + 50);
        root.row();
        root.add(bottomTable).bottom().left().pad(20, 20, 20, 20);
    }

    private void setupWidgetData() {
        TreeSet<String> resSet = new TreeSet<>();
        TreeSet<Integer> refreshSet = new TreeSet<>();
        Stream.of(displayModes)/*.filter(displayMode -> {
            float aspectNum = ((float) displayMode.width / (float) displayMode.height) * 9f;
            return displayMode.width >= 1280 && displayMode.height >= 720 && aspectNum >= 15.9f && aspectNum <= 16.1f;
        })*/.forEach(displayMode -> {
            resSet.add(displayMode.width + "x" + displayMode.height);
            refreshSet.add(displayMode.refreshRate);
        });

        resSelectBox.setItems(resSet.toArray(new String[0]));
        modeSelectBox.setItems("Fullscreen", "Windowed");
        refreshSelectBox.setItems(refreshSet.toArray(new Integer[0]));

        soundVolumeSlider.setValue(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
        labels[5].setText("Sound: " + (int) (soundVolumeSlider.getPercent() * 100) + "%");
        musicVolumeSlider.setValue(prefs.getFloat(PrefsKeys.MUSICVOLUME));
        labels[6].setText("Music: " + (int) (musicVolumeSlider.getPercent() * 100) + "%");

        setSelectedFromPrefs();
    }

    private void setSelectedFromPrefs() {
        resSelectBox.setSelected(prefs.getInteger(PrefsKeys.WIDTH) + "x" + prefs.getInteger(PrefsKeys.HEIGHT));
        if (prefs.getBoolean(PrefsKeys.FULLSCREEN))
            modeSelectBox.setSelected("Fullscreen");
        else
            modeSelectBox.setSelected("Windowed");
        refreshSelectBox.setSelected(prefs.getInteger(PrefsKeys.REFRESHRATE));
        playerNameField.setText(prefs.getString(PrefsKeys.PLAYERNAME));
        if (prefs.getBoolean(PrefsKeys.VSYNC))
            vsyncCheckBox.setText("VSync: ON");
        else
            vsyncCheckBox.setText("VSync: OFF");
        vsyncCheckBox.setChecked(prefs.getBoolean(PrefsKeys.VSYNC));
        if (prefs.getBoolean(PrefsKeys.SHOWFPS))
            showFPSCheckBox.setText("Show FPS: ON");
        else
            showFPSCheckBox.setText("Show FPS: OFF");
        showFPSCheckBox.setChecked(prefs.getBoolean(PrefsKeys.SHOWFPS));
    }

    private void handleWidgetEvents() {
        // ChangeListener catches events including progtamatic ones, not only inputs from mouse
        // ClickListener is more precise
        resSelectBox.getList().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                applyButton.setVisible(isAnyChange = true);
            }
        });

        refreshSelectBox.getList().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                applyButton.setVisible(isAnyChange = true);
            }
        });

        modeSelectBox.getList().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                applyButton.setVisible(isAnyChange = true);
            }
        });

        playerNameField.setProgrammaticChangeEvents(false);
        playerNameField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                applyButton.setVisible(isAnyChange = true);
            }
        });

        vsyncCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                applyButton.setVisible(isAnyChange = true);
                if (vsyncCheckBox.isChecked())
                    vsyncCheckBox.setText("VSync: ON");
                else
                    vsyncCheckBox.setText("VSync: OFF");
            }
        });

        showFPSCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                applyButton.setVisible(isAnyChange = true);
                if (showFPSCheckBox.isChecked())
                    showFPSCheckBox.setText("Show FPS: ON");
                else
                    showFPSCheckBox.setText("Show FPS: OFF");
            }
        });

        soundVolumeSlider.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                prefs.putFloat(PrefsKeys.SOUNDVOLUME, soundVolumeSlider.getValue());
                prefs.flush();
                btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                labels[5].setText("Sound: " + (int) (soundVolumeSlider.getPercent() * 100) + "%");
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                labels[5].setText("Sound: " + (int) (soundVolumeSlider.getPercent() * 100) + "%");
            }
        });

        musicVolumeSlider.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                prefs.putFloat(PrefsKeys.MUSICVOLUME, musicVolumeSlider.getValue());
                prefs.flush();
                gameplayMusic.setVolume(prefs.getFloat(PrefsKeys.MUSICVOLUME) * 0.5f);
                menuMusic.setVolume(prefs.getFloat(PrefsKeys.MUSICVOLUME));
                labels[6].setText("Music: " + (int) (musicVolumeSlider.getPercent() * 100) + "%");
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                labels[6].setText("Music: " + (int) (musicVolumeSlider.getPercent() * 100) + "%");
            }
        });

        testMic.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (audioRecorder == null) audioRecorder = Gdx.audio.newAudioRecorder(samples, true);
                audioRecorder.read(micData, 0, micData.length);
            }
        });

        playMic.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (audioDevice == null) audioDevice = Gdx.audio.newAudioDevice(samples, true);
                new Thread(() -> audioDevice.writeSamples(micData, 0, micData.length)).start();
            }
        });

        applyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Apply new settings
                btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));

                String[] str = resSelectBox.getSelected().split("x");
                if (modeSelectBox.getSelected().equals("Fullscreen")) {
                    for (Graphics.DisplayMode mode : displayModes)
                        if (mode.width == Integer.parseInt(str[0]) && mode.height == Integer.parseInt(str[1])
                                && mode.refreshRate == refreshSelectBox.getSelected()) {
                            Gdx.graphics.setFullscreenMode(mode);
                            break;
                        }
                } else Gdx.graphics.setWindowedMode(Integer.parseInt(str[0]), Integer.parseInt(str[1]));

                Gdx.graphics.setVSync(vsyncCheckBox.isChecked());
                game.setCanDrawFont(showFPSCheckBox.isChecked());

                //Save settings to the Preferences Map
                prefs.putInteger(PrefsKeys.WIDTH, Integer.parseInt(str[0]));
                prefs.putInteger(PrefsKeys.HEIGHT, Integer.parseInt(str[1]));
                prefs.putInteger(PrefsKeys.REFRESHRATE, refreshSelectBox.getSelected());
                if (modeSelectBox.getSelected().equals("Fullscreen"))
                    prefs.putBoolean(PrefsKeys.FULLSCREEN, true);
                else if (modeSelectBox.getSelected().equals("Windowed"))
                    prefs.putBoolean(PrefsKeys.FULLSCREEN, false);
                prefs.putString(PrefsKeys.PLAYERNAME, playerNameField.getText());
                prefs.putBoolean(PrefsKeys.VSYNC, vsyncCheckBox.isChecked());
                prefs.putBoolean(PrefsKeys.SHOWFPS, showFPSCheckBox.isChecked());
                //Write changes to disk
                prefs.flush();

                //when we apply new graphics settings the resolution may have changed so we update the resolution scale
                GameWorld.updateResolutionScale();
                applyButton.setVisible(isAnyChange = false);
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                btnClickSound.play(prefs.getFloat(PrefsKeys.SOUNDVOLUME));
                handleExit();
            }
        });
    }

    private void createDialog() {
        discDialog = new MenuDialog("Changes not applied", "Your changes have not been applied. Discard changes?",
                uiSkin, "dialog");
        /*Button discardBtn = new TextButton(, uiSkin);
        Button cancelBtn = new TextButton(, uiSkin);*/
        discDialog.button("Discard", (Runnable) () -> {
            applyButton.setVisible(isAnyChange = false);
            setSelectedFromPrefs();
            changeToPrevMenu();
        }).button("Cancel");
    }

    private void handleExit() {
        // If there is any change, ask the user to reset settings, or to cancel and apply
        if (isAnyChange) discDialog.show(this, Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.1f)));
        else changeToPrevMenu();
    }

    private void changeToPrevMenu() {
        // We dispose of mic test stuff when we exit options, so it doesnt crash when we go in a game
        if (audioDevice != null) audioDevice.dispose();
        if (audioRecorder != null) audioRecorder.dispose();
        if (parentScreen instanceof MenuScreen) {
            MenuScreen.getRootTable().addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.1f)));
            MenuScreen.setCurrentStage(MenuScreen.getMainMenuStage());
            Gdx.input.setInputProcessor(MenuScreen.getCurrentStage());
        } else if (parentScreen instanceof GameScreen) {
            GameScreen.setState(GameScreen.State.GAME_PAUSED);
            GameScreen.getEscMenuStage().getRootTable().addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.1f)));
            Gdx.input.setInputProcessor(GameScreen.getEscMenuStage());
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (this.getKeyboardFocus() != null && this.getKeyboardFocus() == discDialog) {
            discDialog.hide();
            this.setKeyboardFocus(null);
        } else if (keyCode == Input.Keys.ESCAPE)
            handleExit();
        return super.keyDown(keyCode);
    }
}
