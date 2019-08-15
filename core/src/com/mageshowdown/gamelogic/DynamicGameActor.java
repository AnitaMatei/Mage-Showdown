package com.mageshowdown.gamelogic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class DynamicGameActor extends GameActor {
    public enum VerticalState {
        GROUNDED("idle"),
        FLYING("jumping");

        private String name;

        VerticalState(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public enum HorizontalState {
        GOING_LEFT("running"),
        GOING_RIGHT("running"),
        STANDING("idle");

        private String name;

        HorizontalState(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    protected VerticalState verticalState = VerticalState.FLYING;
    protected HorizontalState horizontalState = HorizontalState.STANDING;


    protected DynamicGameActor(Stage stage, Vector2 position, Vector2 size, Vector2 bodySize, float rotation, Vector2 sizeScaling, boolean isClient) {
        super(stage, position, size, bodySize, rotation, sizeScaling, isClient);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        calculateStates();
    }

    protected void calculateStates(){
        if(body==null || verticalState==null || horizontalState==null)
            return;
        if(body.getLinearVelocity().x>0)
            horizontalState=HorizontalState.GOING_RIGHT;
        else if (body.getLinearVelocity().x<0)
            horizontalState=HorizontalState.GOING_LEFT;
        else horizontalState=HorizontalState.STANDING;

        if(body.getLinearVelocity().y>.0001 && body.getLinearVelocity().y<-.0001)
            verticalState=VerticalState.FLYING;
        else verticalState=VerticalState.GROUNDED;
    }

    public void setHorizontalState(HorizontalState horizontalState) {
        this.horizontalState = horizontalState;
    }

    public void setVerticalState(VerticalState verticalState) {
        this.verticalState = verticalState;
    }

    public HorizontalState getHorizontalState() {
        return horizontalState;
    }

    public VerticalState getVerticalState() {
        return verticalState;
    }

    public void updateGameActor(float delta) {
        super.act(delta);
    }
}
