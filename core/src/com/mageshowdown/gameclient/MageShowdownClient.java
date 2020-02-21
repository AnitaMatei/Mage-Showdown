package com.mageshowdown.gameclient;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.mageshowdown.gamelogic.GameWorld;
import com.mageshowdown.utils.PrefsKeys;

import static com.mageshowdown.gameclient.ClientAssetLoader.bigSizeFont;

public class MageShowdownClient extends Game {
    private static final MageShowdownClient INSTANCE = new MageShowdownClient();
    private static BitmapFont fpsFont;
    private boolean canDrawFont = false;

    private MageShowdownClient() {
        GameWorld.WORLD.setContactListener(new ClientCollisionManager());
    }

    public static MageShowdownClient getInstance() {
        return INSTANCE;
    }

    public void setCanDrawFont(boolean canDrawFont) {
        this.canDrawFont = canDrawFont;
    }

    @Override
    public void create() {
        //Here we specify if we want to display logs in the console
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        GameWorld.updateResolutionScale();
        ClientAssetLoader.getInstance().loadLoadingScreen();

        this.setScreen(new LoadingScreen());
        fpsFont = new BitmapFont(bigSizeFont.getData(), bigSizeFont.getRegion(), bigSizeFont.usesIntegerPositions());
        fpsFont.setColor(Color.YELLOW);
        canDrawFont = ClientAssetLoader.prefs.getBoolean(PrefsKeys.SHOWFPS);
    }

    @Override
    public void render() {
        super.render();
        if (canDrawFont)
            if (this.getScreen() == MenuScreen.getInstance() && MenuScreen.getBatch() != null) {
                MenuScreen.getBatch().begin();
                fpsFont.draw(MenuScreen.getBatch(), Integer.toString(Gdx.graphics.getFramesPerSecond()), 0, Gdx.graphics.getHeight());
                MenuScreen.getBatch().end();
            } else if (this.getScreen() == GameScreen.getInstance() && GameScreen.getBatch() != null) {
                GameScreen.getBatch().begin();
                fpsFont.draw(GameScreen.getBatch(), Integer.toString(Gdx.graphics.getFramesPerSecond()), 0, Gdx.graphics.getHeight());
                GameScreen.getBatch().end();
            }
    }

    @Override
    public void dispose() {
        super.dispose();
        ClientAssetLoader.getInstance().dispose();
        fpsFont.dispose();
    }
}
