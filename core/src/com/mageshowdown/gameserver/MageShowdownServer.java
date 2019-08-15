package com.mageshowdown.gameserver;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.mageshowdown.gamelogic.GameWorld;
import com.mageshowdown.packets.Network;

public class MageShowdownServer extends Game {

    private GameServer myServer = GameServer.getInstance();

    private ServerGameStage gameStage;
    private boolean updatePositions = false;
    private float timePassed = 0f;

    @Override
    public void create() {
        GameWorld.resolutionScale = 1f;
        gameStage = new ServerGameStage();
        myServer.setGameStage(gameStage);
        serverStart();
        ServerAssetLoader.load();
        gameStage.start();
        SendBodyStates.setGameStage(gameStage);
        SendCharacterStates.setGameStage(gameStage);
    }

    @Override
    public void render() {
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " ");

        //if the round is finished we "pause" the stage and only update the round
        if (!ServerRound.getInstance().isFinished())
            gameStage.act();
        else ServerRound.getInstance().act(Gdx.graphics.getDeltaTime());



        if (myServer.getConnections().length > 0 && !ServerRound.getInstance().isFinished()) {
            SendBodyStates.update();
            SendCharacterStates.update();
        }
    }

    @Override
    public void dispose() {
        ServerAssetLoader.dispose();
    }

    public void serverStart() {
        myServer.start();
        myServer.bind(Network.TCP_PORT, Network.UDP_PORT);

        gameStage.startRound();

        myServer.addListener(new ServerListener(gameStage));
    }

}
