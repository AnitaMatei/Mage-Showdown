package com.mageshowdown.gameclient;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mageshowdown.gamelogic.GameLevel;
import com.mageshowdown.gamelogic.GameWorld;


import java.util.HashMap;

public class ClientGameStage extends Stage {
    private GameLevel gameLevel;
    private OrthographicCamera camera;
    private ClientPlayerCharacter playerCharacter;
    private HashMap<Integer,ClientPlayerCharacter> otherPlayers;


    public ClientGameStage() {
        super();
        camera=new OrthographicCamera(1280f,720f);
        camera.position.x=640f;
        camera.position.y= 360f;
        setViewport(new StretchViewport(1280f,720f,camera));
        gameLevel=new GameLevel(this);
        otherPlayers=new HashMap<Integer, ClientPlayerCharacter>();

    }

    @Override
    public void act() {
        super.act();
        GameWorld.world.step(Gdx.graphics.getDeltaTime(),6,2);

        /*
        * anything that affects the bodies inside a world has to be done after a world has stepped
        * otherwise the game will crash because the world is locked; here the positions and velocities of players' bodies are synchronized
        * and the bodies that have to be removed are removed
        */
        GameWorld.clearBodyRemovalQueue();
        if(playerCharacter!=null)
            playerCharacter.clearQueue();
        for(ClientPlayerCharacter pc:otherPlayers.values()){
            pc.clearQueue();
        }
    }

    @Override
    public void draw() {
        gameLevel.render();
        super.draw();
    }

    public void start(){
        Gdx.input.setInputProcessor(this);
    }

    public ClientPlayerCharacter getPlayerCharacter() {
        return playerCharacter;
    }

    public HashMap<Integer, ClientPlayerCharacter> getOtherPlayers() {
        return otherPlayers;
    }

    public void removePlayerCharacter(int connectionId){
        otherPlayers.get(connectionId).destroyActor();
        otherPlayers.remove(connectionId);
    }

    public void removeMyCharacter(){
        playerCharacter.getFrostCrystal().destroyActor();
        playerCharacter.remove();
    }

    public void spawnMyPlayerCharacter(Vector2 position, String userName){
        playerCharacter=new ClientPlayerCharacter(this,position,userName,true);
        setKeyboardFocus(playerCharacter);
        ClientRound.getInstance().start();
        addActor(ClientRound.getInstance());
    }

    public void spawnOtherPlayer(int id, Vector2 position, String userName){
        otherPlayers.put(id,new ClientPlayerCharacter(this,position,userName,false));
    }


    public GameLevel getGameLevel() {
        return gameLevel;
    }
}
