package com.mageshowdown.gameclient;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LoadingScreen implements Screen {

    private AssetManager manager = ClientAssetLoader.getInstance().manager;
    private Stage stage;
    private ProgressBar loadingBar;
    private boolean ldingAssetsFinished = false;
    private long startTime;

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Table table = new Table();
        table.setFillParent(true);

        //Loading the assets for the loading screen
        manager.update(1000);
        if (manager.isFinished()) {
            ClientAssetLoader.getInstance().setLoadingAssets();

//            TiledDrawable tiledDrawable = ClientAssetLoader.hudSkin.getTiledDrawable("progress-bar-life");
//            tiledDrawable.setMinWidth(0f);
//            tiledDrawable.setLeftWidth(100f);
//            ClientAssetLoader.hudSkin.get("default-horizontal", ProgressBar.ProgressBarStyle.class).knobBefore = tiledDrawable;
            loadingBar = new ProgressBar(0f, 1f, 0.1f, false, ClientAssetLoader.hudSkin, "default-horizontal");
            loadingBar.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
            table.add(loadingBar).width(500);
            stage.addActor(table);
            ClientAssetLoader.getInstance().load();
            ldingAssetsFinished = true;
        }

        startTime = System.currentTimeMillis();
        //Loading the rest of the assets
//        manager.update(500);
//        if (manager.isFinished()) {
//            float timeElapsed = System.currentTimeMillis() - startTime;
//            ClientAssetLoader.getInstance().setAssets();
//            MageShowdownClient.getInstance().setScreen(MenuScreen.getInstance());
//            Gdx.app.log("load_time", "Loading took " + timeElapsed + " milliseconds");
//        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(255, 255, 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (ldingAssetsFinished) {
            loadingBar.setValue(manager.getProgress());
            stage.act();
            stage.draw();

            if (manager.update()) {
                ClientAssetLoader.getInstance().setAssets();
                MageShowdownClient.getInstance().setScreen(MenuScreen.getInstance());
                long timeElapsed = System.currentTimeMillis() - startTime;
                Gdx.app.log("load_time", "Loading took " + timeElapsed + " milliseconds");
            }
        }
    }

    @Override
    public void resize(int width, int height) {
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
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
