package com.mageshowdown.gameserver;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.mageshowdown.gamelogic.GameWorld;
import com.mageshowdown.packets.Network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GameServer extends Server {

    private static GameServer instance=new GameServer();

    private final int NUMBER_OF_MAPS=3;
    //a hashmap where the values are the usernames and the keys the id's of the players
    private HashMap<Integer,String> users;
    private boolean updatePositions=false;
    private ServerGameStage gameStage;

    private GameServer(){
        super();
        registerClasses();
        users=new HashMap<Integer, String>();
    }


    @Override
    public void bind(int tcpPort, int udpPort){
        try{
            super.bind(tcpPort, udpPort);
        }catch(IOException e){
            System.out.println("Couldnt start to the server");
        }
    }

    private void registerClasses(){
        Kryo kryo = getKryo();
        kryo.register(Network.OneCharacterState.class);
        kryo.register(Network.CharacterStates.class);
        kryo.register(Network.PlayerConnected.class);
        kryo.register(Vector2.class);
        kryo.register(Network.UpdatePositions.class);
        kryo.register(ArrayList.class);
        kryo.register(Network.MoveKeyDown.class);
        kryo.register(Network.KeyUp.class);
        kryo.register(Network.CastSpellProjectile.class);
        kryo.register(Network.CastBomb.class);
        kryo.register(Network.LoginRequest.class);
        kryo.register(Network.NewPlayerSpawned.class);
        kryo.register(Network.PlayerDisconnected.class);
        kryo.register(Network.CurrentMap.class);
        kryo.register(Network.PlayerDead.class);
        kryo.register(Network.SwitchOrbs.class);
    }

    public void sendMapChange(int nr){
        Network.CurrentMap mapToBeSent=new Network.CurrentMap();
        mapToBeSent.nr=nr;
        gameStage.getGameLevel().setMap(nr);
        gameStage.getGameLevel().changeLevel();

        for(Connection x:getConnections()){
            gameStage.getPlayerById(x.getID()).setQueuedPos(generateSpawnPoint(x.getID()));
        }

        GameServer.getInstance().sendToAllTCP(mapToBeSent);
    }

    public int getARandomMap(){
        //we want the next map to always be different so we pick a random one, but were careful not to pick the current one
        int nextMap;

        do{
            nextMap=(new Random().nextInt(NUMBER_OF_MAPS))+1;
        }
        while(nextMap==getGameStage().getGameLevel().getMapNr());

        return nextMap;
    }

    public void startRound(){
        for(Connection x:getConnections()){
            ServerPlayerCharacter pc=gameStage.getPlayerById(x.getID());

            pc.setQueuedPos(generateSpawnPoint(x.getID()));
            pc.setHealth(15);
            pc.setScore(0);
        }
    }

    public void registerRound(){

    }

    public Vector2 generateSpawnPoint(int id){
        ArrayList<Vector2> spawnPoints=gameStage.getGameLevel().getSpawnPoints();
        Vector2 spawnPoint=new Vector2(spawnPoints.get(new Random().nextInt(spawnPoints.size())));

        //before we change the body's position to a spawn point it needs to be converted to box2d coordinates
        return GameWorld.convertPixelsToWorld(spawnPoint);
    }

    public HashMap<Integer, String> getUsers() {
        return users;
    }

    public void addUser(int id, String userName){
        users.put(id, userName);
    }

    public void removeUser(int id){
        users.remove(id);
    }

    public String getUserNameById(int id){
        return users.get(id);
    }

    public void setUpdatePositions(boolean updatePositions) {
        this.updatePositions = updatePositions;
    }

    public boolean getUpdatePositions(){
        return updatePositions;
    }

    public static GameServer getInstance() {
        return instance;
    }

    public void setGameStage(ServerGameStage gameStage) {
        this.gameStage = gameStage;
    }

    public ServerGameStage getGameStage() {
        return gameStage;
    }
}
