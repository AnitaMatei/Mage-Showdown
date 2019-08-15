package com.mageshowdown.gameserver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.mageshowdown.gamelogic.GameWorld;
import com.mageshowdown.gamelogic.Orb;
import com.mageshowdown.packets.Network;

import java.io.*;
import java.sql.*;

import java.util.*;

public class GameServer extends Server {
    private class Player {
        String username;
        int score;
        int kills;

        Player(String username, int score, int kills) {
            this.score = score;
            this.kills = kills;
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public int getKills() {
            return kills;
        }

        public int getScore() {
            return score;
        }

        public void setKills(int kills) {
            this.kills = kills;
        }

        public void setScore(int score) {
            this.score = score;
        }
    }

    private static GameServer instance = new GameServer();

    private final int NUMBER_OF_MAPS = 3;
    //a hashmap where the values are the usernames and the keys the id's of the players
    private HashMap<Integer, Player> users;
    private boolean updatePositions = false;
    private ServerGameStage gameStage;

    private GameServer() {
        super();
        registerClasses();
        users = new HashMap<>();
    }


    @Override
    public void bind(int tcpPort, int udpPort) {
        try {
            super.bind(tcpPort, udpPort);
        } catch (IOException e) {
            Gdx.app.error("start_error", "Couldnt start to the server");
        }
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

    public void sendMapChange(int nr) {
        Network.CurrentMap mapToBeSent = new Network.CurrentMap();
        mapToBeSent.nr = nr;
        gameStage.getGameLevel().setMapServer(nr);
        gameStage.getGameLevel().changeLevel();

        for (Connection x : getConnections()) {
            gameStage.getPlayerById(x.getID()).setQueuedPos(generateSpawnPoint(x.getID()));
        }

        GameServer.getInstance().sendToAllTCP(mapToBeSent);
        SendBodyStates.sendNow();
    }

    public int getARandomMap() {
        //we want the next map to always be different so we pick a random one, but were careful not to pick the current one
        int nextMap;

        do {
            nextMap = (new Random().nextInt(NUMBER_OF_MAPS)) + 1;
        }
        while (nextMap == getGameStage().getGameLevel().getMapNr());

        return nextMap;
    }

    //register the scores of the players who stayed until the end of the round
    public void registerRound() {
        ArrayList<Player> players = getPlayerFromLeaderboard();
        try (PrintWriter writer = new PrintWriter(new File("leaderboard.txt"))) {
            System.out.println(players.size());

            for (Connection connection : getConnections()) {
                ServerPlayerCharacter playerCharacter = gameStage.getPlayerById(connection.getID());
                boolean register = true;
                //if the player was already on the leaderboard he only gets his score and kill count updated in case they are bigger
                for (Player player : players) {
                    System.out.println(player.getUsername()+" "+playerCharacter.getName());
                    if (player.getUsername().equals(getUserNameById(playerCharacter.getId()))) {
                        register = false;
                        if (player.getScore() > playerCharacter.getScore()) {
                            player.setScore(playerCharacter.getScore());
                            player.setKills(playerCharacter.getKills());
                        }
                        break;
                    }
                }
                if (register)
                    players.add(new Player(getUsers().get(connection.getID()).getUsername(), playerCharacter.getScore(), playerCharacter.getKills()));
            }
            players.sort(Comparator.comparing(Player::getScore));
            for (Player player : players) {
                writer.printf("%s %d %d%n", player.getUsername(), player.getScore(), player.getKills());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Gdx.app.log("game_event", "round registered");
    }

    //read the players and their scores from the file
    private ArrayList<Player> getPlayerFromLeaderboard() {
        ArrayList<Player> players = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("leaderboard.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] elements = line.split(" ");
                players.add(new Player(elements[0], Integer.parseInt(elements[1]), Integer.parseInt(elements[2])));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return players;
    }

    public Vector2 generateSpawnPoint(int id) {
        ArrayList<Vector2> spawnPoints = gameStage.getGameLevel().getSpawnPoints();
        Vector2 spawnPoint = new Vector2(spawnPoints.get(new Random().nextInt(spawnPoints.size())));

        //before we change the body's position to a spawn point it needs to be converted to box2d coordinates
        return GameWorld.convertPixelsToWorld(spawnPoint);
    }

    public HashMap<Integer, Player> getUsers() {
        return users;
    }

    public void addUser(int id, String userName) {
        users.put(id, new Player(userName, 0, 0));
    }

    public void removeUser(int id) {
        users.remove(id);
    }

    public String getUserNameById(int id) {
        return users.get(id).getUsername();
    }

    public boolean isUserLoggedById(int id) {
        return users.containsKey(id);
    }

    public void setUpdatePositions(boolean updatePositions) {
        this.updatePositions = updatePositions;
    }

    public boolean getUpdatePositions() {
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
