package com.mageshowdown.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class Round extends Actor {

    protected static final float TIME_BETWEEN_ROUNDS = 10f;
    protected static final float ROUND_LENGTH = 120f;

    protected boolean finished = false;
    protected boolean started = false;

    protected float timePassed;

    protected float timePassedRoundFinished;

    protected Round() {
        start();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!finished && started)
            timePassed += Gdx.graphics.getDeltaTime();

        if (timePassed > ROUND_LENGTH) {
            stop();
        }

        if (finished) {
            timePassedRoundFinished -= Gdx.graphics.getDeltaTime();
            if (timePassedRoundFinished <= 0) {
                start();
            }
        }
    }

    public void start() {
        finished = false;
        started = true;
        timePassedRoundFinished = TIME_BETWEEN_ROUNDS;
    }

    public void stop() {
        finished = true;
        started = false;
        timePassed = 0f;
        roundHasEnded();
    }

    abstract protected void roundHasEnded();

    public float getTimePassed() {
        return timePassed;
    }

    public void setTimePassed(float timePassed) {
        this.timePassed = timePassed;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isStarted() {
        return started;
    }

    public float getTimePassedRoundFinished() {
        return timePassedRoundFinished;
    }

    public static float getTimeBetweenRounds() {
        return TIME_BETWEEN_ROUNDS;
    }

    public static float getRoundLength() {
        return ROUND_LENGTH;
    }

    public void setTimePassedRoundFinished(float timePassedRoundFinished) {
        this.timePassedRoundFinished = timePassedRoundFinished;
    }
}
