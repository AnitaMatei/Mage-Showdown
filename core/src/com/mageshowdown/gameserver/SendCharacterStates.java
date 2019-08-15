package com.mageshowdown.gameserver;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.mageshowdown.packets.Network;

import java.util.ArrayList;

public class SendCharacterStates extends Thread {
    private GameServer myServer = GameServer.getInstance();

    private static final float UPDATE_TIME = .5f;
    private static float timer = 0f;

    private Network.CharacterStates packetToSend;
    private static ServerGameStage gameStage;

    public SendCharacterStates() {
        packetToSend = new Network.CharacterStates();
        packetToSend.states = new ArrayList<>();
        this.gameStage = gameStage;
        start();
    }

    public static void update(){
        timer += Gdx.graphics.getDeltaTime();
        if(timer >=UPDATE_TIME){
            new SendCharacterStates();
            timer =0f;
        }
    }


    public void run() {
        boolean sendPacket = true;
        for (Connection x : myServer.getConnections()) {
            Network.OneCharacterState oneState = new Network.OneCharacterState();
            ServerPlayerCharacter pc = gameStage.getPlayerById(x.getID());

            if (pc != null) {
                oneState.id = x.getID();
                oneState.health = pc.getHealth();
                oneState.energyShield = pc.getEnergyShield();
                oneState.score=pc.getScore();
                oneState.dmgImmune=pc.isDmgImmune();
                oneState.frozen=pc.isFrozen();
                oneState.kills=pc.getKills();

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
