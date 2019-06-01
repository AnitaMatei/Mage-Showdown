package com.mageshowdown.gameserver;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mageshowdown.gamelogic.GameWorld;
import com.mageshowdown.packets.Network;

public class ServerListener extends Listener {

    private GameServer myServer = GameServer.getInstance();

    private ServerGameStage gameStage;

    public ServerListener(ServerGameStage gameStage) {
        super();
        this.gameStage = gameStage;
    }


    @Override
    public void connected(Connection connection) {
        System.out.println(connection.getID() + " connected!");
        myServer.sendToTCP(connection.getID(), new Network.LoginRequest());
    }

    @Override
    public void received(Connection connection, Object object) {
        handleLoginRequest(connection, object);
        handleMoveKeyDown(connection, object);
        handleCastSpellProjectile(connection, object);
        handleSwitchOrbs(connection, object);
        handleCastBomb(connection, object);
    }

    @Override
    public void disconnected(Connection connection) {
        super.disconnected(connection);
        Network.PlayerDisconnected toBeSent = new Network.PlayerDisconnected();
        toBeSent.id = connection.getID();

        myServer.removeUser(connection.getID());
        gameStage.removePlayerCharacter(connection.getID());

        myServer.sendToAllExceptTCP(connection.getID(), toBeSent);
    }

    private void handleLoginRequest(Connection connection, Object object) {
        if (object instanceof Network.LoginRequest) {
            /*
             * when a player connects, we send him a login request
             * when we get that packet back, we add him as an user, send him the current map
             * and tell all players that a new player has logged in by sending their username and spawn point
             */
            Network.LoginRequest packet = (Network.LoginRequest) object;

            if (myServer.getUserNameById(connection.getID()) != null)
                return;

            myServer.addUser(connection.getID(), packet.user);
            Network.NewPlayerSpawned toBeSent = new Network.NewPlayerSpawned();
            toBeSent.userName = packet.user;
            toBeSent.id = connection.getID();
            toBeSent.pos = GameWorld.convertWorldToPixels(myServer.generateSpawnPoint(connection.getID()));
            toBeSent.roundTimePassed = ServerRound.getInstance().getTimePassed();
            //players always start with the frost orb equipped
            toBeSent.orbEquipped = 1;
            gameStage.addPlayerCharacter(toBeSent);

            myServer.sendToAllTCP(toBeSent);

            Network.CurrentMap mapToBeSent = new Network.CurrentMap();

            mapToBeSent.nr = gameStage.getGameLevel().getMapNr();
            myServer.sendToTCP(connection.getID(), mapToBeSent);
            /*
             * for the player that just logged in we also need to send him packets
             * with who was already logged in
             */
            for (Connection con : myServer.getConnections()) {
                if (con.getID() != connection.getID()) {
                    toBeSent.userName = myServer.getUserNameById(con.getID());
                    toBeSent.id = con.getID();
                    toBeSent.pos = GameWorld.convertPixelsToWorld(gameStage.getPlayerById(con.getID()).getBody().getPosition());
                    toBeSent.orbEquipped = gameStage.getPlayerById(con.getID()).getEquippedOrb();

                    myServer.sendToTCP(connection.getID(), toBeSent);
                }
            }
        }
    }

    private void handleMoveKeyDown(Connection connection, Object object) {
        if (object instanceof Network.MoveKeyDown) {
            Network.MoveKeyDown packet = (Network.MoveKeyDown) object;
            myServer.setUpdatePositions(true);

            gameStage.getPlayerById(connection.getID()).setMoveDirection(packet.keycode);
        }
    }


    private void handleCastSpellProjectile(Connection connection, Object object) {
        if (object instanceof Network.CastSpellProjectile) {
            Network.CastSpellProjectile packet = (Network.CastSpellProjectile) object;

            gameStage.getPlayerById(connection.getID()).castSpellProjectile(packet);
        }
    }

    private void handleCastBomb(Connection connection, Object object) {
        if (object instanceof Network.CastBomb) {
            Network.CastBomb packet = (Network.CastBomb) object;

            gameStage.getPlayerById(connection.getID()).castBomb(packet);
        }
    }

    private void handleSwitchOrbs(Connection connection, Object object) {
        if (object instanceof Network.SwitchOrbs) {
            Network.SwitchOrbs packet = (Network.SwitchOrbs) object;

            packet.id = connection.getID();
            gameStage.getPlayerById(packet.id).switchMyOrbs();
            myServer.sendToAllExceptTCP(connection.getID(), packet);
        }
    }


}
