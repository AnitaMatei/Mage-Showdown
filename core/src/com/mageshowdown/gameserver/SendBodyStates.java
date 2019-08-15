package com.mageshowdown.gameserver;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.mageshowdown.packets.Network;


import java.util.ArrayList;

public class SendBodyStates extends Thread {

    private GameServer myServer = GameServer.getInstance();

    private static final float UPDATE_TIME = 1f;
    private static float timer = 0f;

    private Network.BodyStates packetToSend;
    private static ServerGameStage gameStage;

    public SendBodyStates() {
        packetToSend = new Network.BodyStates();
        packetToSend.states = new ArrayList<>();
        start();
    }

    public static void update(){
        timer += Gdx.graphics.getDeltaTime();
        if(timer >=UPDATE_TIME){
            sendNow();
        }
    }

    public static void sendNow(){
        new SendBodyStates();
        timer=0f;
    }


    public void run() {
        boolean sendPacket = true;
        for (Connection x : myServer.getConnections()) {
            Network.OneBodyState oneState = new Network.OneBodyState();
            ServerPlayerCharacter pc = gameStage.getPlayerById(x.getID());

            if (pc != null) {
                oneState.id = x.getID();
                oneState.pos = pc.getBody().getPosition();

                packetToSend.states.add(oneState);
            } else {
                sendPacket = false;
            }
        }
        if (sendPacket)
            myServer.sendToAllTCP(packetToSend);
    }

    public static void setGameStage(ServerGameStage argGameStage){
        gameStage=argGameStage;
    }
}
