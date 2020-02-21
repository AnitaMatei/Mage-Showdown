package com.mageshowdown.gameclient;

import com.mageshowdown.gamelogic.Laser;
import com.mageshowdown.gamelogic.Round;

public class ClientRound extends Round {

    private class RoundEndManager extends Thread {
        RoundEndManager() {
            start();
        }

        @Override
        public void run() {
            super.run();
            Laser.BurningEffect.clearBurningEffects();
            GameScreen.getGameStage().clearSpells();
        }
    }

    private static final ClientRound INSTANCE = new ClientRound();

    private ClientRound() {
        super();
    }

    @Override
    protected void roundHasEnded() {
        new RoundEndManager();
    }

    public static ClientRound getInstance() {
        return INSTANCE;
    }
}
