package com.mageshowdown.gameserver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mageshowdown.gamelogic.GameLevel;
import com.mageshowdown.gamelogic.GameWorld;
import com.mageshowdown.packets.Network;

import java.util.HashMap;

public class ServerGameStage extends Stage {
    private GameLevel gameLevel;
    private OrthographicCamera camera;
    private HashMap<Integer, ServerPlayerCharacter> playerCharacters;

    public ServerGameStage() {
        super();
        camera = new OrthographicCamera(1280f, 720f);
        camera.position.x = 640f;
        camera.position.y = 360f;
        setViewport(new StretchViewport(1280f, 720f, camera));
        gameLevel = new GameLevel(this);
        playerCharacters = new HashMap<Integer, ServerPlayerCharacter>();


        GameWorld.WORLD.setContactListener(new ServerCollisionManager(this));
        addActor(ServerRound.getInstance());
    }

    @Override
    public void draw() {
    }

    @Override
    public void act() {
        super.act();
        getInput();
        GameWorld.stepBox2DWorld();

        GameWorld.clearBodyCreationQueue();
        GameWorld.clearBodyRemovalQueue();

        for (ServerPlayerCharacter x : playerCharacters.values()) {
            x.clearQueue();
        }
    }

    public void addPlayerCharacter(Network.NewPlayerSpawned packet) {
        playerCharacters.put(packet.id, new ServerPlayerCharacter(this, packet.pos, packet.orbEquipped, packet.id));
    }

    public ServerPlayerCharacter getPlayerById(int connectionId) {
        return playerCharacters.get(connectionId);
    }

    public void removePlayerCharacter(int connectionId) {
        if (!playerCharacters.containsKey(connectionId))
            return;
        playerCharacters.get(connectionId).remove();
        playerCharacters.remove(connectionId);
    }

    public void start() {
        gameLevel.setMapServer(2);
        gameLevel.changeLevel();
    }

    public void startRound() {
        ServerRound.getInstance().start();
    }

    public GameLevel getGameLevel() {
        return gameLevel;
    }

    private void getInput() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            GameServer.getInstance().sendMapChange(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            GameServer.getInstance().sendMapChange(2);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            GameServer.getInstance().sendMapChange(3);
        }
    }

    public void clearSpells() {
        for (Integer key : playerCharacters.keySet()) {
            getPlayerById(key).removeCastSpells();
        }
    }

    public int getPlayerCount() {
        return playerCharacters.size();
    }
}
