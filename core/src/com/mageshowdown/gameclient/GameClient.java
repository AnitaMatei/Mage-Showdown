package com.mageshowdown.gameclient;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.mageshowdown.gamelogic.Orb;
import com.mageshowdown.packets.Network;

import java.util.ArrayList;

public class GameClient extends Client {

    private static GameClient instance = new GameClient();

    private String userName;
    //indicates if the client is logged into the server
    private boolean logged = false;

    private GameClient() {
        super();
        registerClasses();
    }

    private void registerClasses() {
        Kryo kryo = getKryo();
        kryo.register(Network.OneCharacterState.class);
        kryo.register(Network.CharacterStates.class);
        kryo.register(Network.OneBodyState.class);
        kryo.register(Network.BodyStates.class);
        kryo.register(Network.PlayerConnected.class);
        kryo.register(Vector2.class);
        kryo.register(Network.UpdatePositions.class);
        kryo.register(ArrayList.class);
        kryo.register(Network.MoveKeyDown.class);
        kryo.register(Network.MoveKeyUp.class);
        kryo.register(Network.CastSpellProjectile.class);
        kryo.register(Network.CastBomb.class);
        kryo.register(Network.LoginRequest.class);
        kryo.register(Network.NewPlayerSpawned.class);
        kryo.register(Network.PlayerDisconnected.class);
        kryo.register(Network.CurrentMap.class);
        kryo.register(Network.PlayerDead.class);
        kryo.register(Network.SwitchOrbs.class);
        kryo.register(Orb.SpellType.class);
    }

    @Override
    public void stop() {
        super.stop();
        logged = false;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public boolean isLogged() {
        return logged;
    }

    public static GameClient getInstance() {
        return instance;
    }
}
